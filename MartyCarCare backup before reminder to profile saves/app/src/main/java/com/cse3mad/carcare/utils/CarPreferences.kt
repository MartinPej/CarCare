package com.cse3mad.carcare.utils

import android.content.Context
import android.content.SharedPreferences

object CarPreferences {
    private const val PREF_NAME = "car_prefs"
    private const val KEY_MAKE = "car_make"
    private const val KEY_MODEL = "car_model"
    private const val KEY_YEAR = "car_year"
    
    private lateinit var sharedPreferences: SharedPreferences
    
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    fun saveCarDetails(make: String, model: String, year: String) {
        sharedPreferences.edit().apply {
            putString(KEY_MAKE, make)
            putString(KEY_MODEL, model)
            putString(KEY_YEAR, year)
            apply()
        }
    }
    
    fun getCarDetails(): Triple<String, String, String> {
        val make = sharedPreferences.getString(KEY_MAKE, "") ?: ""
        val model = sharedPreferences.getString(KEY_MODEL, "") ?: ""
        val year = sharedPreferences.getString(KEY_YEAR, "") ?: ""
        return Triple(make, model, year)
    }
    
    fun hasCarDetails(): Boolean {
        return !sharedPreferences.getString(KEY_MAKE, "").isNullOrEmpty() &&
               !sharedPreferences.getString(KEY_MODEL, "").isNullOrEmpty() &&
               !sharedPreferences.getString(KEY_YEAR, "").isNullOrEmpty()
    }
    
    fun clearCarDetails() {
        sharedPreferences.edit().clear().apply()
    }
} 