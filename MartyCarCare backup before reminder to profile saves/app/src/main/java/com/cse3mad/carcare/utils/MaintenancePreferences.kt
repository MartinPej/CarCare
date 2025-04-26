package com.cse3mad.carcare.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar
import java.util.concurrent.TimeUnit

object MaintenancePreferences {
    private const val PREF_NAME = "maintenance_prefs"
    private const val KEY_MAINTENANCE_DATE = "maintenance_date"
    private const val KEY_OIL_CHANGE_DATE = "oil_change_date"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveMaintenanceDate(timestamp: Long) {
        prefs.edit().putLong(KEY_MAINTENANCE_DATE, timestamp).apply()
    }

    fun saveOilChangeDate(timestamp: Long) {
        prefs.edit().putLong(KEY_OIL_CHANGE_DATE, timestamp).apply()
    }

    fun getMaintenanceDate(): Long {
        return prefs.getLong(KEY_MAINTENANCE_DATE, -1)
    }

    fun getOilChangeDate(): Long {
        return prefs.getLong(KEY_OIL_CHANGE_DATE, -1)
    }

    fun hasMaintenanceDate(): Boolean {
        return getMaintenanceDate() != -1L
    }

    fun hasOilChangeDate(): Boolean {
        return getOilChangeDate() != -1L
    }

    fun clearMaintenanceDate() {
        prefs.edit().remove(KEY_MAINTENANCE_DATE).apply()
    }

    fun clearOilChangeDate() {
        prefs.edit().remove(KEY_OIL_CHANGE_DATE).apply()
    }

    fun getDaysUntilMaintenance(): Int {
        val maintenanceDate = getMaintenanceDate()
        if (maintenanceDate == -1L) return -1

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val diffMillis = maintenanceDate - today
        return TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
    }

    fun getDaysUntilOilChange(): Int {
        val oilChangeDate = getOilChangeDate()
        if (oilChangeDate == -1L) return -1

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val diffMillis = oilChangeDate - today
        return TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
    }
} 