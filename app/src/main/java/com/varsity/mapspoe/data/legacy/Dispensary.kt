package com.varsity.mapspoe.data.legacy

data class Dispensary(
    val id: Int,
    val name: String,
    val address: String,
    val distance: String,
    val rating: Double,
    val isOpen: Boolean
)
