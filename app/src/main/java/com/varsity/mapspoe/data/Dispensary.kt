package com.varsity.mapspoe.data

// Data class representing a dispensary
data class Dispensary(
    val id: Int,          // Unique identifier for the dispensary
    val name: String,     // Name of the dispensary
    val address: String,  // Physical address of the dispensary
    val distance: String, // Distance from the user (e.g., "1.2 km")
    val rating: Double,   // Average rating of the dispensary (e.g., 4.5)
    val isOpen: Boolean   // Whether the dispensary is currently open (true = open, false = closed)
)
