package com.cse3mad.carcare.ui.mycar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cse3mad.carcare.R
import com.cse3mad.carcare.databinding.FragmentCarDetailsFormBinding

class CarDetailsFormFragment : Fragment() {
    private var _binding: FragmentCarDetailsFormBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarDetailsFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.confirmButton.setOnClickListener {
            val make = binding.makeInput.text.toString().trim()
            val model = binding.modelInput.text.toString().trim()
            val year = binding.yearInput.text.toString().trim()

            if (make.isEmpty() || model.isEmpty() || year.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bundle = Bundle().apply {
                putString("make", make)
                putString("model", model)
                putString("year", year)
            }
            findNavController().navigate(R.id.action_carDetailsFormFragment_to_carDisplayFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 