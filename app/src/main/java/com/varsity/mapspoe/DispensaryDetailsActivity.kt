package com.varsity.mapspoe

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.varsity.mapspoe.data.DataRepository
import com.varsity.mapspoe.data.PlacesClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DispensaryDetailsActivity : Activity() {

    private val TAG = "DispensaryDetails"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dispensary_details)

        // UI refs (match your XML ids)
        val nameTv = findViewById<TextView>(R.id.nameText)
        val addrTv = findViewById<TextView>(R.id.addressText)
        val ratingTv = findViewById<TextView>(R.id.ratingText)
        val statusTv = findViewById<TextView>(R.id.statusText)
        val directionsBtn = findViewById<Button>(R.id.getDirectionsButton)
        val callBtn = findViewById<Button>(R.id.callButton)
        val image = findViewById<ImageView>(R.id.dispensaryImage)
        val reviewAuthorTv = findViewById<TextView>(R.id.reviewAuthor)
        val reviewRatingTv = findViewById<TextView>(R.id.reviewRating)
        val reviewBodyTv = findViewById<TextView>(R.id.reviewBody)

        // Inputs from Dashboard
        val localId = intent.getIntExtra("dispensaryId", -1)
        val placeId = intent.getStringExtra("placeId")

        val dispensary = DataRepository.getDispensaries().find { it.id == localId }
        if (dispensary == null) {
            Toast.makeText(this, "Dispensary not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Populate from local model immediately (fast paint)
        nameTv.text = dispensary.name
        addrTv.text = dispensary.address
        ratingTv.text = "⭐ ${dispensary.rating}"
        statusTv.text = if (dispensary.isOpen) getString(R.string.open_now) else getString(R.string.closed)

        // Variables that may get filled from Places
        var lat: Double? = null
        var lng: Double? = null
        var phone: String? = null

        // Actions: navigation + dial
        directionsBtn.setOnClickListener {
            val uri = if (lat != null && lng != null) {
                Uri.parse("google.navigation:q=$lat,$lng&mode=d")
            } else {
                // fallback: geocode by name+address
                Uri.parse("geo:0,0?q=${Uri.encode("${dispensary.name} ${dispensary.address}")}")
            }
            startActivity(Intent(Intent.ACTION_VIEW, uri).apply {
                `package` = "com.google.android.apps.maps"
            })
        }

        callBtn.setOnClickListener {
            phone?.let { p -> startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$p"))) }
                ?: Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show()
        }

        // If we don't have a Google Place ID, we're done (local-only view).
        if (placeId.isNullOrBlank()) return

        // Fetch live details from Places (background thread)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val details = PlacesClient.getPlaceDetails("places/$placeId")
                val top = details.reviews?.firstOrNull()

                lat = details.location?.lat
                lng = details.location?.lng
                phone = details.nationalPhoneNumber ?: details.internationalPhoneNumber

                withContext(Dispatchers.Main) {
                    // Update rating if Google has one
                    details.rating?.let { ratingTv.text = "⭐ $it" }

                    // Latest review card (author, rating, body)
                    if (top != null) {
                        reviewAuthorTv.text = top.author?.displayName ?: "Anon"
                        reviewRatingTv.text = top.rating?.let { r -> "⭐ $r" } ?: ""
                        reviewBodyTv.text = top.text?.text ?: ""
                    }

                    // Photo/logo (optional). Requires Glide in deps to actually load.
                    val photoName = details.photos?.firstOrNull()?.name
                    if (!photoName.isNullOrBlank()) {
                        val url = PlacesClient.buildPhotoUrl(photoName, maxWidth = 700)
                        try {
                            // If Glide is added: implementation("com.github.bumptech.glide:glide:4.16.0")
                            // and annotationProcessor kapt not required in Kotlin-only module.
                            com.bumptech.glide.Glide.with(this@DispensaryDetailsActivity)
                                .load(url)
                                .into(image)
                        } catch (_: Throwable) {
                            // If Glide isn't available, just ignore; UI still works.
                        }
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Places details fetch failed", t)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DispensaryDetailsActivity,
                        "Could not load live details",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}