package com.cse3mad.carcare.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cse3mad.carcare.R
import com.cse3mad.carcare.databinding.FragmentEmailSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class EmailSignUpFragment : Fragment() {
    private var _binding: FragmentEmailSignUpBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailSignUpBinding.inflate(inflater, container, false)
        
        // Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        
        setupClickListeners()
        
        return binding.root
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            
            if (validateInput(name, email, password)) {
                showLoading(true)
                signUpUser(name, email, password)
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        if (name.isEmpty()) {
            binding.nameLayout.error = "Name is required"
            return false
        }
        binding.nameLayout.error = null

        if (email.isEmpty()) {
            binding.emailLayout.error = "Email is required"
            return false
        }
        binding.emailLayout.error = null

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

    private fun signUpUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Get the current user
                    val user = auth.currentUser
                    
                    // Create a user data map
                    val userData = hashMapOf(
                        "uid" to user?.uid,
                        "displayName" to name,
                        "email" to email,
                        "provider" to "email",
                        "createdAt" to System.currentTimeMillis()
                    )
                    
                    // Store user data in Firestore
                    user?.let { firebaseUser ->
                        db.collection("users")
                            .document(firebaseUser.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                showLoading(false)
                                // Navigate to dashboard
                                findNavController().navigate(R.id.action_emailSignUpFragment_to_myCarDashboardFragment)
                            }
                            .addOnFailureListener { e ->
                                showLoading(false)
                                Toast.makeText(context, "Failed to store user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    showLoading(false)
                    Toast.makeText(context, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSignUp.isEnabled = !show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 