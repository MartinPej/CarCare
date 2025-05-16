package com.cse3mad.carcare.ui.ratings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cse3mad.carcare.R
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class MechanicRatingAdapter(
    private val onEditClick: (MechanicRating) -> Unit,
    private val onDeleteClick: (MechanicRating) -> Unit
) : ListAdapter<MechanicRating, MechanicRatingAdapter.RatingViewHolder>(RatingDiffCallback()) {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mechanic_rating, parent, false)
        return RatingViewHolder(view, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: RatingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RatingViewHolder(
        itemView: View,
        private val onEditClick: (MechanicRating) -> Unit,
        private val onDeleteClick: (MechanicRating) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val mechanicNameText: TextView = itemView.findViewById(R.id.mechanicNameText)
        private val locationText: TextView = itemView.findViewById(R.id.locationText)
        private val dateVisitedText: TextView = itemView.findViewById(R.id.dateVisitedText)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val commentText: TextView = itemView.findViewById(R.id.commentText)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        fun bind(rating: MechanicRating) {
            mechanicNameText.text = rating.mechanicName
            locationText.text = "${rating.suburb}, ${rating.city}, ${rating.country}"
            dateVisitedText.text = dateFormat.format(rating.dateVisited)
            ratingBar.rating = rating.rating
            commentText.text = rating.comment

            // Only show edit and delete buttons if the rating belongs to the current user
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val isOwner = currentUserId == rating.userId

            editButton.visibility = if (isOwner) View.VISIBLE else View.GONE
            deleteButton.visibility = if (isOwner) View.VISIBLE else View.GONE

            if (isOwner) {
                editButton.setOnClickListener { onEditClick(rating) }
                deleteButton.setOnClickListener { onDeleteClick(rating) }
            }
        }
    }

    private class RatingDiffCallback : DiffUtil.ItemCallback<MechanicRating>() {
        override fun areItemsTheSame(oldItem: MechanicRating, newItem: MechanicRating): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MechanicRating, newItem: MechanicRating): Boolean {
            return oldItem == newItem
        }
    }
} 