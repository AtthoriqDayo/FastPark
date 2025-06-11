package com.example.fastpark.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastpark.data.ParkingSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FullHistoryViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Sealed interface untuk merepresentasikan state UI dengan lebih baik
    sealed interface HistoryUiState {
        object Loading : HistoryUiState
        data class Success(val sessions: List<ParkingSession>) : HistoryUiState
        object Empty : HistoryUiState
        data class Error(val message: String) : HistoryUiState
    }

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState

    init {
        fetchParkingHistory()
    }

    fun fetchParkingHistory() {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading

            try {
                val querySnapshot = firestore.collection("parking_sessions")
                    .whereEqualTo("status", "paid") // Hanya sesi yang selesai
                    .orderBy("exitTimestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    _uiState.value = HistoryUiState.Empty
                } else {
                    val sessions = querySnapshot.toObjects(ParkingSession::class.java)
                    _uiState.value = HistoryUiState.Success(sessions)
                }
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Error fetching parking history", e)
                _uiState.value = HistoryUiState.Error("Gagal memuat riwayat: ${e.localizedMessage}")
            }
        }
    }

}
