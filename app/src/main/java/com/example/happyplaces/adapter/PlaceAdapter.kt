package com.example.happyplaces.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.database.DBHelpler
import com.example.happyplaces.model.Place
import com.example.happyplaces.utils.Utils
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.net.URI

class PlaceAdapter(val context: Context, val itemsList: ArrayList<Place>) : RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    var placeItemOnClickListener: PlaceItemOnClickListener? = null
    val dbHelper = DBHelpler(context)

    interface PlaceItemOnClickListener {
        fun placeItemOnClick(place: Place);
        fun btnEditOnClick(position: Int);
        fun btnDeleteOnClick(position: Int);
    }

    init {
        placeItemOnClickListener = context as PlaceItemOnClickListener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val civImage = itemView.findViewById<CircleImageView>(R.id.civ_image)
        val tvTitle = itemView.findViewById<TextView>(R.id.tv_title)
        val tvDescription = itemView.findViewById<TextView>(R.id.tv_description)
        val ibEdit = itemView.findViewById<ImageButton>(R.id.ib_edit)
        val ibDelete = itemView.findViewById<ImageButton>(R.id.ib_delete)
        val cvPlace = itemView.findViewById<CardView>(R.id.cv_place)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_place, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = itemsList.get(position)
        if(File(URI.create(place.image)).exists()) {
            holder.civImage.setImageURI(Uri.parse(place.image))
        } else {
            holder.civImage.setImageResource(R.drawable.img_null_image)
        }
        holder.tvTitle.text = place.title
        holder.tvDescription.text = place.description

        holder.cvPlace.setOnClickListener {
            placeItemOnClickListener!!.placeItemOnClick(place)
        }
        holder.ibEdit.setOnClickListener {
            placeItemOnClickListener!!.btnEditOnClick(getPosition(place._id!!))
        }
        holder.ibDelete.setOnClickListener {
            placeItemOnClickListener!!.btnDeleteOnClick(getPosition(place._id!!))
        }
    }

    fun getPosition(_id: Int): Int {
        for((idx, value) in itemsList.withIndex())
            if(_id == value._id)
                return idx
        return -1
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    fun removeItem(position: Int) {
        // remove image
        Utils.removeImageFromUri(itemsList.get(position).image!!)
        // remove place in database
        dbHelper.removePlace(itemsList.get(position)._id!!)
        // remove place in list
        itemsList.removeAt(position)
        // remove viewholder in adapter
        notifyItemRemoved(position)
    }

    fun updateItem(position: Int, place: Place) {
        // update in database
        dbHelper.updatePlace(place)
        // change in list
        itemsList.set(position, place)
        // change in viewholder
        notifyItemChanged(position)
    }

    fun insertItem(place: Place) {
        // add in database
        place._id = dbHelper.addPlace(place)
        // add in list
        itemsList.add(0, place)
        // add viewholder
        notifyItemInserted(0)
    }
}