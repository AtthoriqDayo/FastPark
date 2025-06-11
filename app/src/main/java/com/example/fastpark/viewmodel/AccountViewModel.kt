package com.example.fastpark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastpark.data.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// Data class untuk menampung hasil statistik
data class AccountStats(
    val totalUsers: Int = 0,
    val newUsersThisMonth: Int = 0,
    val userGrowthByMonth: Map<String, Int> = emptyMap()
)

class AccountViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _accountStats = MutableStateFlow<AccountStats?>(null)
    val accountStats: StateFlow<AccountStats?> = _accountStats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchAccountStats()
    }

    private fun fetchAccountStats() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users").get().await()
                val users = snapshot.toObjects(User::class.java)

                // Hitung total pengguna
                val totalUsers = users.size

                // Hitung pengguna baru bulan ini
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, 1) // Set ke awal bulan ini
                val startOfMonth = calendar.time
                val newUsersThisMonth = users.count {
                    it.createdAt != null && it.createdAt.toDate().after(startOfMonth)
                }

                // Kelompokkan pertumbuhan pengguna per bulan
                val monthFormat = SimpleDateFormat("MMM ''yy", Locale("id", "ID"))
                val userGrowthByMonth = users
                    .filter { it.createdAt != null }
                    .groupBy { monthFormat.format(it.createdAt!!.toDate()) }
                    .mapValues { it.value.size }

                _accountStats.value = AccountStats(
                    totalUsers = totalUsers,
                    newUsersThisMonth = newUsersThisMonth,
                    userGrowthByMonth = userGrowthByMonth
                )

            } catch (e: Exception) {
                _accountStats.value = AccountStats() // Reset data on error
            } finally {
                _isLoading.value = false
            }
        }
    }
}