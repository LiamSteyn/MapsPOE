// app/src/main/java/com/varsity/mapspoe/domain/EntityMappers.kt
package com.varsity.mapspoe.domain

import com.varsity.mapspoe.data.local.dao.entity.ReviewEntity
import com.varsity.mapspoe.data.local.dao.entity.StoreEntity

// StoreEntity -> Store (map nullable entity fields to non-null domain fields)
fun StoreEntity.toDomain() = Store(
    id = id,
    name = name,
    lat = latitude ?: 0.0,                 // entity.latitude -> domain.lat
    lon = longitude ?: 0.0,                // entity.longitude -> domain.lon
    address = address.orEmpty(),           // domain expects String (non-null)
    phone = phone,
    openHours = hours,                     // entity.hours -> domain.openHours
    ratingAvg = rating ?: 0.0,             // entity.rating -> domain.ratingAvg
    ratingCount = ratingCount ?: 0,
    googlePlaceId = googlePlaceId
)

fun ReviewEntity.toDomain() = Review(
    id = id,
    storeId = storeId,
    author = author,
    rating = rating,
    comment = comment,
    createdAt = createdAt
)

// Domain -> Entity
fun Review.toEntity() = ReviewEntity(
    id = id,
    storeId = storeId,
    author = author,
    rating = rating,
    comment = comment,
    createdAt = createdAt
)
