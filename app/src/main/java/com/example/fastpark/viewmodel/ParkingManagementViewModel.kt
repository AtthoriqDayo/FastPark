package com.example.fastpark.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastpark.data.ParkingSession
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ParkingManagementViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val _allActiveSessions = MutableStateFlow<List<ParkingSession>>(emptyList())

    // State untuk UI, menampung daftar yang sudah difilter
    private val _uiState = MutableStateFlow<ParkingManagementUiState>(ParkingManagementUiState.Loading)
    val uiState: StateFlow<ParkingManagementUiState> = _uiState

    // State untuk query pencarian
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // State untuk dialog detail
    private val _selectedSession = MutableStateFlow<ParkingSession?>(null)
    val selectedSession: StateFlow<ParkingSession?> = _selectedSession

    init {
        fetchActiveSessions()

        // Kombinasikan daftar sesi dengan query pencarian
        viewModelScope.launch {
            combine(_allActiveSessions, _searchQuery) { sessions, query ->
                if (query.isBlank()) {
                    sessions
                } else {
                    sessions.filter {
                        it.uid.contains(query, ignoreCase = true) ||
                                it.parkingArea.contains(query, ignoreCase = true)
                    }
                }
            }.collect { filteredSessions ->
                if (filteredSessions.isEmpty() && _searchQuery.value.isBlank()) {
                    _uiState.value = ParkingManagementUiState.Empty
                } else {
                    _uiState.value = ParkingManagementUiState.Success(filteredSessions)
                }
            }
        }
    }

    private fun fetchActiveSessions() {
        viewModelScope.launch {
            try {
                // Listener real-time untuk data parkir aktif
                firestore.collection("parking_sessions")
                    .whereEqualTo("status", "active")
                    .orderBy("entryTimestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("ParkingVM", "Listen failed.", error)
                            _uiState.value = ParkingManagementUiState.Error(error.message ?: "Gagal memuat data")
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            // --- UBAH BAGIAN INI ---
                            val sessions = snapshot.documents.mapNotNull { doc ->
                                // Ubah dokumen menjadi objek ParkingSession
                                val session = doc.toObject(ParkingSession::class.java)
                                // Isi sessionId dengan ID dari dokumen
                                session?.apply {
                                    sessionId = doc.id
                                }
                            } // filterNotNull untuk keamanan
                            _allActiveSessions.value = sessions
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = ParkingManagementUiState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    // Fungsi untuk mengelola state UI
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSessionSelected(session: ParkingSession) {
        _selectedSession.value = session
    }

    fun onDialogDismiss() {
        _selectedSession.value = null
    }
}

// Sealed class untuk merepresentasikan state UI
sealed class ParkingManagementUiState {
    object Loading : ParkingManagementUiState()
    data class Success(val sessions: List<ParkingSession>) : ParkingManagementUiState()
    object Empty : ParkingManagementUiState()
    data class Error(val message: String) : ParkingManagementUiState()
}