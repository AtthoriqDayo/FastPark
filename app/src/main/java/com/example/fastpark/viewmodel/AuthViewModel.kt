package com.example.fastpark.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastpark.data.model.AppUser
import com.example.fastpark.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: AppUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val repo = AuthRepository()
    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun signUp(email: String, pass: String, username: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            repo.signUp(email, pass, username)
                .onSuccess { _state.value = AuthState.Success(it) }
                .onFailure { _state.value = AuthState.Error(it.message ?: "Error") }
        }
    }

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            repo.signIn(email, pass)
                .onSuccess { _state.value = AuthState.Success(it) }
                .onFailure { _state.value = AuthState.Error(it.message ?: "Error") }
        }
    }
}
