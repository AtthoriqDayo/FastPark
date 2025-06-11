package com.example.fastpark.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class User(
    val uid: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val role: String = "user",
    val balance: Double = 0.0,
    val noPlat: String? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null
)