package com.varsity.mapspoe.data.local.dao.entity

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDAO {
    @Upsert
    suspend fun upsertAll(reviews: List<ReviewEntity>)

    @Query("SELECT * FROM reviews WHERE storeId = :storeId ORDER BY createdAt DESC")
    fun getByStore(storeId: String): Flow<List<ReviewEntity>>
}

