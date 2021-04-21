package com.example.happyplaces.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.Criteria
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.happyplaces.R
import com.example.happyplaces.model.Place
import com.example.happyplaces.utils.GetLocationFromLatLngAsync
import com.example.happyplaces.utils.Utils
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class AddPlacesActivity : AppCompatActivity(), View.OnClickListener, GetLocationFromLatLngAsync.SetLocationCallBack {

    var tabAddActivity: androidx.appcompat.widget.Toolbar? = null
    var etDate: EditText? = null
    var tvAddImage: TextView? = null
    var ivAddImage: ImageView? = null
    var etTitle: EditText? = null
    var etDescription: EditText? = null
    var etLocation: EditText? = null
    var btnSave: Button? = null
    var tvCurrentLocation: TextView? = null

    var calendar = Calendar.getInstance()
    var latitude = 0.0
    var longitude = 0.0
    var bitmap: Bitmap? = null
    var placeEdited: Place? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null

    companion object {
        val CAMERA_REQUEST_CODE = 0
        val GALLERY_REQUEST_CODE = 1
        val GOOGLE_PLACE_REQUEST_CODE = 2
    }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_places)

        // set action bar
        tabAddActivity = findViewById(R.id.tb_add_activity)
        setSupportActionBar(tabAddActivity)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle("Add Places")
        tabAddActivity!!.setNavigationOnClickListener {
            onBackPressed()
        }

        // get view
        etDate = findViewById(R.id.et_date)
        tvAddImage = findViewById(R.id.tv_add_image)
        ivAddImage = findViewById(R.id.iv_add_image)
        etTitle = findViewById(R.id.et_title)
        etDescription = findViewById(R.id.et_description)
        etLocation = findViewById(R.id.et_location)
        btnSave = findViewById(R.id.btn_save)
        tvCurrentLocation = findViewById(R.id.tv_current_location)

        // init prop
        etDate!!.setText(
            SimpleDateFormat(
                "dd.MM.yy",
                Locale.getDefault()
            ).format(System.currentTimeMillis())
        )
        if(intent.hasExtra(MainActivity.EXTRA_PLACE)) {
            placeEdited = intent.getParcelableExtra(MainActivity.EXTRA_PLACE)
            etTitle!!.setText(placeEdited!!.title)
            etDescription!!.setText(placeEdited!!.description)
            etLocation!!.setText(placeEdited!!.location)
            etDate!!.setText(placeEdited!!.date)
            ivAddImage!!.setImageURI(Uri.parse(placeEdited!!.image))

            val src = ImageDecoder.createSource(contentResolver, Uri.parse(placeEdited!!.image))
            bitmap = ImageDecoder.decodeBitmap(src)
        }
        if(!Places.isInitialized()) {
            Places.initialize(this, resources.getString(R.string.google_map_api_key))
        }
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        // set on click
        etDate!!.setOnClickListener(this)
        tvAddImage!!.setOnClickListener(this)
        btnSave!!.setOnClickListener(this)
        etLocation!!.setOnClickListener(this)
        tvCurrentLocation!!.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.et_date -> {
                val year = calendar.get(Calendar.YEAR)
                var month = calendar.get(Calendar.MONTH)
                var dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                DatePickerDialog(
                    this,
                    DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                        calendar.set(Calendar.YEAR, year)
                        calendar.set(Calendar.MONTH, month)
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        etDate!!.setText(
                            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(
                                calendar.time
                            )
                        )
                    },
                    year, month, dayOfMonth
                ).show()
            }
            R.id.tv_add_image -> {
                AlertDialog.Builder(this)
                    .setTitle("Select Action")
                    .setItems(
                        arrayOf("Choose from gallery", "Take a picture"),
                        DialogInterface.OnClickListener { dialog, which ->
                            when (which) {
                                0 -> {
                                    choosePhotoFromGallery()
                                }
                                1 -> {
                                    takeAPictureByCamera()
                                }

                            }
                        }).show()
            }
            R.id.btn_save -> {
                val title = etTitle!!.text.toString()
                val description = etDescription!!.text.toString()
                val location = etLocation!!.text.toString()
                val date = etDate!!.text.toString()

                when {
                    title.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please fill in Title", Toast.LENGTH_SHORT).show()
                    }
                    description.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please fill in Description", Toast.LENGTH_SHORT)
                            .show()
                    }
                    location.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please fill in Location", Toast.LENGTH_SHORT).show()
                    }
                    bitmap == null -> {
                        Toast.makeText(this, "Please select an Image", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val intent = Intent()
                        val savedImageUri = saveImageToInternalStorage(bitmap!!)
                        val place = Place(
                            null,
                            title,
                            description,
                            savedImageUri.toString(),
                            date,
                            location,
                            latitude,
                            longitude
                        )
                        if (placeEdited != null) {
                            // delete old image
                            Utils.removeImageFromUri(placeEdited!!.image!!)

                            place._id = placeEdited!!._id
                            intent.putExtra(MainActivity.EXTRA_PLACE, place)
                            setResult(Activity.RESULT_OK, intent)
                        } else {
                            intent.putExtra(MainActivity.EXTRA_PLACE, place)
                            setResult(Activity.RESULT_OK, intent)
                        }
                        finish()
                    }
                }
            }
            R.id.et_location -> {
                // Set the fields to specify which types of place data to
                // return after the user has made a selection.
                val fields = listOf(com.google.android.libraries.places.api.model.Place.Field.ID,
                    com.google.android.libraries.places.api.model.Place.Field.NAME,
                    com.google.android.libraries.places.api.model.Place.Field.ADDRESS,
                    com.google.android.libraries.places.api.model.Place.Field.LAT_LNG)

                // Start the autocomplete intent.
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this)
                startActivityForResult(intent, GOOGLE_PLACE_REQUEST_CODE)

            }
            R.id.tv_current_location -> {
                if(!isLocationenabled()) {
                    AlertDialog.Builder(this)
                            .setMessage("Please turn a GPS on.")
                            .setPositiveButton("Go to Settings", DialogInterface.OnClickListener { dialog, which ->
                                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                startActivity(intent)
                            })
                            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                                dialog.dismiss()
                            })
                            .show()
                } else {
                    Dexter.withContext(this)
                            .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            .withListener(object : MultiplePermissionsListener {
                                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                                    if(p0!!.areAllPermissionsGranted()) {
                                        requestNewLocation()
                                        CoroutineScope(IO).launch {
                                            GetLocationFromLatLngAsync(this@AddPlacesActivity, latitude, longitude).Execute()
                                        }

                                    } else {
                                        showAlertDialogForPermission()
                                    }
                                }

                                override fun onPermissionRationaleShouldBeShown(p0: MutableList<PermissionRequest>?, p1: PermissionToken?) {
                                    p1!!.continuePermissionRequest()
                                }
                            }).onSameThread().check()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocation() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.numUpdates = 1

        fusedLocationProviderClient!!.requestLocationUpdates(locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(p0: LocationResult?) {
                        latitude = p0!!.lastLocation.latitude
                        longitude = p0!!.lastLocation.longitude
                    }
                },
                Looper.myLooper())

