package com.cse3mad.carcare.ui.ratings

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cse3mad.carcare.databinding.FragmentMechanicRatingsBinding
import com.cse3mad.carcare.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class MechanicRatingsFragment : Fragment() {
    private var _binding: FragmentMechanicRatingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var ratingAdapter: MechanicRatingAdapter
    private val ratings = mutableListOf<MechanicRating>()
    private val filteredRatings = mutableListOf<MechanicRating>()
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMechanicRatingsBinding.inflate(inflater, container, false)
        
        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        setupSearch()
        loadRatings()
    }

    private fun setupRecyclerView() {
        ratingAdapter = MechanicRatingAdapter(
            onEditClick = { rating -> showEditRatingDialog(rating) },
            onDeleteClick = { rating -> showDeleteConfirmationDialog(rating) }
        )
        binding.ratingsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ratingAdapter
        }
    }

    private fun setupFab() {
        binding.addRatingFab.setOnClickListener {
            showAddRatingDialog()
        }
    }

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterRatings(s.toString())
            }
        })
    }

    private fun loadRatings() {
        db.collection("mechanic_ratings")
            .orderBy("dateVisited", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Error loading ratings: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                ratings.clear()
                snapshot?.documents?.forEach { doc ->
                    val rating = doc.toObject(MechanicRating::class.java)
                    rating?.let {
                        ratings.add(it)
                    }
                }
                filterRatings(binding.searchInput.text.toString())
            }
    }

    private fun filterRatings(query: String) {
        filteredRatings.clear()
        if (query.isEmpty()) {
            filteredRatings.addAll(ratings)
        } else {
            filteredRatings.addAll(ratings.filter { 
                it.mechanicName.contains(query, ignoreCase = true) 
            })
        }
        ratingAdapter.submitList(filteredRatings.toList())
    }

    private fun showAddRatingDialog() {
        showRatingDialog(null)
    }

    private fun showEditRatingDialog(rating: MechanicRating) {
        showRatingDialog(rating)
    }

    private fun showRatingDialog(existingRating: MechanicRating?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_rating, null)
        val mechanicNameInput = dialogView.findViewById<TextInputEditText>(R.id.mechanicNameInput)
        val dateVisitedInput = dialogView.findViewById<TextInputEditText>(R.id.dateVisitedInput)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val commentInput = dialogView.findViewById<TextInputEditText>(R.id.commentInput)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        // If editing, populate fields
        existingRating?.let {
            mechanicNameInput.setText(it.mechanicName)
            dateVisitedInput.setText(formatDate(it.dateVisited))
            ratingBar.rating = it.rating
            commentInput.setText(it.comment)
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView as View)
            .create()

        dateVisitedInput.setOnClickListener {
            showDatePicker { date ->
                dateVisitedInput.setText(date)
            }
        }

        saveButton.setOnClickListener {
            val mechanicName = mechanicNameInput.text.toString()
            val dateVisited = dateVisitedInput.text.toString()
            val rating = ratingBar.rating
            val comment = commentInput.text.toString()

            if (mechanicName.isBlank() || dateVisited.isBlank() || rating == 0f) {
                Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newRating = MechanicRating(
                id = existingRating?.id ?: UUID.randomUUID().toString(),
                mechanicName = mechanicName,
                dateVisited = parseDate(dateVisited),
                rating = rating,
                comment = comment,
                userId = auth.currentUser?.uid ?: "anonymous"
            )

            // Save to Firestore
            db.collection("mechanic_ratings")
                .document(newRating.id)
                .set(newRating)
                .addOnSuccessListener {
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error saving rating: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(rating: MechanicRating) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Rating")
            .setMessage("Are you sure you want to delete this rating?")
            .setPositiveButton("Delete") { _, _ ->
                // Delete from Firestore
                db.collection("mechanic_ratings")
                    .document(rating.id)
                    .delete()
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error deleting rating: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                onDateSelected(date)
            },
            year,
            month,
            day
        ).show()
    }

    private fun parseDate(dateStr: String): Date {
        val format = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.parse(dateStr) ?: Date()
    }

    private fun formatDate(date: Date): String {
        val format = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(date)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 