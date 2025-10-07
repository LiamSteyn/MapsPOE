package com.varsity.mapspoe

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.varsity.mapspoe.data.DataRepository

class DashboardActivity : Activity() {

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Profile button → ProfileActivity
        val profileButton = findViewById<Button>(R.id.profileButton)
        val userEmail = intent.getStringExtra("userEmail") ?: ""
        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java).apply {
                putExtra("userEmail", userEmail)
            })
        }

        // Source data
        val dispensaries = DataRepository.getDispensaries().toMutableList()

        // ListView + simple adapter
        listView = findViewById(R.id.dispensaryList)
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            dispensaries.map { "${it.name} (${it.distance}) ⭐${it.rating}" }
        )
        listView.adapter = adapter

        // Place IDs (edit/replace with your real matches)
        val placeIdByDispensaryId = mapOf(
            1 to "ChIJdy6oxx5dzB0RiWyZzXOX_mg",
            2 to "ChIJD5HmgdpDzB0RIzdRt-2HQK0",
            3 to "ChIJrZQ-85JnzB0RS18yWDkYCGw"
        )

        // Sort controls – get refs BEFORE defining helpers that use them
        val chipGroup = findViewById<ChipGroup>(R.id.sortChipGroup)
        val chipRating = findViewById<Chip>(R.id.chipSortRating)
        // chipSortDistance exists and is checked by default in XML

        // ---- helpers (must be above first usage)
        fun parseKm(distance: String): Double =
            distance.lowercase().replace("km", "").trim().toDoubleOrNull() ?: Double.MAX_VALUE

        fun currentSorted(list: List<com.varsity.mapspoe.data.Dispensary>) =
            if (chipRating.isChecked) list.sortedByDescending { it.rating }
            else list.sortedBy { parseKm(it.distance) }

        val refresh: () -> Unit = {
            val sorted = currentSorted(dispensaries)
            adapter.clear()
            adapter.addAll(sorted.map { "${it.name} (${it.distance}) ⭐${it.rating}" })
            adapter.notifyDataSetChanged()
        }
        // -------------------------------

        chipGroup.setOnCheckedStateChangeListener { _, _ -> refresh() }
        refresh() // initial fill

        // Row click → details
        listView.setOnItemClickListener { _, _, position, _ ->
            val d = currentSorted(dispensaries).getOrNull(position) ?: return@setOnItemClickListener
            startActivity(Intent(this, DispensaryDetailsActivity::class.java).apply {
                putExtra("dispensaryId", d.id)
                placeIdByDispensaryId[d.id]?.let { putExtra("placeId", it) }
            })
        }
    }
}