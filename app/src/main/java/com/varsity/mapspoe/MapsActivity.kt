// app/src/main/java/com/varsity/mapspoe/MapsActivity.kt
package com.varsity.mapspoe

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.varsity.mapspoe.data.DataRepository
import com.varsity.mapspoe.data.local.dao.entity.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Make sure repo/DB are ready
        DataRepository.init(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync { gMap ->
            // Open Reviews when tapping the info window
            gMap.setOnInfoWindowClickListener { marker ->
                val name = marker.title ?: return@setOnInfoWindowClickListener
                lifecycleScope.launch {
                    val dao = AppDatabase.get(this@MapsActivity).storeDao()
                    val row = withContext(Dispatchers.IO) { dao.getByName(name) }
                    row?.let {
                        val intent = Intent(this@MapsActivity, ReviewsActivity::class.java)
                            .putExtra("storeId", it.id)
                        startActivity(intent)
                    }
                }
            }

            // Load markers from Room (StoreEntity has latitude/longitude)
            lifecycleScope.launch {
                val dao = AppDatabase.get(this@MapsActivity).storeDao()
                val stores = withContext(Dispatchers.IO) {
                    // one-time snapshot is fine for map plotting
                    dao.getStoresOnce()
                }

                gMap.clear()
                val bounds = LatLngBounds.Builder()
                var count = 0

                stores.forEach { s ->
                    val lat = s.latitude
                    val lng = s.longitude
                    if (lat != null && lng != null) {
                        val pos = LatLng(lat, lng)
                        gMap.addMarker(MarkerOptions().position(pos).title(s.name))
                        bounds.include(pos)
                        count++
                    }
                }

                if (count > 0) {
                    gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
                } else {
                    // Fallback: Cape Town CBD
                    gMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(-33.9249, 18.4241), 12f)
                    )
                }
            }
        }
    }
}
