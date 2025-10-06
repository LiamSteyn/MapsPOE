package com.varsity.mapspoe.data.remote

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.varsity.mapspoe.domain.Review
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Keep only ONE copy of this interface in your project. */
interface RealReviewsProvider {
    suspend fun getReviews(placeId: String): List<Review>
}

class GooglePlacesReviewsProvider(
    private val client: PlacesClient
) : RealReviewsProvider {

    override suspend fun getReviews(placeId: String): List<Review> = withContext(Dispatchers.IO) {
        try {
            val fields = listOf(
                Place.Field.ID,
                Place.Field.REVIEWS,
                Place.Field.RATING,
                Place.Field.USER_RATINGS_TOTAL
            )
            val req = FetchPlaceRequest.builder(placeId, fields).build()
            val resp = Tasks.await(client.fetchPlace(req))
            val place = resp.place
            val reviews = place.reviews ?: emptyList()

            reviews.map { r ->
                val author = r.authorAttribution?.name ?: "Google user"
                val rating = (r.rating ?: 0.0).toInt()
                val text   = r.text ?: ""
                val ts     = System.currentTimeMillis()

                Review(
                    id = "gp_${placeId}_${author.hashCode()}_$ts",
                    storeId = placeId,          // remap to your storeId in the repository before persisting
                    author = author,
                    rating = rating,
                    comment = text,
                    createdAt = ts              // SDK doesn't expose millis; use now
                )
            }
        } catch (t: Throwable) {
            Log.e("GPlacesReviews", "Fetch failed: ${t.message}", t)
            emptyList()
        }
    }

    companion object {
        /**
         * Places v3.4.0 requires an API key for initialize(...).
         * If apiKey is null/blank, we read it from AndroidManifest meta-data:
         *   <meta-data android:name="com.google.android.geo.API_KEY" android:value="..."/>
         */
        fun create(context: Context, apiKey: String? = null): GooglePlacesReviewsProvider {
            val key = apiKey?.takeIf { it.isNotBlank() } ?: readKeyFromManifest(context)
            if (!Places.isInitialized()) {
                Places.initialize(context.applicationContext, key)
            }
            return GooglePlacesReviewsProvider(Places.createClient(context))
        }

        private fun readKeyFromManifest(context: Context): String {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val key = appInfo.metaData?.getString("com.google.android.geo.API_KEY")
            require(!key.isNullOrBlank()) {
                "Missing Google API key. Add <meta-data android:name=\"com.google.android.geo.API_KEY\" android:value=\"YOUR_KEY\"/> to AndroidManifest."
            }
            return key!!
        }
    }
}
