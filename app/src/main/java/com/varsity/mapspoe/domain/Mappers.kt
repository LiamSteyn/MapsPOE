package com.varsity.mapspoe.domain

import com.varsity.mapspoe.data.local.dao.entity.ReviewEntity
import com.varsity.mapspoe.data.local.dao.entity.StoreEntity

fun StoreEntity.toDomain() = Store(id, name, lat, lon, address, phone, openHours, ratingAvg, ratingCount, googlePlaceId)
fun ReviewEntity.toDomain() = Review(id, storeId, author, rating, comment, createdAt)

fun Review.toEntity() = ReviewEntity(id, storeId, author, rating, comment, createdAt)
