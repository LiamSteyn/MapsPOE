package com.varsity.mapspoe.core

sealed class DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>()
    data class Empty(val reason: String? = null) : DataResult<Nothing>()
    data class Error(val code: String, val message: String, val cause: Throwable? = null) : DataResult<Nothing>()
}
