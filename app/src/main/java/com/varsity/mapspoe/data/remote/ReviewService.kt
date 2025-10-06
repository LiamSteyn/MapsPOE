package com.varsity.mapspoe.data.remote

import com.varsity.mapspoe.domain.Review

interface ReviewService {
    suspend fun getReviewsForStore(storeId: String): List<Review>
}
