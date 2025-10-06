package com.varsity.mapspoe.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.varsity.mapspoe.core.DataResult
import com.varsity.mapspoe.data.local.dao.entity.AppDatabase
import com.varsity.mapspoe.data.local.dao.entity.ReviewEntity
import com.varsity.mapspoe.data.local.dao.entity.StoreEntity
import com.varsity.mapspoe.data.remote.InMemoryReviewService
import com.varsity.mapspoe.data.remote.ReviewService
import com.varsity.mapspoe.domain.toDomain
import com.varsity.mapspoe.domain.toEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DataRepositoryTest {

    @get:Rule val instant = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        // Minimal seed (store + one review)
        val store = StoreEntity(
            id = "st_1",
            name = "Green Leaf Dispensary",
            lat = -33.9249,
            lon = 18.4241,
            address = "123 Long St, Cape Town",
            phone = "+27 21 000 0001",
            openHours = "09:00-20:00",
            ratingAvg = 4.5,
            ratingCount = 128,
            googlePlaceId = null
        )
        val review = ReviewEntity(
            id = "rv_1", storeId = "st_1", author = "Zanele",
            rating = 5, comment = "Friendly staff", createdAt = 100L
        )

        runBlocking {
            db.storeDao().upsertAll(listOf(store))
            db.reviewDao().upsertAll(listOf(review))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getStoreFoundReturnsSuccess() = runBlocking {
        val repo = TestHarness(db, InMemoryReviewService())
        val r = repo.getStore("st_1")
        assert(r is DataResult.Success)
        val s = (r as DataResult.Success).data
        Assert.assertEquals("st_1", s.id)
    }

    @Test
    fun getStoreMissingReturnsEmpty() = runBlocking {
        val repo = TestHarness(db, InMemoryReviewService())
        val r = repo.getStore("does_not_exist")
        assert(r is DataResult.Empty)
    }

    @Test
    fun refreshReviewsHandlesFailureReturnsError() = runBlocking {
        val repo = TestHarness(db, InMemoryReviewService(errorRate = 1.0))
        val r = repo.refreshReviews("st_1")
        assert(r is DataResult.Error)
    }

    @Test
    fun refreshReviewsSuccessPersistsToRoom() = runBlocking {
        val repo = TestHarness(db, InMemoryReviewService(errorRate = 0.0))

        val countBefore = db.reviewDao().getByStore("st_1").first().size

        val res = repo.refreshReviews("st_1")
        assert(res is DataResult.Success)

        val listAfter = db.reviewDao().getByStore("st_1").first()
        Assert.assertTrue(listAfter.size >= countBefore) // upsert shouldn't reduce rows
    }
}

/**
 * Tiny harness mirroring DataRepository behavior for unit tests.
 * Avoids singleton init & Context; injects DB and service directly.
 */
private class TestHarness(
    private val db: AppDatabase,
    private val service: ReviewService
) {
    suspend fun getStore(storeId: String): DataResult<com.varsity.mapspoe.domain.Store> {
        val row = db.storeDao().getById(storeId)
        return when (row) {
            null -> DataResult.Empty("Store not found")
            else -> DataResult.Success(row.toDomain())
        }
    }

    suspend fun refreshReviews(storeId: String): DataResult<Unit> =
        runCatching {
            val remote = service.getReviewsForStore(storeId)
            if (remote.isNotEmpty()) {
                db.reviewDao().upsertAll(remote.map { it.toEntity() })
            }
            DataResult.Success(Unit)
        }.recover { t ->
            DataResult.Error("REV_FETCH_FAIL", "Failed to fetch latest reviews", t)
        }.getOrThrowIfError()

    private fun <T> Result<DataResult<T>>.getOrThrowIfError(): DataResult<T> =
        fold(
            onSuccess = { it },
            onFailure = { t -> DataResult.Error("UNEXPECTED", t.message ?: "Unexpected error", t) }
        )
}
