package com.example.fastpark.data.repository

import com.example.fastpark.data.model.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    suspend fun signUp(email: String, password: String, username: String): Result<AppUser> =
        runCatching {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user!!.uid
            val user = AppUser(uid, username, email)
            db.collection("users").document(uid).set(user).await()
            user
        }

    suspend fun signIn(email: String, password: String): Result<AppUser> =
        runCatching {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user!!.uid
            val doc = db.collection("users").document(uid).get().await()
            doc.toObject(AppUser::class.java)!!
        }
}
