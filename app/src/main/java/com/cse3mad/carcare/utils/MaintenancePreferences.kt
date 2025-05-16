package com.cse3mad.carcare.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MaintenancePreferences(context: Context) {
    companion object {
        private var instance: MaintenancePreferences? = null

        fun init(context: Context) {
            if (instance == null) {
                instance = MaintenancePreferences(context)
            }
        }

        fun getInstance(): MaintenancePreferences {
            return instance ?: throw IllegalStateException("MaintenancePreferences must be initialized first")
        }
    }

    init {
        MaintenanceManager.init(context)
    }

    suspend fun setMaintenanceDate(calendar: Calendar) {
        MaintenanceManager.saveMaintenanceDate(calendar.timeInMillis)
    }

    suspend fun setOilChangeDate(calendar: Calendar) {
        MaintenanceManager.saveOilChangeDate(calendar.timeInMillis)
    }

    suspend fun getMaintenanceDate(): Long {
        return MaintenanceManager.getMaintenanceDate()
    }

    suspend fun getOilChangeDate(): Long {
        return MaintenanceManager.getOilChangeDate()
    }

    suspend fun clearMaintenanceDate() {
        MaintenanceManager.clearMaintenanceDate()
    }

    suspend fun clearOilChangeDate() {
        MaintenanceManager.clearOilChangeDate()
    }

    suspend fun clearAllReminders() {
        MaintenanceManager.clearAllReminders()
    }

    suspend fun hasMaintenanceDate(): Boolean {
        return MaintenanceManager.hasMaintenanceDate()
    }

    suspend fun hasOilChangeDate(): Boolean {
        return MaintenanceManager.hasOilChangeDate()
    }

    suspend fun getDaysUntilMaintenance(): Int {
        return MaintenanceManager.getDaysUntilMaintenance()
    }

    suspend fun getDaysUntilOilChange(): Int {
        return MaintenanceManager.getDaysUntilOilChange()
    }
} 