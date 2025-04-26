package com.cse3mad.carcare.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object AuthManager {
    private const val PREF_NAME = "auth_prefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_AUTH_METHOD = "auth_method"
    
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var appContext: Context
    
    fun init(context: Context) {
        appContext = context.applicationContext
        sharedPreferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        auth = FirebaseAuth.getInstance()
        
        // Initialize Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.cse3mad.carcare.R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(appContext, gso)
        
        // Set up auth state listener to persist user info
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // Save user info when signed in
                saveUserInfo(user)
            } else {
                // Clear user info when signed out
                clearUserInfo()
            }
        }
    }
    
    private fun saveUserInfo(user: FirebaseUser) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_ID, user.uid)
            putString(KEY_AUTH_METHOD, determineAuthMethod(user))
            apply()
        }
    }
    
    private fun determineAuthMethod(user: FirebaseUser): String {
        return when {
            user.isAnonymous -> "guest"
            user.phoneNumber != null -> "phone"
            user.providerData.any { it.providerId == "google.com" } -> "google"
            else -> "email"
        }
    }
    
    private fun clearUserInfo() {
        sharedPreferences.edit().clear().apply()
    }
    
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }
    
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    fun signOut() {
        // Check if user was signed in with Google
        val isGoogleUser = auth.currentUser?.providerData?.any { it.providerId == "google.com" } == true
        
        // Sign out from Firebase
        auth.signOut()
        
        // If user was signed in with Google, also sign out from Google
        if (isGoogleUser) {
            googleSignInClient.signOut()
        }
        
        clearUserInfo()
    }
} 