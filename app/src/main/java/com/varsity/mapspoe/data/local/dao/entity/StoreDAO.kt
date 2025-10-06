// app/src/main/java/com/varsity/mapspoe/data/local/dao/entity/StoreDAO.kt
package com.varsity.mapspoe.data.local.dao.entity

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDAO {
    @Upsert
    suspend fun upsertAll(stores: List<StoreEntity>)

    @Query("SELECT * FROM stores ORDER BY name")
    fun getStores(): Flow<List<StoreEntity>>

    // 👇 add this helper for one-shot fetch
    @Query("SELECT * FROM stores ORDER BY name")
    suspend fun getStoresOnce(): List<StoreEntity>

    @Query("SELECT * FROM stores WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): StoreEntity?

    @Query("SELECT * FROM stores WHERE googlePlaceId = :pid LIMIT 1")
    suspend fun getByGooglePlaceId(pid: String): StoreEntity?

    // already added earlier for map -> reviews
    @Query("SELECT * FROM stores WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): StoreEntity?
}
