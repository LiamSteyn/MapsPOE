package com.varsity.mapspoe.data.remote

import com.varsity.mapspoe.domain.Review

class FirebaseReviewService : ReviewService {
    override suspend fun getReviewsForStore(storeId: String): List<Review> {
        // TODO: implement after adding Firebase Firestore dependency
        return emptyList()
    }
}
