package com.varsity.mapspoe.domain

data class Store(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val address: String,
    val phone: String?,
    val openHours: String?,
    val ratingAvg: Double,
    val ratingCount: Int,
    val googlePlaceId: String? = null
)

data class Review(
    val id: String,
    val storeId: String,
    val author: String,
    val rating: Int,
    val comment: String,
    val createdAt: Long
)

