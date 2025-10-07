package com.varsity.mapspoe.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long? = null,       // matches BIGINT and identity in Postgres
    val name: String,           // NOT NULL
    val age: Int?,
    val email: String? = null,  // nullable
    val password: String? = null // nullable
)
