package com.varsity.mapspoe.data.local.dao.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDAO {
    @Query("SELECT * FROM reviews WHERE storeId = :storeId ORDER BY createdAt DESC")
    fun getByStore(storeId: String): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ReviewEntity>)
}
