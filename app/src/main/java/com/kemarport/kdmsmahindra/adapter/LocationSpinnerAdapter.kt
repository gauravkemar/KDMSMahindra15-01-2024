package com.kemarport.kdmsmahindra.adapter

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.kemarport.kdmsmahindra.model.setgeofence.GetGeofenceByDealer

class LocationSpinnerAdapter(
    context: Context,
    private val items: List<GetGeofenceByDealer>
) : ArrayAdapter<GetGeofenceByDealer>(context, android.R.layout.simple_spinner_item, items) {

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    override fun isEnabled(position: Int): Boolean {
        // Disable the hint (first item)
        return position != 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        view.text = items[position].displayName

        // Make hint look gray
        if (position == 0) {
            view.setTextColor(Color.GRAY)
        } else {
            view.setTextColor(Color.BLACK)
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent) as TextView
        view.text = items[position].displayName

        // Make hint item gray in dropdown
        if (position == 0) {
            view.setTextColor(Color.GRAY)
        } else {
            view.setTextColor(Color.BLACK)
        }
        return view
    }
}
