package com.example.fastpark.data

data class User(
    val uid: String = "",
    val email: String? = null,
    val displayName: String? = null,
    var role: String = "user" // Default role
)