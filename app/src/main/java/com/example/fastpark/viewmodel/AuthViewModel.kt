// File: app/src/main/java/com/example/fastpark/viewmodel/AuthViewModel.kt
// KODE GABUNGAN LENGKAP

@file:Suppress("DEPRECATION")

package com.example.fastpark.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fastpark.data.User
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
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

    // Klien Firebase & Google
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val functions: FirebaseFunctions = Firebase.functions("us-central1") // Sesuaikan dengan region Anda
    private val oneTapClient = Identity.getSignInClient(application)
    val googleSignInClient: GoogleSignInClient

    // Konfigurasi
    private val webClientId = "122734914182-vioeetcrl9k7kmrks3sm2v1n1htplcfn.apps.googleusercontent.com" // Pastikan ini benar
    private val TOKEN_REFRESH_INTERVAL_SECONDS = 55L

    // LiveData & StateFlow untuk data utama
    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers: LiveData<List<User>> = _allUsers

    // State untuk UI (Loading dan Error)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // State untuk Proses Pembayaran Duitku

    // State untuk QR Code Dinamis
    private val _dynamicQrToken = MutableStateFlow<String?>(null)
    val dynamicQrToken: StateFlow<String?> = _dynamicQrToken.asStateFlow()

    // Listeners & Jobs
    private var allUsersListener: ListenerRegistration? = null
    private var userDataListener: ListenerRegistration? = null
    private var tokenRefreshJob: Job? = null

    private val _paymentUrl = MutableLiveData<String?>()
    val paymentUrl: LiveData<String?> = _paymentUrl

    init {
        // Konfigurasi Google Sign-In Client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(application, gso)

        // Listener utama yang bereaksi terhadap perubahan status login/logout
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.postValue(user)
            if (user != null) {
                // Jika user login, mulai dengarkan perubahan datanya secara real-time
                listenToUserData(user.uid)
            } else {
                // Jika user logout, hentikan semua listener dan job
                stopListeningToUserData()
                stopListeningForAllUsers()
                stopDynamicQrTokenUpdates()
                _userData.postValue(null)
                _allUsers.postValue(listOf())
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Pastikan semua listener dan job dihentikan saat ViewModel hancur
        stopListeningToUserData()
        stopListeningForAllUsers()
        stopDynamicQrTokenUpdates()
    }

    // --- FUNGSI UTILITAS UI STATE ---
    fun clearError() {
        _error.value = null
    }

    fun clearPaymentUrl() {
        _paymentUrl.value = null
    }

    // --- FUNGSI AUTENTIKASI & MANAJEMEN USER ---

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
                        role = "user",

                    )
                    saveUserToFirestore(newUser)
                    // Listener AuthState akan menangani pembaruan _currentUser dan _userData
                } else {
                    _error.value = "Gagal membuat pengguna."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Terjadi kesalahan saat pendaftaran."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithEmailPassword(email: String, pass: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                // Listener AuthState akan menangani sisanya (fetch data, dll)
            } catch (e: Exception) {
                _error.value = e.message ?: "Terjadi kesalahan saat login."
            } finally {
                _isLoading.value = false
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
                    }
                    // Listener AuthState akan menangani sisanya
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Terjadi kesalahan saat login dengan Google."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
        // Listener AuthState akan menangani pembersihan data
    }

    // --- FUNGSI MANAJEMEN DATA (ADMIN & USER) ---

    private suspend fun saveUserToFirestore(user: User) {
        withContext(Dispatchers.IO) {
            try {
                db.collection("users").document(user.uid)
                    .set(user, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                _error.value = "Gagal menyimpan data pengguna ke Firestore: ${e.message}"
            }
        }
    }

    fun updateUserProfile(uid: String, newDisplayName: String?, newRole: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) {
            if (userData.value?.role != "administrator") {
                _error.value = "Anda tidak memiliki hak untuk mengubah profil pengguna lain."
                _isLoading.value = false
                return@launch
            }

            try {
                val updates = mutableMapOf<String, Any>()
                newDisplayName?.let { updates["displayName"] = it }
                updates["role"] = newRole
                db.collection("users").document(uid).update(updates).await()
                // Listener real-time akan memperbarui data secara otomatis
            } catch (e: Exception) {
                _error.value = "Gagal mengubah profil pengguna: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteUser(uid: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) {
            if (userData.value?.role != "administrator") {
                _error.value = "Anda tidak memiliki hak untuk menghapus pengguna."
                _isLoading.value = false
                return@launch
            }
            try {
                // Note: Menghapus user dari Firestore tidak menghapusnya dari Auth.
                // Untuk fungsionalitas penuh, diperlukan Cloud Function.
                db.collection("users").document(uid).delete().await()
            } catch (e: Exception) {
                _error.value = "Gagal menghapus pengguna: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createUserWithEmailAndPassword(email: String, pass: String, displayName: String?, role: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) {
            if (userData.value?.role != "administrator") {
                _error.value = "Anda tidak memiliki hak untuk membuat pengguna baru."
                _isLoading.value = false
                return@launch
            }
            try {
                // Ini adalah contoh untuk admin membuat user, namun idealnya ini dilakukan
                // di backend/cloud function untuk keamanan.
                // Kode ini akan membuat user di Firebase Auth, namun user tersebut
                // tidak akan otomatis login.
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
                    _error.value = "Gagal membuat pengguna baru."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Terjadi kesalahan saat membuat akun baru."
            } finally {
                _isLoading.value = false
            }
        }
    }


    // --- FUNGSI REAL-TIME LISTENER ---

    private fun listenToUserData(uid: String) {
        stopListeningToUserData() // Hentikan listener lama jika ada
        _isLoading.value = true
        userDataListener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    Log.w("AuthViewModel", "Gagal mendengarkan data user.", e)
                    _error.value = "Gagal memuat data profil."
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    _userData.postValue(user)
                    // Jika user adalah admin, mulai dengarkan data semua pengguna
                    if (user?.role == "administrator") {
                        startListeningForAllUsers()
                    } else {
                        stopListeningForAllUsers()
                    }
                } else {
                    Log.d("AuthViewModel", "Data user belum ada di Firestore untuk UID: $uid")
                    // Bisa ditambahkan logika untuk membuat profil user jika belum ada
                }
            }
    }

    private fun stopListeningToUserData() {
        userDataListener?.remove()
        userDataListener = null
    }

    private fun startListeningForAllUsers() {
        if (allUsersListener != null) return // Sudah berjalan
        _isLoading.value = true
        allUsersListener = db.collection("users")
            .addSnapshotListener { snapshots, e ->
                _isLoading.value = false
                if (e != null) {
                    _error.value = "Gagal memuat daftar pengguna: ${e.message}"
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
        _allUsers.postValue(listOf()) // Kosongkan list saat berhenti listen
    }

    // --- FUNGSI TOP UP / PEMBAYARAN ---

    fun requestTopUpPayment(amount: Int, paymentMethod: String) {

        // Buat data yang diperlukan
        val data = hashMapOf(
            "paymentAmount" to amount,
            "productDetails" to "Top Up Saldo FastPark",
            "paymentMethod" to paymentMethod  // Ganti dengan metode bayar pilihan user
        )

        // Panggil fungsi dan tangani hasilnya HANYA di dalam listener
        functions
            .getHttpsCallable("createDuitkuTransaction")
            .call(data)
            .addOnSuccessListener { result ->
                val url = (result.data as? Map<*, *>)?.get("paymentUrl") as? String
                // Jika sukses, post nilainya ke LiveData
                _paymentUrl.postValue(url)
            }
            .addOnFailureListener { e ->
                // Jika gagal, post null ke LiveData agar UI tahu ada error
                _paymentUrl.postValue(null)
            }

    }

    fun onNavigationDone() {
        _paymentUrl.value = null
    }


    // --- FUNGSI QR CODE DINAMIS ---

    fun startDynamicQrTokenUpdates() {
        if (tokenRefreshJob?.isActive == true) {
            Log.d("AuthViewModel", "Pembaruan token QR dinamis sudah berjalan.")
            return
        }
        if (auth.currentUser == null) {
            _error.value = "Anda harus login untuk melihat QR Code."
            return
        }

        tokenRefreshJob = viewModelScope.launch {
            Log.d("AuthViewModel", "Memulai job pembaruan token QR Dinamis untuk user: ${auth.currentUser?.uid}")
            while (isActive) {
                fetchNewTokenFromServer()
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
            _error.value = "Sesi tidak valid. Silakan coba login kembali."
            stopDynamicQrTokenUpdates()
            return
        }

        var idToken: String? = null
        try {
            idToken = firebaseUser.getIdToken(true).await().token
            if (idToken.isNullOrEmpty()) {
                _error.value = "Gagal mendapatkan token otentikasi."
                return
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Pengecualian saat mengambil ID Token", e)
            _error.value = "Gagal memvalidasi sesi Anda."
            return
        }

        val functionUrl = "https://generatedynamicqrtoken-tmasj2diia-uc.a.run.app"
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val emptyRequestBody = "".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url(functionUrl)
                    .header("Authorization", "Bearer $idToken")
                    .post(emptyRequestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBodyString = response.body?.string()

                if (response.isSuccessful && responseBodyString != null) {
                    val jsonResponse = JSONObject(responseBodyString)
                    val token = jsonResponse.optString("token")
                    if (token.isNotEmpty()) {
                        _dynamicQrToken.value = token
                        _error.value = null // Bersihkan error jika sukses
                    } else {
                        _error.value = "Gagal memproses data QR dari server."
                    }
                } else {
                    val serverErrorMessage = try {
                        JSONObject(responseBodyString ?: "{}").optString("message", "Error dari server (Kode: ${response.code}).")
                    } catch (e: Exception) { "Error dari server (Kode: ${response.code})." }
                    _error.value = serverErrorMessage
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching QR token", e)
                _error.value = "Terjadi kesalahan jaringan saat memuat QR Code."
            }
        }
    }
}