package com.example.fastpark.viewmodel

import com.example.fastpark.data.ParkingSession

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val sessions: List<ParkingSession>) : HistoryUiState()
    object Empty : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}