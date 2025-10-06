package com.varsity.mapspoe.data.remote

import com.varsity.mapspoe.domain.Review
import kotlinx.coroutines.delay
import kotlin.random.Random

class InMemoryReviewService(
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val errorRate: Double = 0.0
) : ReviewService {
    private val authors = listOf("Ava", "Musa", "Thando", "Jade", "Yusuf")
    override suspend fun getReviewsForStore(storeId: String): List<Review> {
        if (errorRate > 0 && Random.nextDouble() < errorRate) {
            throw RuntimeException("Simulated network failure")
        }
        delay(150)
        val now = clock()
        return (1..3).map {
            Review(
                id = "mem_${storeId}_$it",
                storeId = storeId,
                author = authors.random(),
                rating = (3..5).random(),
                comment = listOf("Solid service.", "Consistent quality.", "Helpful staff.", "Clean store.", "Great prices.").random(),
                createdAt = now - it * 60_000L
            )
        }
    }
}
