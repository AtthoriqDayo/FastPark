// File: viewmodel/AuthViewModel.kt
package com.example.fastpark.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.fastpark.data.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch // Pastikan import ini ada
import androidx.lifecycle.viewModelScope // Pastikan import ini ada

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    val googleSignInClient: GoogleSignInClient

    init {
        _currentUser.value = auth.currentUser
        if (_currentUser.value != null) {
            // Panggil fetchUserData di dalam viewModelScope
            viewModelScope.launch {
                fetchUserData(_currentUser.value!!.uid)
            }
        }

        // Konfigurasi Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("122734914182-vioeetcrl9k7kmrks3sm2v1n1htplcfn.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(application, gso)
    }

    fun clearError() {
        _error.value = null
    }

    // --- Pendaftaran dengan Email & Password ---
    suspend fun signUpWithEmailPassword(email: String, pass: String, displayName: String) {
        try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val newUser = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = displayName,
                    role = "user" // Default role saat pendaftaran baru
                )
                saveUserToFirestore(newUser)
                _currentUser.value = firebaseUser
                _userData.value = newUser
                _error.value = null
            } else {
                _error.value = "Gagal membuat pengguna."
            }
        } catch (e: Exception) {
            _error.value = e.message ?: "Terjadi kesalahan saat pendaftaran."
            Log.e("AuthViewModel", "SignUp Error: ${e.message}", e)
        }
    }

    // --- Login dengan Email & Password ---
    suspend fun signInWithEmailPassword(email: String, pass: String) {
        try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user
            _currentUser.value = firebaseUser
            if (firebaseUser != null) {
                fetchUserData(firebaseUser.uid)
            }
            _error.value = null
        } catch (e: Exception) {
            _error.value = e.message ?: "Terjadi kesalahan saat login."
            Log.e("AuthViewModel", "SignIn Error: ${e.message}", e)
        }
    }

    // --- Login dengan Google ---
    suspend fun signInWithGoogleCredential(credential: AuthCredential) {
        try {
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user
            _currentUser.value = firebaseUser
            if (firebaseUser != null) {
                val userDoc = db.collection("users").document(firebaseUser.uid).get().await()
                if (!userDoc.exists()) {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email,
                        displayName = firebaseUser.displayName,
                        role = "user" // Default role
                    )
                    saveUserToFirestore(newUser)
                    _userData.value = newUser
                } else {
                    _userData.value = userDoc.toObject(User::class.java)
                }
            }
            _error.value = null
        } catch (e: Exception) {
            _error.value = e.message ?: "Terjadi kesalahan saat login dengan Google."
            Log.e("AuthViewModel", "Google SignIn Error: ${e.message}", e)
        }
    }


    // --- Menyimpan atau Memperbarui Data Pengguna di Firestore ---
    private suspend fun saveUserToFirestore(user: User) {
        try {
            db.collection("users").document(user.uid)
                .set(user, SetOptions.merge()) // SetOptions.merge() untuk update jika sudah ada
                .await()
        } catch (e: Exception) {
            _error.value = "Gagal menyimpan data pengguna ke Firestore: ${e.message}"
            Log.e("AuthViewModel", "Firestore Save Error: ${e.message}", e)
        }
    }

    // --- Mengambil Data Pengguna dari Firestore ---
    suspend fun fetchUserData(uid: String) {
        try {
            val document = db.collection("users").document(uid).get().await()
            if (document.exists()) {
                _userData.value = document.toObject(User::class.java)
            } else {
                // Handle kasus jika data pengguna tidak ditemukan di Firestore
                // Mungkin buat entri baru jika diperlukan, atau tandai sebagai error
                _error.value = "Data pengguna tidak ditemukan di Firestore."
                // Jika user ada di Auth tapi tidak di Firestore (misal migrasi/error sebelumnya)
                // Anda bisa membuat ulang dokumen user di sini
                val firebaseUser = auth.currentUser
                if (firebaseUser != null && firebaseUser.uid == uid) {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email,
                        displayName = firebaseUser.displayName,
                        role = "user" // Default role
                    )
                    saveUserToFirestore(newUser) // Coba simpan lagi
                    _userData.value = newUser
                    _error.value = null // Reset error jika berhasil disimpan
                }
            }
        } catch (e: Exception) {
            _error.value = "Gagal mengambil data pengguna: ${e.message}"
            Log.e("AuthViewModel", "Firestore Fetch Error: ${e.message}", e)
        }
    }

    // --- Logout ---
    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut() // Penting untuk Google Sign-Out
        _currentUser.value = null
        _userData.value = null
        _error.value = null
    }

    // --- Mengubah Peran Pengguna (Hanya untuk Admin) ---
    suspend fun updateUserRole(uid: String, newRole: String): Boolean {
        // Idealnya, fungsi ini hanya bisa diakses jika user saat ini adalah admin
        // Anda perlu menambahkan validasi role di sini atau di UI
        if (_userData.value?.role != "administrator") {
            _error.value = "Anda tidak memiliki hak untuk mengubah peran."
            return false
        }
        try {
            db.collection("users").document(uid).update("role", newRole).await()
            // Jika user yang diubah adalah user saat ini, refresh datanya
            if (uid == _currentUser.value?.uid) {
                fetchUserData(uid)
            }
            _error.value = null
            return true
        } catch (e: Exception) {
            _error.value = "Gagal mengubah peran pengguna: ${e.message}"
            Log.e("AuthViewModel", "Update Role Error: ${e.message}", e)
            return false
        }
    }
}