package com.example.happyplaces.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.happyplaces.model.Place

class DBHelpler(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        val DB_NAME = "database"
        val DB_VERSION = 2
        val TABLE_PLACE = "place"
        val KEY_ID = "_id"
        val KEY_TITLE = "title"
        val KEY_DESCRIPTION = "description"
        val KEY_DATE = "date"
        val KEY_IMAGE = "image"
        val KEY_LOCATION = "localtion"
        val KEY_LATITUDE = "latitude"
        val KEY_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val sqlScript = "CREATE TABLE " + TABLE_PLACE + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," +
                KEY_TITLE + " TEXT NOT NULL," +
                KEY_DESCRIPTION + " TEXT NOT NULL," +
                KEY_IMAGE + " TEXT NOT NULL," +
                KEY_DATE + " TEXT NOT NULL," +
                KEY_LOCATION + " TEXT NOT NULL," +
                KEY_LATITUDE + " REAL NOT NULL, " +
                KEY_LONGITUDE + " READL NOT NULL)"
        db!!.execSQL(sqlScript)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACE)
        onCreate(db)
    }

    fun addPlace(place: Place): Int {
        var db = writableDatabase
        val cv = ContentValues()

        cv.put(KEY_TITLE, place.title)
        cv.put(KEY_DESCRIPTION, place.description)
        cv.put(KEY_IMAGE, place.image)
        cv.put(KEY_DATE, place.date)
        cv.put(KEY_LOCATION, place.location)
        cv.put(KEY_LATITUDE, place.latitude)
        cv.put(KEY_LONGITUDE, place.longitude)
        db.insert(TABLE_PLACE, null, cv)
        db.close()

        // get id
        db = readableDatabase
        val cursor = db.rawQuery("SELECT " + KEY_ID + " FROM " + TABLE_PLACE + " ORDER BY " + KEY_ID + " DESC LIMIT 1", null)
        cursor.moveToFirst()
        val _id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
        cursor.close()
        db.close()

        return _id
    }

    fun getAllPlaces(): ArrayList<Place> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM " + TABLE_PLACE, null)
        val itemsList = ArrayList<Place>()
        var id: Int
        var title: String
        var description: String
        var image: String
        var date: String
        var location: String
        var latitude: Double
        var longitude: Double

        cursor.moveToFirst()
        while(cursor.isAfterLast != true) {
            id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
            title = cursor.getString(cursor.getColumnIndex(KEY_TITLE))
            description = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION))
            image = cursor.getString(cursor.getColumnIndex(KEY_IMAGE))
            date = cursor.getString(cursor.getColumnIndex(KEY_DATE))
            location = cursor.getString(cursor.getColumnIndex(KEY_LOCATION))
            latitude = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE))
            longitude = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
            itemsList.add(Place(id, title, description, image, date, location, latitude, longitude))
            cursor.moveToNext()
        }
        cursor.close()
        db.close()
        return itemsList
    }

    fun removePlace(_id: Int): Int {
        val db = writableDatabase

        val result = db.delete(TABLE_PLACE, KEY_ID + " = " + _id, null)
        db.close()

        return result
    }

    fun updatePlace(place: Place): Int {
        val db = writableDatabase
        val cv = ContentValues()

        cv.put(KEY_TITLE, place.title)
        cv.put(KEY_DESCRIPTION, place.description)
        cv.put(KEY_IMAGE, place.image)
        cv.put(KEY_DATE, place.date)
        cv.put(KEY_LOCATION, place.location)
        cv.put(KEY_LATITUDE, place.latitude)
        cv.put(KEY_LONGITUDE, place.longitude)
        val result = db.update(TABLE_PLACE, cv, KEY_ID + " = " + place._id, null)
        db.close()

        return result
    }
}