package com.varsity.mapspoe.data.local.dao.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val address: String,
    val phone: String?,
    val openHours: String?,
    val ratingAvg: Double,
    val ratingCount: Int,
    val googlePlaceId: String? = null // optional, for later Google reviews
)