//        Log.i("latlng", "${latitude} ${longitude}")
    }

    private fun isLocationenabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        if (p0!!.areAllPermissionsGranted()) {
                            val intent = Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.INTERNAL_CONTENT_URI
                            )
                            startActivityForResult(intent, GALLERY_REQUEST_CODE)
                        } else {
                            showAlertDialogForPermission()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        p1!!.continuePermissionRequest()
                    }

                }).onSameThread().check()
    }

    private fun takeAPictureByCamera() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, CAMERA_REQUEST_CODE)
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        showAlertDialogForPermission()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: PermissionRequest?,
                        p1: PermissionToken?
                    ) {
                        p1!!.continuePermissionRequest()
                    }

                }).check()
    }

    private fun showAlertDialogForPermission() {
        AlertDialog.Builder(this@AddPlacesActivity)
                .setMessage("You have disabled permissions needed for this action!\nPlease enable those permission to continue!")
                .setPositiveButton(
                    "Go to setting",
                    DialogInterface.OnClickListener { dialog, which ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", packageName, null)
                        startActivity(intent)
                    })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                }).show()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == CAMERA_REQUEST_CODE) {
                bitmap = data!!.extras!!.get("data") as Bitmap
                ivAddImage!!.setImageBitmap(bitmap)
            }
            if(requestCode == GALLERY_REQUEST_CODE) {
                val imageURI = data!!.data
                val src = ImageDecoder.createSource(contentResolver, imageURI!!)
                bitmap = ImageDecoder.decodeBitmap(src)
                ivAddImage!!.setImageBitmap(bitmap)
            }
            if(requestCode == GOOGLE_PLACE_REQUEST_CODE) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                etLocation!!.setText(place.address)
                latitude = place.latLng!!.latitude
                longitude = place.latLng!!.longitude
            }
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap) : Uri {
//        val dir = ContextWrapper(applicationContext).getDir("happyPlaceImage", MODE_PRIVATE)
        val dir = File(externalCacheDir.toString() + separator + "happyPlaceImage")
        if(!dir.exists()) {
            dir.mkdir()
        }
        val file = File(dir, "${UUID.randomUUID()}.jpeg")
        try {
            val outStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            outStream.flush()
            outStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.fromFile(file)
    }

    override fun setLocation(location: String) {
        etLocation!!.setText(location)
    }
}