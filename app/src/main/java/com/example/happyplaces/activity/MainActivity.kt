package com.example.happyplaces.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.adapter.PlaceAdapter
import com.example.happyplaces.database.DBHelpler
import com.example.happyplaces.model.Place
import com.example.happyplaces.utils.Utils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener, PlaceAdapter.PlaceItemOnClickListener {

    var fabAdd: FloatingActionButton? = null
    var rvPlace: RecyclerView? = null
    var tvNoPlaces: TextView? = null

    val dbHelper = DBHelpler(this)
    var adapter: PlaceAdapter? = null
    var places: ArrayList<Place>? = null
    var positionItemEdited = -1

    companion object {
        val EXTRA_PLACE = "extra_place"
        val ADD_PLACE_REQUEST_CODE = 0
        val EDIT_PLACE_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get view
        fabAdd = findViewById(R.id.fab_add)
        rvPlace = findViewById(R.id.rv_place)
        tvNoPlaces = findViewById(R.id.tv_no_places)

        // set on click
        fabAdd!!.setOnClickListener(this)

        // init
        initAfterOnCreate()
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.fab_add -> {
                val intent = Intent(this, AddPlacesActivity::class.java)
                startActivityForResult(intent, ADD_PLACE_REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                ADD_PLACE_REQUEST_CODE -> {
                    val place = data!!.getParcelableExtra<Place>(EXTRA_PLACE)
                    adapter!!.insertItem(place!!)

                    setPlacesVisibility()
                }
                EDIT_PLACE_REQUEST_CODE -> {
                    val place = data!!.getParcelableExtra<Place>(EXTRA_PLACE)
                    adapter!!.updateItem(positionItemEdited, place!!)
                }
            }
        }
    }

    private fun initAfterOnCreate() {
        places = dbHelper.getAllPlaces()
        Collections.reverse(places)
        rvPlace!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = PlaceAdapter(this, places!!)
        rvPlace!!.adapter = adapter
        setPlacesVisibility()
    }

    private fun setPlacesVisibility() {
        if(places!!.size > 0) {
            rvPlace!!.visibility = View.VISIBLE
            tvNoPlaces!!.visibility = View.GONE
        } else {
            rvPlace!!.visibility = View.GONE
            tvNoPlaces!!.visibility = View.VISIBLE
        }
    }

    override fun placeItemOnClick(place: Place) {
        val intent = Intent(this, DetailPlaceActivity::class.java)
        intent.putExtra(EXTRA_PLACE, place)
        startActivity(intent)
    }

    override fun btnEditOnClick(position: Int) {
        val intent = Intent(this, AddPlacesActivity::class.java)
        intent.putExtra(EXTRA_PLACE, places!!.get(position))
        startActivityForResult(intent, EDIT_PLACE_REQUEST_CODE)

        // store position of place
        positionItemEdited = position
    }

    override fun btnDeleteOnClick(position: Int) {
        adapter!!.removeItem(position)
        setPlacesVisibility()
    }
}