package com.example.happyplaces.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.happyplaces.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    var tbMapActivity: Toolbar? = null
    var place: com.example.happyplaces.model.Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // set toolbar
        tbMapActivity = findViewById(R.id.tb_map_activity)
        setSupportActionBar(tbMapActivity)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle("Map")
        tbMapActivity!!.setNavigationOnClickListener {
            onBackPressed()
        }

        // get intent
        place = intent.getParcelableExtra(MainActivity.EXTRA_PLACE)

        // get support map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap?) {
        // get position
        val position = LatLng(place!!.latitude, place!!.longitude)
        // set position
        p0!!.addMarker(MarkerOptions().position(position).title(place!!.location))
        p0!!.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10f))
    }
}