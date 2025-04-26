package com.cse3mad.carcare

import android.app.Application
import com.cse3mad.carcare.utils.AuthManager
import com.cse3mad.carcare.utils.CarPreferences
import com.cse3mad.carcare.utils.MaintenancePreferences
import com.google.firebase.FirebaseApp

class CarCareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        // Initialize AuthManager
        AuthManager.init(this)
        // Initialize CarPreferences
        CarPreferences.init(this)
        // Initialize MaintenancePreferences
        MaintenancePreferences.init(this)
    }
} 