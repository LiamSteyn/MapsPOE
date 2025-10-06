// data/remote/google/PlacesApiService.kt
package com.varsity.mapspoe.data.remote.google

import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    @GET("place/textsearch/json")
    suspend fun textSearch(
        @Query("query") query: String,         // e.g., "CannaKingdom, Cape Town"
        @Query("key") apiKey: String
    ): PlacesTextSearchResponse

    @GET("place/details/json")
    suspend fun placeDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String,       // "place_id,name,rating,user_ratings_total,reviews,geometry,formatted_address,opening_hours"
        @Query("key") apiKey: String
    ): PlaceDetailsResponse
}
