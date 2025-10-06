package com.varsity.mapspoe.data.legacy

data class User(
    val name: String,     // Full name of the user
    val email: String,    // Email address used for login and identification
    val password: String
)

data class Dispensary(
    val id: Int,
    val name: String,
    val address: String,
    val distance: String,
    val rating: Double,
    val isOpen: Boolean
)
