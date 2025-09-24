package com.kemarport.kdmsmahindra.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.kemarport.kdmsmahindra.R
import com.kemarport.kdmsmahindra.model.newapi.DashboardGetDeliveredDetailsResponse
import java.text.SimpleDateFormat
import java.util.Locale

class DealerDashboardNew(
    private var context: Context,
    var items: ArrayList<DashboardGetDeliveredDetailsResponse>
) : RecyclerView.Adapter<DealerDashboardNew.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.new_dashboard_table_item, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val visitorMod: DashboardGetDeliveredDetailsResponse = items[position]

        // Null safe assignment with fallback "N/A"
        holder.tv_column_one.text = visitorMod.vin ?: "N/A"
        holder.tv_column_two.text =
            "${visitorMod.modelCode ?: "N/A"}/${visitorMod.colorDescription ?: "N/A"}"
        holder.tv_column_three.text =
            convertDateFormat(visitorMod.deliveredAt ?: "")

        if (position == items.size - 1) {
            holder.row_divider.visibility = View.GONE
        }
    }

    fun convertDateFormat(inputDateString: String): String {
        return try {
            if (inputDateString.isBlank()) return "N/A"

            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())

            val date = inputFormat.parse(inputDateString)
            if (date != null) {
                outputFormat.format(date)
            } else {
                "N/A"
            }
        } catch (e: Exception) {
            try {
                // fallback when microseconds not available
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
                val date = inputFormat.parse(inputDateString)
                if (date != null) outputFormat.format(date) else "N/A"
            } catch (ex: Exception) {
                "N/A"
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val tv_column_one: TextView = itemView.findViewById(R.id.tv_column_one)
        val tv_column_two: TextView = itemView.findViewById(R.id.tv_column_two)
        val tv_column_three: TextView = itemView.findViewById(R.id.tv_column_three)
        val row_divider: View = itemView.findViewById(R.id.row_divider)
    }
}
