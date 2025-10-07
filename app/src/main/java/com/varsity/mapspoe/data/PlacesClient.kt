package com.varsity.mapspoe.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.client.call.body
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object PlacesClient {
    // TODO: replace with your real key
    private const val PLACES_API_KEY = "AIzaSyB5Vaiy24Gv-M8Qy6fZSj_lwsVXBpwpRqA"
    private val http = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // Example: resource = "places/ChIJxxxx..."
    suspend fun getPlaceDetails(resource: String): PlaceDetails {
        val resp = http.get {
            url("https://places.googleapis.com/v1/$resource")
            header("X-Goog-Api-Key", PLACES_API_KEY)
            header(
                "X-Goog-FieldMask",
                "id,displayName,formattedAddress,location,rating,userRatingCount," +
                        "reviews,photos,nationalPhoneNumber,internationalPhoneNumber"
            )
            header("Accept", ContentType.Application.Json)
        }
        return resp.body()
    }

    fun buildPhotoUrl(photoName: String, maxWidth: Int = 800): String =
        "https://places.googleapis.com/v1/$photoName/media?maxWidth=$maxWidth&key=$PLACES_API_KEY"
}

@Serializable
data class PlaceDetails(
    val id: String? = null,
    val displayName: Text? = null,
    val formattedAddress: String? = null,
    val location: LatLngLiteral? = null,
    val rating: Double? = null,
    val userRatingCount: Int? = null,
    val reviews: List<Review>? = null,
    val photos: List<Photo>? = null,
    val nationalPhoneNumber: String? = null,
    val internationalPhoneNumber: String? = null
)

@Serializable data class Text(val text: String? = null)
@Serializable data class LatLngLiteral(val lat: Double? = null, val lng: Double? = null)
@Serializable data class Photo(val name: String? = null)

@Serializable
data class Review(
    val name: String? = null,
    val rating: Double? = null,
    val text: Text? = null,
    val author: AuthorAttribution? = null
)

@Serializable
data class AuthorAttribution(
    val displayName: String? = null,
    val uri: String? = null,
    @SerialName("photoUri") val photoUri: String? = null
)