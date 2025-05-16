package com.cse3mad.carcare.ui.ratings

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class MechanicRating(
    @DocumentId
    val id: String = "",
    val mechanicName: String = "",
    val dateVisited: Date = Date(),
    val rating: Float = 0f,
    val comment: String = "",
    val userId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
) 