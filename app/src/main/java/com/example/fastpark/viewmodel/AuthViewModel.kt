package com.example.fastpark.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fastpark.data.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers: LiveData<List<User>> = _allUsers //

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    val googleSignInClient: GoogleSignInClient

    // Firestore listener untuk allUsers
    private var allUsersListener: ListenerRegistration? = null

    init {
        _currentUser.value = auth.currentUser
        if (_currentUser.value != null) {
            viewModelScope.launch { // Launch coroutine untuk fetchUserData di init
                fetchUserData(_currentUser.value!!.uid)
            }
            // Mulai mendengarkan perubahan semua pengguna jika user sudah login
            startListeningForAllUsers()
        }

        // Konfigurasi Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("122734914182-vioeetcrl9k7kmrks3sm2v1n1htplcfn.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(application, gso)
    }

    // Dipanggil saat ViewModel di-clear
    override fun onCleared() {
        super.onCleared()
        stopListeningForAllUsers()
    }

    private fun startListeningForAllUsers() {
        allUsersListener?.remove() // Hapus listener sebelumnya jika ada
        _isLoading.value = true
        allUsersListener = db.collection("users")
            .addSnapshotListener { snapshots, e ->
                _isLoading.postValue(false) // Gunakan postValue karena ini dari callback background
                if (e != null) {
                    _error.postValue("Gagal memuat daftar pengguna: ${e.message}")
                    Log.w("AuthViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val usersList = snapshots.documents.mapNotNull { it.toObject(User::class.java) }
                    _allUsers.postValue(usersList)
                }
            }
    }

    private fun stopListeningForAllUsers() {
        allUsersListener?.remove()
        allUsersListener = null
    }

    fun clearError() {
        _error.value = null
    }

    // --- Pendaftaran dengan Email & Password (untuk pengguna biasa) ---
    // Ubah dari suspend fun menjadi fun biasa, dan bungkus isinya dengan viewModelScope.launch
    fun signUpWithEmailPassword(email: String, pass: String, displayName: String) {
        _isLoading.value = true
        _error.value = null // Bersihkan error sebelumnya
        viewModelScope.launch(Dispatchers.IO) { // Jalankan di Dispatchers.IO
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
                    saveUserToFirestore(newUser) // Ini masih suspend, tapi dipanggil dari coroutine
                    _currentUser.postValue(firebaseUser)
                    _userData.postValue(newUser)
                } else {
                    _error.postValue("Gagal membuat pengguna.")
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Terjadi kesalahan saat pendaftaran.")
                Log.e("AuthViewModel", "SignUp Error: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // --- Pendaftaran Akun Baru oleh ADMIN ---
    // Ubah dari suspend fun menjadi fun biasa, dan bungkus isinya dengan viewModelScope.launch
    fun createUserWithEmailAndPassword(email: String, pass: String, displayName: String?, role: String) {
        _isLoading.value = true
        _error.value = null // Bersihkan error sebelumnya
        viewModelScope.launch(Dispatchers.IO) { // Jalankan di Dispatchers.IO
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val firebaseUser = result.user

                if (firebaseUser != null) {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email,
                        displayName = displayName,
                        role = role
                    )
                    saveUserToFirestore(newUser) // Ini masih suspend, tapi dipanggil dari coroutine
                    // Mulai ulang listening for all users untuk merefresh daftar di UI Admin
                    startListeningForAllUsers()
                } else {
                    _error.postValue("Gagal membuat pengguna baru.")
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Terjadi kesalahan saat membuat akun baru.")
                Log.e("AuthViewModel", "Admin Create User Error: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // --- Login dengan Email & Password ---
    // Ubah dari suspend fun menjadi fun biasa, dan bungkus isinya dengan viewModelScope.launch
    fun signInWithEmailPassword(email: String, pass: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) { // Jalankan di Dispatchers.IO
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                val firebaseUser = result.user
                _currentUser.postValue(firebaseUser)
                if (firebaseUser != null) {
                    fetchUserData(firebaseUser.uid) // Ini masih suspend, tapi dipanggil dari coroutine
                    startListeningForAllUsers()
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Terjadi kesalahan saat login.")
                Log.e("AuthViewModel", "SignIn Error: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // --- Login dengan Google ---
    // Ubah dari suspend fun menjadi fun biasa, dan bungkus isinya dengan viewModelScope.launch
    fun signInWithGoogleCredential(credential: AuthCredential) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) { // Jalankan di Dispatchers.IO
            try {
                val result = auth.signInWithCredential(credential).await()
                val firebaseUser = result.user
                _currentUser.postValue(firebaseUser)
                if (firebaseUser != null) {
                    val userDoc = db.collection("users").document(firebaseUser.uid).get().await()
                    if (!userDoc.exists()) {
                        val newUser = User(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email,
                            displayName = firebaseUser.displayName,
                            role = "user" // Default role
                        )
                        saveUserToFirestore(newUser) // Ini masih suspend
                        _userData.postValue(newUser)
                    } else {
                        _userData.postValue(userDoc.toObject(User::class.java))
                    }
                    startListeningForAllUsers()
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Terjadi kesalahan saat login dengan Google.")
                Log.e("AuthViewModel", "Google SignIn Error: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // --- Menyimpan atau Memperbarui Data Pengguna di Firestore ---
    // Ini adalah fungsi internal, biarkan suspend
    private suspend fun saveUserToFirestore(user: User) {
        try {
            db.collection("users").document(user.uid)
                .set(user, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            _error.postValue("Gagal menyimpan data pengguna ke Firestore: ${e.message}")
            Log.e("AuthViewModel", "Firestore Save Error: ${e.message}", e)
        }
    }

    // --- Mengambil Data Pengguna dari Firestore ---
    private suspend fun fetchUserData(uid: String) {
        _isLoading.postValue(true)
        _error.postValue(null)
        try {
            val document = db.collection("users").document(uid).get().await()
            if (document.exists()) {
                _userData.postValue(document.toObject(User::class.java))
            } else {
                _error.postValue("Data pengguna tidak ditemukan di Firestore.")
                // Jika user ada di Auth tapi tidak di Firestore
                val firebaseUser = auth.currentUser
                if (firebaseUser != null && firebaseUser.uid == uid) {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email,
                        displayName = firebaseUser.displayName,
                        role = "user" // Default role
                    )
                    saveUserToFirestore(newUser)
                    _userData.postValue(newUser)
                    _error.postValue(null)
                }
            }
        } catch (e: Exception) {
            _error.postValue("Gagal mengambil data pengguna: ${e.message}")
            Log.e("AuthViewModel", "Firestore Fetch Error: ${e.message}", e)
        } finally {
            _isLoading.postValue(false)
        }
    }

    // --- Mengubah Profil Pengguna (oleh Admin) ---
    // Ubah dari suspend fun menjadi fun biasa, dan bungkus isinya dengan viewModelScope.launch
    fun updateUserProfile(uid: String, newDisplayName: String?, newRole: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) { // Jalankan di Dispatchers.IO
            if (_userData.value?.role != "administrator") {
                _error.postValue("Anda tidak memiliki hak untuk mengubah profil pengguna lain.")
                _isLoading.postValue(false)
                return@launch // Keluar dari coroutine
            }

            try {
                val updates = mutableMapOf<String, Any>()
                newDisplayName?.let { updates["displayName"] = it }
                updates["role"] = newRole

                db.collection("users").document(uid).update(updates).await()

                // Jika user yang diupdate adalah user yang sedang login, refresh data mereka
                if (uid == _currentUser.value?.uid) {
                    fetchUserData(uid) // Ini masih suspend
                }
                // Mulai ulang listening for all users untuk merefresh daftar di UI Admin
                startListeningForAllUsers()
            } catch (e: Exception) {
                _error.postValue("Gagal mengubah profil pengguna: ${e.message}")
                Log.e("AuthViewModel", "Update Profile Error: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // --- Menghapus Pengguna (oleh Admin) ---
    // Ubah dari suspend fun menjadi fun biasa, dan bungkus isinya dengan viewModelScope.launch
    fun deleteUser(uid: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) { // Jalankan di Dispatchers.IO
            if (_userData.value?.role != "administrator") {
                _error.postValue("Anda tidak memiliki hak untuk menghapus pengguna.")
                _isLoading.postValue(false)
                return@launch // Keluar dari coroutine
            }

            try {
                // Hapus dokumen user dari Firestore
                db.collection("users").document(uid).delete().await()

                // Mulai ulang listening for all users untuk merefresh daftar di UI Admin
                startListeningForAllUsers()

                // --- BAGIAN INI MEMBUTUHKAN CLOUD FUNCTION UNTUK MENGHAPUS DARI FIREBASE AUTH ---
                // ... (tetap dengan komentar Anda)
                // ----------------------------------------------------------------------------------

            } catch (e: Exception) {
                _error.postValue("Gagal menghapus pengguna: ${e.message}")
                Log.e("AuthViewModel", "Delete User Error: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }


    // --- Logout ---
    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
        _currentUser.value = null
        _userData.value = null
        _error.value = null
        stopListeningForAllUsers()
    }
}