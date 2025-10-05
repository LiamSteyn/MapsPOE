package com.varsity.mapspoe

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.varsity.mapspoe.data.DataRepository

class DispensaryDetailsActivity : Activity() {

    // Called when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the layout of this activity to activity_dispensary_details.xml
        setContentView(R.layout.activity_dispensary_details)

        // Retrieve the dispensary ID passed from the previous activity
        val id = intent.getIntExtra("dispensaryId", -1) // Default to -1 if not found

        // Get the list of dispensaries from the repository and find the one with matching ID
        val dispensary = DataRepository.getDispensaries().find { it.id == id }

        // If a matching dispensary is found, populate the UI with its details
        dispensary?.let {
            // Set the dispensary name
            findViewById<TextView>(R.id.nameText).text = it.name

            // Set the dispensary address
            findViewById<TextView>(R.id.addressText).text = it.address

            // Set the dispensary rating with a star emoji
            findViewById<TextView>(R.id.ratingText).text = "⭐ ${it.rating}"

            // Set the dispensary status text depending on whether it's open
            findViewById<TextView>(R.id.statusText).text =
                if (it.isOpen) "Open Now" else "Closed"
        }
    }
}
