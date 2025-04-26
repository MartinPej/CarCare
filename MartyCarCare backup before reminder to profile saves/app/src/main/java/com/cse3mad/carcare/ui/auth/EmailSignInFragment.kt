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
import com.cse3mad.carcare.databinding.FragmentEmailSignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class EmailSignInFragment : Fragment() {
    private var _binding: FragmentEmailSignInBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailSignInBinding.inflate(inflater, container, false)
        
        // Initialize Firebase Auth
        auth = Firebase.auth
        
        setupClickListeners()
        setupBackNavigation()
        
        return binding.root
    }

    private fun setupBackNavigation() {
        // Handle system back button
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
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            
            if (validateInput(email, password)) {
                showLoading(true)
                signInUser(email, password)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.emailLayout.error = "Email is required"
            return false
        }
        binding.emailLayout.error = null

        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password is required"
            return false
        }
        binding.passwordLayout.error = null

        return true
    }

    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    // Sign in success
                    findNavController().navigate(R.id.action_emailSignInFragment_to_myCarDashboardFragment)
                } else {
                    // Sign in failed
                    Toast.makeText(context, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
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