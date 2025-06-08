@file:Suppress("DEPRECATION")

package com.example.fastpark.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
//import com.example.fastpark.data.ParkingSlot // Make sure you have this data class
import com.example.fastpark.data.User
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Suppress("DEPRECATION")
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val oneTapClient = Identity.getSignInClient(application)
    private val webClientId = "122734914182-vioeetcrl9k7kmrks3sm2v1n1htplcfn.apps.googleusercontent.com"

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

    // --- LiveData for other screens ---
    //private val _parkingSlots = MutableLiveData<List<ParkingSlot>>()
    //val parkingSlots: LiveData<List<ParkingSlot>> = _parkingSlots

    private var allUsersListener: ListenerRegistration? = null

    init {
        _currentUser.value = auth.currentUser
        if (_currentUser.value != null) {
            viewModelScope.launch {
                fetchUserData(_currentUser.value!!.uid)
            }
            startListeningForAllUsers()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningForAllUsers()
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

    suspend fun getGoogleSignInRequest(): IntentSenderRequest? {
        return try {
            val result = oneTapClient.beginSignIn(
                BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId(webClientId)
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .setAutoSelectEnabled(false)
                    .build()
            ).await()
            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Google Sign-In request failed", e)
            _error.postValue("Google Sign-In failed: ${e.message}")
            null
        }
    }

    fun signInWithGoogleIntent(intent: Intent?) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(intent)
                val googleIdToken = credential.googleIdToken
                if (googleIdToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    signInWithGoogleCredential(firebaseCredential)
                } else {
                    _error.postValue("Google ID Token not found.")
                }
            } catch (e: Exception) {
                _error.postValue("Google Sign-In failed: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private suspend fun signInWithGoogleCredential(credential: AuthCredential) {
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
            Log.e("AuthViewModel", "Google SignIn Error: ${e.message}", e)
        }
    }

    private suspend fun saveUserToFirestore(user: User) {
        try {
            db.collection("users").document(user.uid).set(user, SetOptions.merge()).await()
        } catch (e: Exception) {
            _error.postValue("Gagal menyimpan data pengguna ke Firestore: ${e.message}")
        }
    }

    private suspend fun fetchUserData(uid: String) {
        _isLoading.postValue(true)
        try {
            val document = db.collection("users").document(uid).get().await()
            if (document.exists()) {
                _userData.postValue(document.toObject(User::class.java))
            } else {
                val firebaseUser = auth.currentUser
                if (firebaseUser != null && firebaseUser.uid == uid) {
                    val newUser = User(uid = firebaseUser.uid, email = firebaseUser.email, displayName = firebaseUser.displayName, role = "user")
                    saveUserToFirestore(newUser)
                    _userData.postValue(newUser)
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
            try {
                db.collection("users").document(uid).delete().await()
                // Deleting from Firebase Auth requires a Cloud Function for security reasons.
                // The client SDK cannot delete other users.
            } catch (e: Exception) {
                _error.postValue("Gagal menghapus pengguna: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun signOut() {
        auth.signOut()
        oneTapClient.signOut()
        _currentUser.value = null
        _userData.value = null
        _error.value = null
        stopListeningForAllUsers()
    }

    // --- Functions for other admin screens ---
    //fun loadParkingSlots() {
      //  viewModelScope.launch {
        //    // TODO: Implement logic to load parking slots from Firestore
          //  // For now, returning a dummy list.
            //_parkingSlots.postValue(listOf(
              //  ParkingSlot("P001", "Area A - Lantai 1", "Tersedia", 50),
                //ParkingSlot("P002", "Area B - Lantai 1", "Penuh", 40)
           // ))
        //}
   // }
}