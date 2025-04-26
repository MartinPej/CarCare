package com.cse3mad.carcare.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cse3mad.carcare.R
import com.cse3mad.carcare.databinding.FragmentPhoneSignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class PhoneSignInFragment : Fragment() {
    private var _binding: FragmentPhoneSignInBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneSignInBinding.inflate(inflater, container, false)
        
        // Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        
        setupClickListeners()
        setupBackNavigation()
        
        return binding.root
    }

    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        )
    }

    private fun setupClickListeners() {
        binding.btnSignIn.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val phone = binding.phoneInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            
            if (validateInput(name, phone, password)) {
                showLoading(true)
                signInWithPhone(name, phone, password)
            }
        }
    }

    private fun validateInput(name: String, phone: String, password: String): Boolean {
        if (name.isEmpty()) {
            binding.nameLayout.error = "Name is required"
            return false
        }
        binding.nameLayout.error = null

        if (phone.isEmpty()) {
            binding.phoneLayout.error = "Phone number is required"
            return false
        }
        if (!phone.matches(Regex("^\\+?[1-9]\\d{1,14}\$"))) {
            binding.phoneLayout.error = "Invalid phone number format"
            return false
        }
        binding.phoneLayout.error = null

        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password is required"
            return false
        }
        if (password.length < 6) {
            binding.passwordLayout.error = "Password must be at least 6 characters"
            return false
        }
        binding.passwordLayout.error = null

        return true
    }

    private fun signInWithPhone(name: String, phone: String, password: String) {
        // Create a user data map
        val userData = hashMapOf(
            "displayName" to name,
            "phone" to phone,
            "provider" to "phone",
            "createdAt" to System.currentTimeMillis()
        )
        
        // Store user data in Firestore using phone number as document ID
        db.collection("users")
            .document(phone)
            .set(userData)
            .addOnSuccessListener {
                showLoading(false)
                // Navigate to dashboard
                findNavController().navigate(R.id.action_phoneSignInFragment_to_myCarDashboardFragment)
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(context, "Failed to store user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSignIn.isEnabled = !show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 