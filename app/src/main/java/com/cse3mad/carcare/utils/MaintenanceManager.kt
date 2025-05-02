package com.cse3mad.carcare.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.concurrent.TimeUnit

object MaintenanceManager {
    private const val PREF_NAME = "maintenance_prefs"
    private const val KEY_MAINTENANCE_DATE = "maintenance_date"
    private const val KEY_OIL_CHANGE_DATE = "oil_change_date"
    private lateinit var prefs: SharedPreferences
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun isUserLoggedIn(): Boolean = auth.currentUser != null

    suspend fun saveMaintenanceDate(timestamp: Long) {
        if (isUserLoggedIn()) {
            // Save to Firestore
            val userId = auth.currentUser!!.uid
            val maintenanceData = hashMapOf(
                "timestamp" to timestamp,
                "type" to "maintenance"
            )
            
            db.collection("users")
                .document(userId)
                .collection("reminders")
                .document("maintenance")
                .set(maintenanceData)
                .await()
        } else {
            // Save locally
            prefs.edit().putLong(KEY_MAINTENANCE_DATE, timestamp).apply()
        }
    }

    suspend fun saveOilChangeDate(timestamp: Long) {
        if (isUserLoggedIn()) {
            // Save to Firestore
            val userId = auth.currentUser!!.uid
            val oilChangeData = hashMapOf(
                "timestamp" to timestamp,
                "type" to "oil_change"
            )
            
            db.collection("users")
                .document(userId)
                .collection("reminders")
                .document("oil_change")
                .set(oilChangeData)
                .await()
        } else {
            // Save locally
            prefs.edit().putLong(KEY_OIL_CHANGE_DATE, timestamp).apply()
        }
    }

    suspend fun getMaintenanceDate(): Long {
        return if (isUserLoggedIn()) {
            try {
                val userId = auth.currentUser!!.uid
                val document = db.collection("users")
                    .document(userId)
                    .collection("reminders")
                    .document("maintenance")
                    .get()
                    .await()
                
                document.getLong("timestamp") ?: 0L
            } catch (e: Exception) {
                0L
            }
        } else {
            prefs.getLong(KEY_MAINTENANCE_DATE, 0L)
        }
    }

    suspend fun getOilChangeDate(): Long {
        return if (isUserLoggedIn()) {
            try {
                val userId = auth.currentUser!!.uid
                val document = db.collection("users")
                    .document(userId)
                    .collection("reminders")
                    .document("oil_change")
                    .get()
                    .await()
                
                document.getLong("timestamp") ?: 0L
            } catch (e: Exception) {
                0L
            }
        } else {
            prefs.getLong(KEY_OIL_CHANGE_DATE, 0L)
        }
    }

    suspend fun clearMaintenanceDate() {
        if (isUserLoggedIn()) {
            val userId = auth.currentUser!!.uid
            db.collection("users")
                .document(userId)
                .collection("reminders")
                .document("maintenance")
                .delete()
                .await()
        } else {
            prefs.edit().remove(KEY_MAINTENANCE_DATE).apply()
        }
    }

    suspend fun clearOilChangeDate() {
        if (isUserLoggedIn()) {
            val userId = auth.currentUser!!.uid
            db.collection("users")
                .document(userId)
                .collection("reminders")
                .document("oil_change")
                .delete()
                .await()
        } else {
            prefs.edit().remove(KEY_OIL_CHANGE_DATE).apply()
        }
    }

    suspend fun clearAllReminders() {
        if (isUserLoggedIn()) {
            val userId = auth.currentUser!!.uid
            val batch = db.batch()
            
            val maintenanceRef = db.collection("users")
                .document(userId)
                .collection("reminders")
                .document("maintenance")
            
            val oilChangeRef = db.collection("users")
                .document(userId)
                .collection("reminders")
                .document("oil_change")
            
            batch.delete(maintenanceRef)
            batch.delete(oilChangeRef)
            batch.commit().await()
        } else {
            prefs.edit()
                .remove(KEY_MAINTENANCE_DATE)
                .remove(KEY_OIL_CHANGE_DATE)
                .apply()
        }
    }

    suspend fun hasMaintenanceDate(): Boolean {
        return getMaintenanceDate() != 0L
    }

    suspend fun hasOilChangeDate(): Boolean {
        return getOilChangeDate() != 0L
    }

    suspend fun getDaysUntilMaintenance(): Int {
        val maintenanceDate = getMaintenanceDate()
        if (maintenanceDate == 0L) return 0
        
        // Get current time and normalize to start of day
        val currentCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // Create calendar for maintenance date
        val maintenanceCalendar = Calendar.getInstance().apply {
            timeInMillis = maintenanceDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // Calculate days between
        val diff = maintenanceCalendar.timeInMillis - currentCalendar.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }

    suspend fun getDaysUntilOilChange(): Int {
        val oilChangeDate = getOilChangeDate()
        if (oilChangeDate == 0L) return 0
        
        // Get current time and normalize to start of day
        val currentCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // Create calendar for oil change date
        val oilChangeCalendar = Calendar.getInstance().apply {
            timeInMillis = oilChangeDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // Calculate days between
        val diff = oilChangeCalendar.timeInMillis - currentCalendar.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }
} 