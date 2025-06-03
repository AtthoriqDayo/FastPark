package com.example.fastpark.data

data class User(
    val balance: Double = 0.0,
    val uid: String = "",
    val email: String? = null,
    val displayName: String? = null,
    var role: String = "user" // Default role
){
    constructor() : this(0.0) // Untuk Firestore deserialization
}