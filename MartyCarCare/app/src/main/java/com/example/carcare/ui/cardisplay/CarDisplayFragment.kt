package com.cse3mad.carcare.ui.cardisplay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cse3mad.carcare.R
import com.cse3mad.carcare.databinding.FragmentCarDisplayBinding

class CarDisplayFragment : Fragment() {
    private var _binding: FragmentCarDisplayBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarDisplayBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupClickListeners()
        updateCarInfo()

        return root
    }

    private fun setupClickListeners() {
        // Add Reminder button click
        binding.addReminderButton.setOnClickListener {
            findNavController().navigate(R.id.action_carDisplayFragment_to_addOilChangeFragment)
        }

        // Delete Profile button click
        binding.deleteProfileButton.setOnClickListener {
            // Handle delete profile
            findNavController().navigate(R.id.action_carDisplayFragment_to_carDetailsFormFragment)
        }
    }

    private fun updateCarInfo() {
        arguments?.let { bundle ->
            val make = bundle.getString("make", "").trim()
            val model = bundle.getString("model", "").trim()
            val year = bundle.getString("year", "")

            // Update car name
            binding.carNameTextView.text = "$year $make $model"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 