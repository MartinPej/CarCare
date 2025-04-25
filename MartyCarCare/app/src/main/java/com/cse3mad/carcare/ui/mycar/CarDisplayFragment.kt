package com.cse3mad.carcare.ui.mycar

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.cse3mad.carcare.R
import com.cse3mad.carcare.databinding.FragmentCarDisplayBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CarDisplayFragment : Fragment() {
    private var _binding: FragmentCarDisplayBinding? = null
    private val binding get() = _binding!!
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarDisplayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            val make = bundle.getString("make", "").trim()
            val model = bundle.getString("model", "").trim()
            val year = bundle.getString("year", "")

            // Display car name with year
            binding.carNameText.text = "$year $make $model"

            // Create path using make and model as entered
            val basePath = "cars/$make $model"
            val jpgPath = "$basePath.jpg"
            val pngPath = "$basePath.png"
            
            // Create Glide options for consistent image display
            val options = RequestOptions()
                .transform(CenterCrop(), RoundedCorners(16)) // Center crop and rounded corners
                .override(800, 600) // Set a consistent size for all images
            
            // Try .jpg first
            val jpgRef: StorageReference = storage.reference.child(jpgPath)
            jpgRef.downloadUrl.addOnSuccessListener { uri: Uri ->
                Glide.with(this)
                    .load(uri.toString())
                    .apply(options)
                    .into(binding.carImage)
            }.addOnFailureListener { exception: Exception ->
                // If .jpg fails, try .png
                val pngRef: StorageReference = storage.reference.child(pngPath)
                pngRef.downloadUrl.addOnSuccessListener { uri: Uri ->
                    Glide.with(this)
                        .load(uri.toString())
                        .apply(options)
                        .into(binding.carImage)
                }.addOnFailureListener { exception: Exception ->
                    // If both fail, show error and go back to form
                    Toast.makeText(context, "Error: Incorrect Car Make/Model", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 