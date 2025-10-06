// app/src/main/java/com/varsity/mapspoe/data/remote/GooglePlacesReviewsProvider.kt
package com.varsity.mapspoe.data.remote

import com.squareup.moshi.Json
import com.varsity.mapspoe.data.local.dao.entity.AppDatabase
import com.varsity.mapspoe.domain.Review
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// NOTE: The RealReviewsProvider interface is defined in another file in this package.
// Do NOT redeclare it here.

class GooglePlacesReviewsProvider(
    private val apiKey: String,
    private val db: AppDatabase
) : RealReviewsProvider {

    private val api = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/maps/api/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(PlacesApi::class.java)

    override suspend fun ensurePlaceIdForStore(
        storeId: String,
        name: String,
        cityHint: String
    ): String? = withContext(Dispatchers.IO) {
        val store = db.storeDao().getById(storeId) ?: return@withContext null
        store.googlePlaceId?.takeIf { it.isNotBlank() }?.let { return@withContext it }

        val ts = api.textSearch("$name, $cityHint", apiKey)
        val hit = ts.results.firstOrNull() ?: return@withContext null

        db.storeDao().upsertAll(
            listOf(
                store.copy(
                    googlePlaceId = hit.placeId,
                    latitude = hit.geometry?.location?.lat ?: store.latitude,
                    longitude = hit.geometry?.location?.lng ?: store.longitude,
                    address = hit.formattedAddress ?: store.address
                )
            )
        )
        hit.placeId
    }

    override suspend fun getReviews(placeId: String): List<Review> =
        withContext(Dispatchers.IO) {
            val fields = "place_id,name,rating,user_ratings_total,reviews,geometry,formatted_address,opening_hours"
            val details = api.placeDetails(placeId, fields, apiKey).result ?: return@withContext emptyList()
            details.reviews.orEmpty().mapIndexed { idx, r ->
                Review(
                    id = "g_${placeId}_$idx",
                    storeId = placeId, // caller remaps to local storeId
                    author = r.authorName ?: "Google user",
                    rating = r.rating ?: 0,
                    comment = r.text.orEmpty(),
                    createdAt = (r.unixTime ?: 0L) * 1000
                )
            }
        }

    interface PlacesApi {
        @GET("place/textsearch/json")
        suspend fun textSearch(
            @Query("query") query: String,
            @Query("key") apiKey: String
        ): TextSearchResponse

        @GET("place/details/json")
        suspend fun placeDetails(
            @Query("place_id") placeId: String,
            @Query("fields") fields: String,
            @Query("key") apiKey: String
        ): PlaceDetailsResponse
    }

    data class TextSearchResponse(val results: List<TextSearchResult> = emptyList(), val status: String)
    data class TextSearchResult(
        @Json(name = "place_id") val placeId: String,
        val name: String?,
        val geometry: Geometry?,
        @Json(name = "formatted_address") val formattedAddress: String?
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

    companion object {
        fun create(apiKey: String, db: AppDatabase) = GooglePlacesReviewsProvider(apiKey, db)
    }
}
