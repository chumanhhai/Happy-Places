package com.example.happyplaces.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.happyplaces.R
import com.example.happyplaces.model.Place
import java.util.*


class DetailPlaceActivity : AppCompatActivity() {

    var tbDetailActivity: Toolbar? = null
    var ivDetailImage: ImageView? = null
    var tvDetailDescription: TextView? = null
    var tvDetailLocation: TextView? = null
    var btnSeeOnMap: Button? = null

    var place: Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_place)

        // set tool bar
        tbDetailActivity = findViewById(R.id.tb_detail_place)
        setSupportActionBar(tbDetailActivity)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle("Detail")
        tbDetailActivity!!.setNavigationOnClickListener {
            onBackPressed()
        }

        // get view
        ivDetailImage = findViewById(R.id.iv_detail_image)
        tvDetailDescription = findViewById(R.id.tv_detail_description)
        tvDetailLocation = findViewById(R.id.tv_detail_location)
        btnSeeOnMap = findViewById(R.id.btn_see_on_the_map)

        // get place
        place = intent.getParcelableExtra(MainActivity.EXTRA_PLACE)
        ivDetailImage!!.setImageURI(Uri.parse(place!!.image))
        tvDetailLocation!!.text = place!!.location
        tvDetailDescription!!.text = place!!.description

        // set on click
        btnSeeOnMap!!.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_PLACE, place)
            startActivity(intent)
        }
    }
}