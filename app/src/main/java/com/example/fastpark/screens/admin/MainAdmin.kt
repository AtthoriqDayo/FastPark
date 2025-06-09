package com.example.fastpark.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.fastpark.screens.components.admin.AdminBottomBar
import com.example.fastpark.screens.components.admin.AdminMainPage
import com.example.fastpark.viewmodel.AuthViewModel

@Composable
fun AdminDashboardScreen(
    navController: NavHostController, // TAMBAHKAN PARAMETER
    authViewModel: AuthViewModel      // TAMBAHKAN PARAMETER
) {
    var selectedPage by remember { mutableStateOf(AdminMainPage.HOME_ADMIN) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when (selectedPage) {
                    AdminMainPage.HOME_ADMIN -> {
                        val userData by authViewModel.userData.observeAsState()
                        val userName = userData?.displayName ?: "User"
                        AdminScreen(userName = userName)
                    }
                    AdminMainPage.ACCOUNT_MANAGEMENT -> AccountManagementScreen(authViewModel)
                    AdminMainPage.SETTINGS -> SettingsAdminScreen(navController, authViewModel)
                }
            }
            AdminBottomBar(selectedPage = selectedPage) { newSelectedPage ->
                selectedPage = newSelectedPage
            }
        }
    }
}