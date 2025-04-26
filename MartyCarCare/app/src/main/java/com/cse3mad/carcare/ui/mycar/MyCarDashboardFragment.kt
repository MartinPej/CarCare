package com.cse3mad.carcare.ui.mycar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cse3mad.carcare.R
import com.cse3mad.carcare.databinding.FragmentMyCarDashboardBinding
import com.cse3mad.carcare.utils.CarPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class MyCarDashboardFragment : Fragment() {
    private var _binding: FragmentMyCarDashboardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyCarDashboardBinding.inflate(inflater, container, false)
        
        // Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Check for existing car details
        checkExistingCarDetails()
    }
    
    private fun checkExistingCarDetails() {
        val currentUser = auth.currentUser
        if (currentUser != null && !currentUser.isAnonymous) {
            // For logged in users, check Firestore
            db.collection("users")
                .document(currentUser.uid)
                .collection("cars")
                .document("primary")
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Get car details from Firestore
                        val make = document.getString("make") ?: ""
                        val model = document.getString("model") ?: ""
                        val year = document.getString("year") ?: ""
                        
                        // Also save to local storage
                        CarPreferences.saveCarDetails(make, model, year)
                        
                        navigateToCarDisplay(make, model, year)
                    } else {
                        // Check local storage as fallback
                        checkLocalStorage()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error checking car details: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Check local storage as fallback
                    checkLocalStorage()
                }
        } else {
            // For guest users, check local storage
            checkLocalStorage()
        }
    }
    
    private fun checkLocalStorage() {
        if (CarPreferences.hasCarDetails()) {
            val (make, model, year) = CarPreferences.getCarDetails()
            navigateToCarDisplay(make, model, year)
        } else {
            // No car details found, navigate to car form
            findNavController().navigate(R.id.action_myCarDashboardFragment_to_carDetailsFormFragment)
        }
    }
    
    private fun navigateToCarDisplay(make: String, model: String, year: String) {
        val bundle = Bundle().apply {
            putString("make", make)
            putString("model", model)
            putString("year", year)
        }
        findNavController().navigate(
            R.id.action_myCarDashboardFragment_to_carDisplayFragment,
            bundle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 