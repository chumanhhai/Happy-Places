package com.example.happyplaces.utils

import android.app.Dialog
import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.example.happyplaces.R
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*

class  GetLocationFromLatLngAsync(val context: Context, val latitude: Double, val longitude: Double) {

    var dialog: Dialog? = null
    var location = ""
    val geocoder = Geocoder(context, Locale.getDefault())

    interface SetLocationCallBack {
        fun setLocation(location: String)
    }

    suspend fun preExe() {
        withContext(Main) {
            dialog = Dialog(context)
            dialog!!.setContentView(R.layout.dialog_loaing)
            dialog!!.show()
        }
    }

    suspend fun postExe() {
        withContext(Main) {
            dialog!!.dismiss()
            val callback = context as SetLocationCallBack
            callback.setLocation(location)
        }

    }

    fun doingInBackgound() {
        try {

            var addressList = geocoder.getFromLocation(latitude, longitude, 1)

            if(!addressList.isNullOrEmpty()) {
                val address = addressList[0]
                val sb = StringBuffer()

                for(i in 0..address.maxAddressLineIndex)
                    sb.append(address.getAddressLine(i)).append(", ")
                sb.deleteCharAt(sb.length-1)
                sb.deleteCharAt(sb.length-1)
                location = sb.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun Execute() {
        preExe()
        doingInBackgound()
        postExe()
    }

}