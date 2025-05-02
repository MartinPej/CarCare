package com.cse3mad.carcare.ui.mechanic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cse3mad.carcare.R
import com.google.android.libraries.places.api.model.Place
import kotlin.math.roundToInt

class MechanicListAdapter(private val onItemClick: (MechanicInfo) -> Unit) :
    ListAdapter<MechanicListAdapter.Item, RecyclerView.ViewHolder>(DiffCallback()) {

    // Add items to the adapter with header
    fun submitMechanicList(mechanics: List<MechanicInfo>) {
        val items = listOf(Item.Header) + mechanics.map { Item.MechanicItem(it) }
        submitList(items)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Item.Header -> VIEW_TYPE_HEADER
            is Item.MechanicItem -> VIEW_TYPE_MECHANIC
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_mechanic_header, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_MECHANIC -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_mechanic, parent, false)
                MechanicViewHolder(view, onItemClick)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Item.Header -> (holder as HeaderViewHolder).bind()
            is Item.MechanicItem -> (holder as MechanicViewHolder).bind(item.mechanicInfo)
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            // No binding needed for header as it's static
        }
    }

    class MechanicViewHolder(
        itemView: View,
        private val onItemClick: (MechanicInfo) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.mechanic_name)
        private val addressTextView: TextView = itemView.findViewById(R.id.mechanic_address)
        private val distanceTextView: TextView = itemView.findViewById(R.id.mechanic_distance)
        private val ratingTextView: TextView = itemView.findViewById(R.id.mechanic_rating)
        private val ratingContainer: View = itemView.findViewById(R.id.rating_container)

        fun bind(mechanicInfo: MechanicInfo) {
            val place = mechanicInfo.place
            nameTextView.text = place.name
            addressTextView.text = place.address
            
            // Format distance
            val distanceKm = mechanicInfo.distance / 1000.0
            distanceTextView.text = String.format("%.1f km away", distanceKm)

            // Format rating
            place.rating?.let { rating ->
                ratingTextView.text = String.format("%.1f (%d)", rating, place.userRatingsTotal ?: 0)
                ratingContainer.visibility = View.VISIBLE
            } ?: run {
                ratingContainer.visibility = View.GONE
            }

            itemView.setOnClickListener { onItemClick(mechanicInfo) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return when {
                oldItem is Item.Header && newItem is Item.Header -> true
                oldItem is Item.MechanicItem && newItem is Item.MechanicItem ->
                    oldItem.mechanicInfo.place.id == newItem.mechanicInfo.place.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }

    sealed class Item {
        object Header : Item()
        data class MechanicItem(val mechanicInfo: MechanicInfo) : Item()
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_MECHANIC = 1
    }
} 