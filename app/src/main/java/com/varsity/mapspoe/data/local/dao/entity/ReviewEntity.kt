package com.varsity.mapspoe.data.local.dao.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "reviews", indices = [Index("storeId")])
data class ReviewEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val author: String,
    val rating: Int,
    val comment: String,
    val createdAt: Long
)
