// data/remote/google/PlacesClient.kt
package com.varsity.mapspoe.data.remote.google

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object PlacesClient {
    fun create(baseUrl: String = "https://maps.googleapis.com/maps/api/"): PlacesApiService =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(PlacesApiService::class.java)
}
