// app/src/main/java/com/varsity/mapspoe/data/DataRepository.kt
package com.varsity.mapspoe.data

import android.content.Context
import android.util.Log
import com.varsity.mapspoe.core.DataResult
import com.varsity.mapspoe.data.legacy.LegacyAuth
import com.varsity.mapspoe.data.local.dao.entity.AppDatabase
import com.varsity.mapspoe.data.local.dao.entity.ReviewEntity
import com.varsity.mapspoe.data.local.dao.entity.StoreEntity
import com.varsity.mapspoe.data.remote.InMemoryReviewService
import com.varsity.mapspoe.data.remote.RealReviewsProvider
import com.varsity.mapspoe.data.remote.ReviewService
import com.varsity.mapspoe.domain.Review
import com.varsity.mapspoe.domain.Store
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataRepository {

    @Volatile private var db: AppDatabase? = null
    @Volatile private var reviewService: ReviewService? = null

    @Volatile private var googleProvider: RealReviewsProvider? = null
    @Volatile private var googleEnabled: Boolean = false

    fun init(context: Context, simulateErrors: Boolean = false) {
        if (db == null || reviewService == null) {
            synchronized(this) {
                if (db == null) {
                    db = AppDatabase.get(context.applicationContext)
                }
                if (reviewService == null) {
                    reviewService = InMemoryReviewService(
                        errorRate = if (simulateErrors) 1.0 else 0.0
                    )
                }
            }
        }
    }
    // ========= Legacy demo-only data (in-memory) for DispensaryDetailsActivity =========
    private val legacyDispensaries = mutableListOf<com.varsity.mapspoe.data.legacy.Dispensary>().apply {
        if (isEmpty()) addAll(
            listOf(
                com.varsity.mapspoe.data.legacy.Dispensary(
                    id = 1,
                    name = "Green Leaf Dispensary",
                    address = "123 Long St, Cape Town",
                    distance = "1.2 km",
                    rating = 4.6,
                    isOpen = true
                ),
                com.varsity.mapspoe.data.legacy.Dispensary(
                    id = 2,
                    name = "Herbal House",
                    address = "45 Long Street, Cape Town",
                    distance = "2.3 km",
                    rating = 4.2,
                    isOpen = false
                ),
                com.varsity.mapspoe.data.legacy.Dispensary(
                    id = 3,
                    name = "Mary Jane Market",
                    address = "78 Bree St, Cape Town",
                    distance = "3.1 km",
                    rating = 4.9,
                    isOpen = true
                )
            )
        )
    }

    /** Legacy getter used by DispensaryDetailsActivity (kept for backward-compat). */
    @JvmStatic
    fun getDispensaries(): List<com.varsity.mapspoe.data.legacy.Dispensary> = legacyDispensaries.toList()

    // ===== Legacy demo-only auth shims (keeps RegisterActivity/LoginActivity working) =====
    @JvmStatic fun registerUser(user: com.varsity.mapspoe.data.legacy.User): Boolean =
        LegacyAuth.register(user)

    @JvmStatic fun login(email: String, password: String): Boolean =
        LegacyAuth.login(email, password)

    @JvmStatic fun getUsers(): List<com.varsity.mapspoe.data.legacy.User> =
        LegacyAuth.users()

    // Enables/disables simulated errors for the in-memory review service (demo/testing).
    @JvmStatic
    fun enableErrorSimulation(enable: Boolean) {
        // Ensure repo is initialized if someone calls this early
        if (reviewService == null) {
            reviewService = InMemoryReviewService(errorRate = if (enable) 1.0 else 0.0)
            return
        }
        // Swap the current service instance
        reviewService = InMemoryReviewService(errorRate = if (enable) 1.0 else 0.0)
    }


    // ---------------- Stores (Room -> Domain) ----------------
    fun observeStores(): Flow<List<Store>> =
        requireDb().storeDao()
            .getStores()
            .map { rows -> rows.map { it.asDomain() } }

    suspend fun getStore(storeId: String): DataResult<Store> {
        val row = requireDb().storeDao().getById(storeId)
        return if (row == null) DataResult.Empty("Store not found")
        else DataResult.Success(row.asDomain())
    }

    // ---------------- Reviews (Room + Providers) ----------------
    fun observeReviews(storeId: String): Flow<List<Review>> =
        requireDb().reviewDao()
            .getByStore(storeId)
            .map { it.map { r -> r.asDomain() } }

    /**
     * Refresh reviews for a store.
     * Priority: Google (if enabled and store has placeId) -> in-memory fallback.
     * Persists successful fetches to Room. Returns DataResult, never throws.
     */
    suspend fun refreshReviews(storeId: String): DataResult<Unit> =
        runCatching {
            val db = requireDb()
            val store = db.storeDao().getById(storeId)
            val placeId = store?.googlePlaceId

            val incoming: List<Review> = when {
                googleEnabled && !placeId.isNullOrBlank() -> {
                    val googleReviews = googleProvider?.getReviews(placeId).orEmpty()
                    // remap to our local storeId before persisting
                    googleReviews.map { it.copy(storeId = storeId) }
                }
                else -> requireService().getReviewsForStore(storeId)
            }

            if (incoming.isNotEmpty()) {
                db.reviewDao().upsertAll(incoming.map { it.asEntity() })
            }
            DataResult.Success(Unit)
        }.recover { t ->
            if (t is CancellationException) throw t
            Log.e("DataRepository", "refreshReviews failed: ${t.message}", t)
            DataResult.Error("REV_FETCH_FAIL", "Failed to fetch latest reviews", t)
        }.getOrThrowIfError()

    fun enableGooglePlaces(provider: RealReviewsProvider?, enable: Boolean) {
        googleEnabled = enable && provider != null
        googleProvider = provider
    }

    /** Resolve and persist Google placeId for a store (idempotent). */
    suspend fun resolveAndAttachPlaceId(
        storeId: String,
        name: String,
        cityHint: String = "Cape Town"
    ) {
        if (!googleEnabled) return
        val db = requireDb()
        val current = db.storeDao().getById(storeId) ?: return
        if (current.googlePlaceId.isNullOrBlank()) {
            val pid = googleProvider?.ensurePlaceIdForStore(storeId, name, cityHint) ?: return
            db.storeDao().upsertAll(listOf(current.copy(googlePlaceId = pid)))
        }
    }

    // ---------------- helpers ----------------
    private fun requireDb(): AppDatabase =
        db ?: error("DataRepository.init(context) must be called before use")

    private fun requireService(): ReviewService =
        reviewService ?: error("DataRepository.init(context) must be called before use")

    private fun <T> Result<DataResult<T>>.getOrThrowIfError(): DataResult<T> =
        fold(onSuccess = { it }, onFailure = { t ->
            if (t is CancellationException) throw t
            DataResult.Error("UNEXPECTED", t.message ?: "Unexpected error", t)
        })

    // ---- Local mappers to avoid extension ambiguity ----
    private fun StoreEntity.asDomain() = Store(
        id = id,
        name = name,
        lat = latitude ?: 0.0,          // Store expects lat (Double, non-null)
        lon = longitude ?: 0.0,         // Store expects lon (Double, non-null)
        address = address.orEmpty(),
        phone = phone,
        openHours = hours,              // Store expects openHours
        ratingAvg = rating ?: 0.0,      // Store expects ratingAvg (Double, non-null)
        ratingCount = ratingCount ?: 0, // Store expects ratingCount (Int, non-null)
        googlePlaceId = googlePlaceId
    )


    private fun ReviewEntity.asDomain() = Review(
        id = id,
        storeId = storeId,
        author = author,
        rating = rating,
        comment = comment,
        createdAt = createdAt
    )

    private fun Review.asEntity() = ReviewEntity(
        id = id,
        storeId = storeId,
        author = author,
        rating = rating,
        comment = comment,
        createdAt = createdAt
    )
}
