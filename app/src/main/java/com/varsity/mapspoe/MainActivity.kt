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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.varsity.mapspoe.ui.theme.MapsPOETheme

//dbas passowrd EpIdFC1Zlx7hq2Rq

class MainActivity : ComponentActivity() {

    // Tag used for logging errors or debugging
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the Compose UI content for this activity
        setContent {
            // Apply the app's Material3 theme
            MapsPOETheme {
                // Surface acts as a container with background color
                Surface(
                    modifier = Modifier.fillMaxSize(), // Take up full screen
                    color = Color.Black // Set background to black
                ) {
                    // Box allows placing content with alignment
                    Box(
                        contentAlignment = Alignment.Center, // Center content both vertically & horizontally
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Display splash screen text
                        Text(
                            text = "Welcome to Marley Maps",
                            color = Color.White // White text for contrast on black background
                        )
                    }
                }
            }
        }

        // Create a 3-second delay before navigating to the Login screen
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                // Create intent to launch LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent) // Start LoginActivity
                finish() // Finish MainActivity so user can't go back to splash
            } catch (e: Exception) {
                // Log any errors if starting LoginActivity fails
                Log.e(TAG, "Failed to start LoginActivity", e)
            }
        }, 3000) // Delay time in milliseconds (3000ms = 3 seconds)
    }
}
