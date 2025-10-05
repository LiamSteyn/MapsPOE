package com.varsity.mapspoe.data

// Data class representing a user of the app
data class User(
    val name: String,     // Full name of the user
    val email: String,    // Email address used for login and identification
    val password: String  // User's password (stored in plain text here for simplicity; consider hashing in real apps)
)
