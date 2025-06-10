// File: viewmodel/AuthViewModel.kt
@file:Suppress("DEPRECATION")

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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // LiveData yang sudah ada
    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers: LiveData<List<User>> = _allUsers

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    val googleSignInClient: GoogleSignInClient

    // --- PENAMBAHAN: State untuk QR Code Dinamis ---
    private val _dynamicQrToken = MutableStateFlow<String?>(null)
    val dynamicQrToken: StateFlow<String?> = _dynamicQrToken.asStateFlow()
    private var tokenRefreshJob: Job? = null
    private val TOKEN_REFRESH_INTERVAL_SECONDS = 55L
    // --- AKHIR PENAMBAHAN ---

    // Firestore listener untuk allUsers
    private var allUsersListener: ListenerRegistration? = null

    init {
        _currentUser.value = auth.currentUser
        if (_currentUser.value != null) {
            viewModelScope.launch {
                fetchUserData(_currentUser.value!!.uid)
            }

        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("122734914182-vioeetcrl9k7kmrks3sm2v1n1htplcfn.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(application, gso)
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningForAllUsers()
        stopDynamicQrTokenUpdates() // PENAMBAHAN: Pastikan job dihentikan saat ViewModel hancur
    }

    private fun startListeningForAllUsers() {
        allUsersListener?.remove()
        _isLoading.value = true
        allUsersListener = db.collection("users")
            .addSnapshotListener { snapshots, e ->
                _isLoading.postValue(false)
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

    fun signUpWithEmailPassword(email: String, pass: String, displayName: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email,
                        displayName = displayName,
                        role = "user"
                    )
                    saveUserToFirestore(newUser)
                    _currentUser.postValue(firebaseUser)
                    _userData.postValue(newUser)
                } else {
                    _error.postValue("Gagal membuat pengguna.")
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Terjadi kesalahan saat pendaftaran.")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun createUserWithEmailAndPassword(email: String, pass: String, displayName: String?, role: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) {
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
                    saveUserToFirestore(newUser)
                } else {
                    _error.postValue("Gagal membuat pengguna baru.")
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Terjadi kesalahan saat membuat akun baru.")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun signInWithEmailPassword(email: String, pass: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                val firebaseUser = result.user
                _currentUser.postValue(firebaseUser)
                if (firebaseUser != null) {
                    fetchUserData(firebaseUser.uid)
                    startListeningForAllUsers()
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Terjadi kesalahan saat login.")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun signInWithGoogleCredential(credential: AuthCredential) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) {
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
                            role = "user"
                        )
                        saveUserToFirestore(newUser)
                        _userData.postValue(newUser)
                    } else {
                        _userData.postValue(userDoc.toObject(User::class.java))
                    }
                    startListeningForAllUsers()
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Terjadi kesalahan saat login dengan Google.")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private suspend fun saveUserToFirestore(user: User) {
        try {
            db.collection("users").document(user.uid)
                .set(user, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            _error.postValue("Gagal menyimpan data pengguna ke Firestore: ${e.message}")
        }
    }

    private suspend fun fetchUserData(uid: String) {
        _isLoading.postValue(true)
        _error.postValue(null)
        try {
            val document = db.collection("users").document(uid).get().await()
            if (document.exists()) {
                _userData.postValue(document.toObject(User::class.java))

                val user = document.toObject(User::class.java)
                _userData.postValue(user)
                // --- PENAMBAHAN LOGIKA KONDISIONAL DI SINI ---
                if (user?.role == "administrator") {
                    startListeningForAllUsers() // Hanya panggil jika user adalah admin
                } else {
                    stopListeningForAllUsers() // Pastikan listener berhenti jika user bukan admin
                }
                // --- AKHIR PENAMBAHAN ---

            } else {
                _error.postValue("Data pengguna tidak ditemukan di Firestore.")
                val firebaseUser = auth.currentUser
                if (firebaseUser != null && firebaseUser.uid == uid) {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email,
                        displayName = firebaseUser.displayName,
                        role = "user"
                    )
                    saveUserToFirestore(newUser)
                    _userData.postValue(newUser)
                    _error.postValue(null)
                }
            }
        } catch (e: Exception) {
            _error.postValue("Gagal mengambil data pengguna: ${e.message}")
        } finally {
            _isLoading.postValue(false)
        }
    }

    fun updateUserProfile(uid: String, newDisplayName: String?, newRole: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) {
            if (_userData.value?.role != "administrator") {
                _error.postValue("Anda tidak memiliki hak untuk mengubah profil pengguna lain.")
                _isLoading.postValue(false)
                return@launch
            }

            try {
                val updates = mutableMapOf<String, Any>()
                newDisplayName?.let { updates["displayName"] = it }
                updates["role"] = newRole

                db.collection("users").document(uid).update(updates).await()
                if (uid == _currentUser.value?.uid) {
                    fetchUserData(uid)
                }
            } catch (e: Exception) {
                _error.postValue("Gagal mengubah profil pengguna: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun deleteUser(uid: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) {
            if (_userData.value?.role != "administrator") {
                _error.postValue("Anda tidak memiliki hak untuk menghapus pengguna.")
                _isLoading.postValue(false)
                return@launch
            }

            try {
                db.collection("users").document(uid).delete().await()
            } catch (e: Exception) {
                _error.postValue("Gagal menghapus pengguna: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }


    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
        _currentUser.value = null
        _userData.value = null
        _error.value = null
        stopListeningForAllUsers()
        stopDynamicQrTokenUpdates() // PENAMBAHAN: Hentikan refresh token saat logout
    }

    fun startDynamicQrTokenUpdates() {
        if (tokenRefreshJob?.isActive == true) {
            Log.d("AuthViewModel", "Pembaruan token QR dinamis sudah berjalan.")
            return
        }
        if (auth.currentUser == null) {
            Log.w("AuthViewModel", "Tidak bisa memulai pembaruan token: Pengguna belum login.")
            _dynamicQrToken.value = null
            _error.postValue("Anda harus login untuk melihat QR Code.")
            return
        }

        tokenRefreshJob = viewModelScope.launch {
            Log.d("AuthViewModel", "Memulai job pembaruan token QR Dinamis untuk user: ${auth.currentUser?.uid}")
            while (isActive) {
                fetchNewTokenFromServer() // Kita tidak perlu menangani nilai return di sini karena fungsi sudah update state
                delay(TOKEN_REFRESH_INTERVAL_SECONDS * 1000L)
            }
        }
    }

    fun stopDynamicQrTokenUpdates() {
        tokenRefreshJob?.cancel()
        tokenRefreshJob = null
        _dynamicQrToken.value = null
        Log.d("AuthViewModel", "Pembaruan Token QR Dinamis dihentikan.")
    }

    private suspend fun fetchNewTokenFromServer() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            _error.postValue("Sesi tidak valid. Silakan coba login kembali.")
            Log.e("AuthViewModel", "fetchNewTokenFromServer dipanggil tapi firebaseUser null.")
            return
        }

        var idToken: String? = null
        try {
            idToken = firebaseUser.getIdToken(true).await().token
            // === LOGGING TAMBAHAN UNTUK DEBUGGING ===
            if (idToken.isNullOrEmpty()) {
                Log.e("AuthViewModel", "getIdToken() berhasil tapi mengembalikan token null atau kosong.")
                _error.postValue("Gagal mendapatkan token otentikasi.")
                return
            }
            Log.d("AuthViewModel", "Berhasil mendapatkan ID Token. Panjang: ${idToken.length}")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Pengecualian saat mengambil ID Token: ${e.message}", e)
            _error.postValue("Gagal memvalidasi sesi Anda.")
            return
        }

        val functionUrl = "https://generatedynamicqrtoken-tmasj2diia-uc.a.run.app"

        withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            try {
                val emptyRequestBody = "".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url(functionUrl)
                    .header("Authorization", "Bearer $idToken") // Mengirim ID Token
                    .post(emptyRequestBody)
                    .build()

                Log.d("AuthViewModel", "Memanggil Cloud Function dengan ID Token...")
                val response = client.newCall(request).execute()
                val responseBodyString = response.body?.string()

                if (response.isSuccessful && responseBodyString != null) {
                    val jsonResponse = JSONObject(responseBodyString)
                    val token = jsonResponse.optString("token")
                    if (token.isNotEmpty()) {
                        _dynamicQrToken.value = token
                        _error.postValue(null) // Bersihkan error jika sukses
                    } else {
                        _error.postValue("Gagal memproses data QR dari server (token tidak valid).")
                    }
                } else {
                    val serverErrorMessage = try {
                        JSONObject(responseBodyString ?: "{}").optString("message", "Error dari server (Kode: ${response.code}).")
                    } catch (e: Exception) { "Error dari server (Kode: ${response.code})." }
                    _error.postValue(serverErrorMessage)
                }
            } catch (e: Exception) {
                _error.postValue("Terjadi kesalahan jaringan saat memuat QR Code.")
            }
        }
    }

    // --- [Sertakan fungsi-fungsi admin Anda yang lain di sini jika perlu] ---
    // (signInWithEmailPassword, createUserWithEmailAndPassword, updateUserProfile, deleteUser, dll.)
    // Pastikan semua fungsi tersebut menggunakan pola viewModelScope.launch(Dispatchers.IO) dan .postValue() seperti di file Anda.
}