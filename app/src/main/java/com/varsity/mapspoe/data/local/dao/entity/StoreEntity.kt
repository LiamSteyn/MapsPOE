package com.varsity.mapspoe.data.local.dao.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double?,
    val longitude: Double?,
    val address: String?,
    val phone: String?,
    val hours: String?,
    val rating: Double?,
    val ratingCount: Int?,
    val googlePlaceId: String?
)