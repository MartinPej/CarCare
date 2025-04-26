package com.cse3mad.carcare.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object MaintenanceDatabase {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String? = auth.currentUser?.uid

    suspend fun saveMaintenanceDate(timestamp: Long) {
        val userId = getUserId() ?: return
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
    }

    suspend fun saveOilChangeDate(timestamp: Long) {
        val userId = getUserId() ?: return
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
    }

    suspend fun getMaintenanceDate(): Long? {
        val userId = getUserId() ?: return null
        return try {
            val document = db.collection("users")
                .document(userId)
                .collection("reminders")
                .document("maintenance")
                .get()
                .await()
            
            document.getLong("timestamp")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getOilChangeDate(): Long? {
        val userId = getUserId() ?: return null
        return try {
            val document = db.collection("users")
                .document(userId)
                .collection("reminders")
                .document("oil_change")
                .get()
                .await()
            
            document.getLong("timestamp")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun clearMaintenanceDate() {
        val userId = getUserId() ?: return
        db.collection("users")
            .document(userId)
            .collection("reminders")
            .document("maintenance")
            .delete()
            .await()
    }

    suspend fun clearOilChangeDate() {
        val userId = getUserId() ?: return
        db.collection("users")
            .document(userId)
            .collection("reminders")
            .document("oil_change")
            .delete()
            .await()
    }

    suspend fun clearAllReminders() {
        val userId = getUserId() ?: return
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
    }
} 