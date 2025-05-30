package com.example.fastpark.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.fastpark.screens.components.BottomBar
import com.example.fastpark.screens.components.MainPage
import com.example.fastpark.screens.workers.HomeScreen
import com.example.fastpark.screens.workers.ScanScreen
import com.example.fastpark.screens.workers.SettingsScreen
import androidx.navigation.NavHostController
import com.example.fastpark.viewmodel.AuthViewModel

@Composable
fun WorkerDashboardScreen(
    navController: NavHostController, // TAMBAHKAN PARAMETER
    authViewModel: AuthViewModel      // TAMBAHKAN PARAMETER
) {
    var selectedPage by remember { mutableStateOf(MainPage.HOME) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when (selectedPage) {
                    // Anda mungkin perlu meneruskan navController dan/atau authViewModel
                    // ke layar-layar ini jika mereka membutuhkannya
                    MainPage.HOME     -> HomeScreen(/* navController, authViewModel */)
                    MainPage.SCAN     -> ScanScreen(/* navController, authViewModel */)
                    MainPage.SETTINGS -> SettingsScreen(/* navController, authViewModel */)
                }
            }
            BottomBar(selectedPage = selectedPage) { selectedPage = it }
        }
    }
}