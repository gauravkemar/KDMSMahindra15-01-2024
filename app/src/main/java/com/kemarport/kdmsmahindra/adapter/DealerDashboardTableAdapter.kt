package com.kemarport.kdmsmahindra.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kemarport.kdmsmahindra.databinding.TableItemBinding
import com.kemarport.kdmsmahindra.model.newapi.DashboardGetDeliveredDetailsResponse
import java.text.SimpleDateFormat
import java.util.Locale

class DealerDashboardTableAdapter(val context: Context, val items: ArrayList<DashboardGetDeliveredDetailsResponse>) :
    RecyclerView.Adapter<DealerDashboardTableAdapter.ItemViewHolder>() {
    inner class ItemViewHolder(private val itemViewBinding: TableItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        fun bind(item: DashboardGetDeliveredDetailsResponse) {
            itemViewBinding.tvColumnOne.text = "${item.vin}"
            itemViewBinding.tvColumnTwo.text = "${item.modelCode}/${item.colorDescription}"
            itemViewBinding.tvColumnThree.text = "${convertDateFormat(item.deliveredAt)}"
            if (layoutPosition == items.size - 1) {
                itemViewBinding.rowDivider.visibility = View.GONE
            }
        }
    }

    fun convertDateFormat(inputDateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())

        // Parse the input date string
        val date = inputFormat.parse(inputDateString)

        // Format the date in the desired output format
        return outputFormat.format(date)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(TableItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun updateItems(list: ArrayList<DashboardGetDeliveredDetailsResponse>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

}