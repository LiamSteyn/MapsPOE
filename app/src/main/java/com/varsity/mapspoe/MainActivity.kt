package com.varsity.mapspoe

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.varsity.mapspoe.data.DataRepository
import com.varsity.mapspoe.data.local.dao.entity.AppDatabase
import com.varsity.mapspoe.data.remote.GooglePlacesReviewsProvider
import com.varsity.mapspoe.ui.theme.MapsPOETheme

class MainActivity : ComponentActivity() {

    companion object { private const val TAG = "MainActivity" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize local DB + services
        DataRepository.init(applicationContext)

        val provider = GooglePlacesReviewsProvider.create(
            apiKey = getString(R.string.places_api_key),
            db = AppDatabase.get(applicationContext)
        )
        DataRepository.enableGooglePlaces(provider, enable = true)


        setContent {
            MapsPOETheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(text = "Welcome to Marley Maps", color = Color.White)
                    }
                }
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start LoginActivity", e)
            }
        }, 3000)
    }
}
