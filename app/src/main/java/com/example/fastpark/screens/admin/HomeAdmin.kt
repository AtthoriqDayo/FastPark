package com.example.fastpark.screens.admin
// Import AdminBottomBar dan AdminMainPage dari lokasi baru mereka
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.fastpark.screens.admin.AdminNavGraph
import com.example.fastpark.screens.components.AdminBottomBar
import com.example.fastpark.screens.components.AdminMainPage
import com.example.fastpark.viewmodel.AuthViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val adminNavController = rememberNavController() // NavController internal untuk navigasi BottomBar
    var selectedAdminPage by remember { mutableStateOf(AdminMainPage.ACCOUNT_MANAGEMENT) } // State untuk BottomBar

    Scaffold(
        bottomBar = {
            AdminBottomBar( // Gunakan AdminBottomBar yang baru
                selectedAdminPage = selectedAdminPage,
                onPageSelected = { newAdminPage ->
                    selectedAdminPage = newAdminPage // Update state halaman yang dipilih
                    adminNavController.navigate(newAdminPage.route) { // Navigasi NavController internal
                        // Pastikan hanya satu instance halaman di back stack
                        popUpTo(adminNavController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White) // Background untuk konten utama
        ) {
            AdminNavGraph(navController = adminNavController, authViewModel = authViewModel)
        }
    }
}