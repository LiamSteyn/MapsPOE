// app/src/main/java/com/varsity/mapspoe/data/DataRepository.kt
package com.varsity.mapspoe.data

import android.content.Context
import android.util.Log
import com.varsity.mapspoe.core.DataResult
import com.varsity.mapspoe.data.legacy.Dispensary
import com.varsity.mapspoe.data.legacy.User
import com.varsity.mapspoe.data.local.dao.entity.AppDatabase
import com.varsity.mapspoe.data.local.dao.entity.seedDatabase
import com.varsity.mapspoe.data.remote.GooglePlacesReviewsProvider
import com.varsity.mapspoe.data.remote.InMemoryReviewService
import com.varsity.mapspoe.data.remote.RealReviewsProvider
import com.varsity.mapspoe.data.remote.ReviewService
import com.varsity.mapspoe.domain.Review
import com.varsity.mapspoe.domain.Store
import com.varsity.mapspoe.domain.toDomain
import com.varsity.mapspoe.domain.toEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataRepository {

    // ========= Legacy demo-only data (IN-MEMORY) =========
    private val users = mutableListOf<User>()

    /** Demo list shown in DashboardActivity (legacy). Safe to remove later. */
    private val dispensaries = mutableListOf<Dispensary>()

    init {
        // Seed the legacy list once (does not affect Room)
        if (dispensaries.isEmpty()) {
            dispensaries += listOf(
                Dispensary(1, "Green Leaf Dispensary", "123 Main St, Cape Town", "1.2 km", 4.6, true),
                Dispensary(2, "Herbal House",          "45 Long Street, Cape Town", "2.3 km", 4.2, false),
                Dispensary(3, "Mary Jane Market",       "78 Bree St, Cape Town",    "3.1 km", 4.9, true)
            )
        }
    }

    /** Legacy getter used by DashboardActivity ListView. */
    @JvmStatic fun getDispensaries(): List<Dispensary> = dispensaries.toList()

    /** Exposed for ProfileActivity (Java). */
    @JvmStatic fun getUsers(): List<User> = users.toList()

    /** (Optional) legacy auth helpers, keep if your login screen calls them. */
    fun registerUser(user: User): Boolean {
        if (users.any { it.email == user.email }) return false
        users += user
        return true
    }
    fun login(email: String, password: String): Boolean =
        users.any { it.email == email && it.password == password }

    /** Hardcoded demo reviews (per legacy spec). Used only if you want a quick details screen. */
    fun getHardcodedReviewsFor(dispensaryId: Int): List<Review> = when (dispensaryId) {
        1 -> listOf(
            Review(id = "legacy_1_a", storeId = "st_1", author = "Zanele", rating = 5, comment = "Friendly staff, fast service.", createdAt = System.currentTimeMillis() - 86_400_000L),
            Review(id = "legacy_1_b", storeId = "st_1", author = "Kyle",   rating = 4, comment = "Good quality, slightly pricey.", createdAt = System.currentTimeMillis() - 172_800_000L)
        )
        2 -> listOf(
            Review(id = "legacy_2_a", storeId = "st_2", author = "Amahle", rating = 5, comment = "Great selection!", createdAt = System.currentTimeMillis() - 259_200_000L),
            Review(id = "legacy_2_b", storeId = "st_2", author = "Leo",    rating = 3, comment = "Okay experience.", createdAt = System.currentTimeMillis() - 345_600_000L)
        )
        3 -> listOf(
            Review(id = "legacy_3_a", storeId = "st_3", author = "Naledi", rating = 5, comment = "Best in the area.", createdAt = System.currentTimeMillis() - 432_000_000L),
            Review(id = "legacy_3_b", storeId = "st_3", author = "Josh",   rating = 4, comment = "Nice deals on weekends.", createdAt = System.currentTimeMillis() - 518_400_000L)
        )
        else -> emptyList()
    }

    // ========= New architecture: Room + Fake/Real Reviews =========

    @Volatile private var db: AppDatabase? = null
    @Volatile private var reviewService: ReviewService? = null

    // Google Places (real reviews)
    @Volatile private var googleProvider: RealReviewsProvider? = null
    @Volatile private var googleEnabled: Boolean = false

    // Firebase (optional mirror/cache)
    // @Volatile private var firebaseSvc: FirebaseReviewService? = null
    @Volatile private var firebaseEnabled: Boolean = false

    /**
     * Call once (e.g., MainActivity.onCreate). Safe to call multiple times; no-op after first.
     */
    fun init(context: Context, simulateErrors: Boolean = false) {
        if (db == null || reviewService == null) {
            synchronized(this) {
                if (db == null) {
                    db = AppDatabase.get(context.applicationContext)
                    seedDatabase(requireDb())
                }
                if (reviewService == null) {
                    reviewService = InMemoryReviewService(
                        errorRate = if (simulateErrors) 1.0 else 0.0
                    )
                }
            }
        }
    }

    /** Toggle in-memory failure simulation (for demo/testing). */
    fun enableErrorSimulation(enable: Boolean) {
        reviewService = InMemoryReviewService(errorRate = if (enable) 1.0 else 0.0)
    }

    /** Enable/disable Google Places reviews. If enabled, pass API key or null to read from Manifest. */
    fun enableGooglePlaces(context: Context, apiKey: String? = null, enable: Boolean) {
        googleEnabled = enable
        googleProvider = if (!enable) null
        else GooglePlacesReviewsProvider.create(context, apiKey)
    }

    /** Enable/disable Firebase mirroring/cache (requires FirebaseReviewService in your project). */
    fun enableFirebase(enable: Boolean) {
        firebaseEnabled = enable
        // firebaseSvc = if (enable) FirebaseReviewService.create() else null
    }

    // ---------------- Stores (Room) ----------------

    fun observeStores(): Flow<List<Store>> =
        requireDb().storeDao().getStores().map { rows -> rows.map { it.toDomain() } }

    suspend fun getStore(storeId: String): DataResult<Store> {
        val row = requireDb().storeDao().getById(storeId)
        return if (row == null) DataResult.Empty("Store not found")
        else DataResult.Success(row.toDomain())
    }

    // ---------------- Reviews (Room + Providers) ----------------

    fun observeReviews(storeId: String): Flow<List<Review>> =
        requireDb().reviewDao().getByStore(storeId).map { it.map { r -> r.toDomain() } }

    /**
     * Refresh reviews for a store.
     * Priority: Google (if enabled and store has googlePlaceId) -> Firebase (if enabled) -> InMemory (fake).
     * Always persists successful fetches to Room. Returns DataResult, never throws.
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
                    googleReviews.map { it.copy(storeId = storeId) }.also {
                        // if (firebaseEnabled) firebaseSvc?.upsertAll(it)
                    }
                }
                // firebaseEnabled && firebaseSvc != null -> {
                //     firebaseSvc!!.getReviewsForStore(storeId)
                // }
                else -> {
                    // Default fake reviews
                    requireService().getReviewsForStore(storeId)
                }
            }

            if (incoming.isNotEmpty()) {
                db.reviewDao().upsertAll(incoming.map { it.toEntity() })
            }
            DataResult.Success(Unit)
        }.recover { t ->
            if (t is CancellationException) throw t
            Log.e("DataRepository", "refreshReviews failed: ${t.message}", t)
            DataResult.Error("REV_FETCH_FAIL", "Failed to fetch latest reviews", t)
        }.getOrThrowIfError()

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
}
