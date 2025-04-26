package com.cse3mad.carcare.ui.mycar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cse3mad.carcare.R
import com.cse3mad.carcare.databinding.FragmentMyCarBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class MyCarFragment : Fragment() {
    private var _binding: FragmentMyCarBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var db: FirebaseFirestore
    
    companion object {
        private const val RC_SIGN_IN = 9001
        private const val RC_PHONE_SIGN_IN = 9002
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyCarBinding.inflate(inflater, container, false)
        
        // Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        
        setupClickListeners()
        
        return binding.root
    }

    private fun setupClickListeners() {
        binding.btnEmailSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_myCarFragment_to_emailSignInFragment)
        }
        
        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
        
        binding.btnPhoneSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_myCarFragment_to_phoneSignInFragment)
        }
        
        binding.btnGuestSignIn.setOnClickListener {
            signInAsGuest()
        }
        
        binding.txtSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_myCarFragment_to_emailSignUpFragment)
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signInAsGuest() {
        auth.signInAnonymously()
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    findNavController().navigate(R.id.action_myCarFragment_to_myCarDashboardFragment)
                } else {
                    // Sign in failed
                    Toast.makeText(context, "Guest sign in failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(context, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Get the current user
                    val user = auth.currentUser
                    
                    // Create a user data map
                    val userData = hashMapOf(
                        "uid" to user?.uid,
                        "displayName" to user?.displayName,
                        "email" to user?.email,
                        "photoUrl" to user?.photoUrl?.toString(),
                        "provider" to "google",
                        "createdAt" to System.currentTimeMillis()
                    )
                    
                    // Store user data in Firestore
                    user?.let { firebaseUser ->
                        db.collection("users")
                            .document(firebaseUser.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                // Navigate based on current fragment
                                try {
                                    findNavController().navigate(R.id.action_myCarFragment_to_myCarDashboardFragment)
                                } catch (e: Exception) {
                                    // If navigation fails, try to navigate to home
                                    try {
                                        findNavController().navigate(R.id.navigation_home)
                                    } catch (e2: Exception) {
                                        // If all navigation fails, show error
                                        Toast.makeText(context, "Navigation failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to store user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Sign in failed
                    Toast.makeText(context, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 