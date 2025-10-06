package com.varsity.mapspoe.data.remote

import com.varsity.mapspoe.domain.Review

interface RealReviewsProvider {
    suspend fun ensurePlaceIdForStore(storeId: String, name: String, cityHint: String): String?
    suspend fun getReviews(placeId: String): List<Review>
}
