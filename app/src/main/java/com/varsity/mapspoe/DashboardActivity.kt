package com.varsity.mapspoe

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.varsity.mapspoe.data.DataRepository
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    // parallel lists so we can navigate by ID
    private val rowTexts = mutableListOf<String>()
    private val rowIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        findViewById<android.widget.Button>(R.id.btn_open_map)?.setOnClickListener {
            startActivity(android.content.Intent(this, MapsActivity::class.java))
        }

        // Profile button (unchanged)
        val profileButton = findViewById<Button>(R.id.profileButton)
        val userEmail = intent.getStringExtra("userEmail") ?: ""
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userEmail", userEmail)
            startActivity(intent)
        }

        // List + adapter
        val listView = findViewById<ListView>(R.id.dispensaryList)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            rowTexts
        )
        listView.adapter = adapter

        // Collect Room-backed stores from DataRepository
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                DataRepository.observeStores().collect { stores ->
                    rowTexts.clear()
                    rowIds.clear()

                    if (stores.isEmpty()) {
                        rowTexts += "No dispensaries yet. (Seed may still be loading)"
                    } else {
                        for (s in stores) {
                            val stars = "⭐" + String.format("%.1f", s.ratingAvg)
                            rowTexts += "${s.name}  $stars"
                            rowIds += s.id
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }

        // Navigate to details using the parallel ID list
        listView.setOnItemClickListener { _, _, position, _ ->
            // Guard against the empty-state row
            if (position in rowIds.indices) {
                val storeId = rowIds[position]
                val intent = Intent(this, DispensaryDetailsActivity::class.java)
                intent.putExtra("dispensaryId", storeId)
                startActivity(intent)
            }
        }
    }
}
