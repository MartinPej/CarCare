package com.cse3mad.carcare

import android.app.Application
import com.cse3mad.carcare.utils.AuthManager
import com.cse3mad.carcare.utils.CarPreferences
import com.cse3mad.carcare.utils.MaintenancePreferences
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class CarCareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Disable Firebase Analytics logging and data collection
        Firebase.analytics.setAnalyticsCollectionEnabled(false)
        
        // Initialize other components
        AuthManager.init(this)
        CarPreferences.init(this)
        MaintenancePreferences.init(this)
    }
} 