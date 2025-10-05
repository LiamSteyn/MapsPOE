package com.varsity.mapspoe

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import com.varsity.mapspoe.data.DataRepository

class DashboardActivity : Activity() {

    // Called when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the layout of this activity to activity_dashboard.xml
        setContentView(R.layout.activity_dashboard)

        // Get the logged-in user email from intent
        val profileButton = findViewById<Button>(R.id.profileButton)
        val userEmail = intent.getStringExtra("userEmail") ?: "" // fallback to empty string

        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userEmail", userEmail)
            startActivity(intent)
        }



        // Find references to the ListView and Logout Button in the layout
        val listView = findViewById<ListView>(R.id.dispensaryList) // ListView showing dispensaries

        // Load dispensary data from DataRepository
        val dispensaries = DataRepository.getDispensaries()

        // Create an ArrayAdapter to display dispensaries in the ListView
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1, // Default Android list item layout
            dispensaries.map { "${it.name} (${it.distance}) ⭐${it.rating}" } // Display name, distance, and rating
        )
        listView.adapter = adapter // Attach adapter to the ListView

        // Handle item clicks on the ListView
        listView.setOnItemClickListener { _, _, position, _ ->
            // When a dispensary is clicked, open DispensaryDetailsActivity
            val intent = Intent(this, DispensaryDetailsActivity::class.java)
            // Pass the selected dispensary's ID to the details activity
            intent.putExtra("dispensaryId", dispensaries[position].id)
            startActivity(intent)
        }
    }
}
