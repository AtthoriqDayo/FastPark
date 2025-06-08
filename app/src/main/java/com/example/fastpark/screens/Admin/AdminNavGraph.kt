package com.example.fastpark.screens.admin

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.fastpark.navigation.AdminDestinations
import com.example.fastpark.viewmodel.AuthViewModel

@Composable
fun AdminNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = AdminDestinations.ACCOUNT_MANAGEMENT_ROUTE
    ) {
        composable(AdminDestinations.ACCOUNT_MANAGEMENT_ROUTE) {
            AccountManagementScreen(authViewModel = authViewModel)
        }
        composable(AdminDestinations.FINANCIAL_STATS_ROUTE) {
            FinancialStatisticsScreen()
        }
        composable(AdminDestinations.CUSTOMER_STATS_ROUTE) {
            CustomerStatisticsScreen()
        }
        composable(AdminDestinations.PARKING_MANAGEMENT_ROUTE) {
            ParkingManagementScreen()
        }
    }
}