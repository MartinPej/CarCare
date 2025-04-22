package com.cse3mad.carcare

import android.app.Application
import com.google.firebase.FirebaseApp

class CarCareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
} 