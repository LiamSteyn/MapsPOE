// data/remote/google/PlacesModels.kt
package com.varsity.mapspoe.data.remote.google

import com.squareup.moshi.Json

data class PlacesTextSearchResponse(
    val results: List<TextSearchResult> = emptyList(),
    val status: String
)
data class TextSearchResult(
    @Json(name="place_id") val placeId: String,
    val name: String?,
    val geometry: Geometry?,
    @Json(name="formatted_address") val formattedAddress: String?
)
data class Geometry(val location: LatLng)
data class LatLng(val lat: Double, val lng: Double)

data class PlaceDetailsResponse(val result: PlaceDetails?, val status: String)
data class PlaceDetails(
    @Json(name="place_id") val placeId: String,
    val name: String?,
    val rating: Double?,
    @Json(name="user_ratings_total") val userRatingsTotal: Int?,
    val geometry: Geometry?,
    @Json(name="formatted_address") val formattedAddress: String?,
    @Json(name="opening_hours") val openingHours: OpeningHours?,
    val reviews: List<GoogleReview>?
)
data class OpeningHours(@Json(name="weekday_text") val weekdayText: List<String>?)
data class GoogleReview(
    @Json(name="author_name") val authorName: String?,
    val rating: Int?,
    val text: String?,
    @Json(name="time") val unixTime: Long?
)
