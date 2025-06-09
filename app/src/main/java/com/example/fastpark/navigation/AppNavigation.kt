package com.example.fastpark.navigation

import android.app.Application
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fastpark.auth.SignInScreen
import com.example.fastpark.auth.SignUpScreen
import com.example.fastpark.data.User
import com.example.fastpark.screens.admin.AdminDashboardScreen
import com.example.fastpark.screens.users.MainUser
import com.example.fastpark.screens.users.UserSettingScreen
import com.example.fastpark.screens.users.UserShowQrScreen
import com.example.fastpark.screens.workers.WorkerDashboardScreen
import com.example.fastpark.viewmodel.AuthViewModel
import com.example.fastpark.viewmodel.AuthViewModelFactory
import com.google.firebase.auth.FirebaseUser

object AppDestinations {
    const val LOGIN_ROUTE = "signin"
    const val SIGNUP_ROUTE = "signup"
    const val USER_HOME_ROUTE = "user_home"
    const val USER_SETTINGS_ROUTE = "user_settings"
    const val USER_SHOW_QR_ROUTE = "user_show_qr"
    const val WORKER_HOME_ROUTE = "worker_home"
    const val ADMIN_HOME_ROUTE = "admin_home"
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(application)
    )


    val currentUserSnapshot by authViewModel.currentUser.observeAsState()
    val userDataSnapshot by authViewModel.userData.observeAsState()
    val isLoading by authViewModel.isLoading.observeAsState(initial = false) // Amati loading state

    LaunchedEffect(currentUserSnapshot, userDataSnapshot, isLoading) {
        val currentUser = currentUserSnapshot
        val userData = userDataSnapshot

        if (currentUser != null) {
            if (userData == null && !isLoading) {
                Log.w("AppNavigation", "currentUser exists but userData is null and not loading. Waiting for data...")
                // Mungkin perlu menambahkan timeout atau indikator loading di sini
                return@LaunchedEffect // Jangan lanjutkan navigasi sampai userData ada
            } else if (userData != null) { // Jika userData sudah tersedia
                val currentRoute = navController.currentDestination?.route
                val targetRoute = when (userData.role) {
                    "administrator" -> AppDestinations.ADMIN_HOME_ROUTE
                    "worker" -> AppDestinations.WORKER_HOME_ROUTE
                    else -> AppDestinations.USER_HOME_ROUTE
                }
                if (currentRoute != targetRoute) {
                    navController.navigate(targetRoute) {
                        popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        } else {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != AppDestinations.LOGIN_ROUTE && currentRoute != AppDestinations.SIGNUP_ROUTE) {
                navController.navigate(AppDestinations.LOGIN_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = determineStartDestination(
            currentUser = currentUserSnapshot,
            userProfileData = userDataSnapshot,
            isLoading = isLoading
        )
    ) {
        composable(AppDestinations.LOGIN_ROUTE) {
            SignInScreen(
                authViewModel = authViewModel,
                onSignInSuccess = {},
                onNavigateToSignUp = { navController.navigate(AppDestinations.SIGNUP_ROUTE) },
                navController = navController
            )
        }
        composable(AppDestinations.SIGNUP_ROUTE) {
            SignUpScreen(
                authViewModel = authViewModel,
                onSignUpSuccess = {},
                onNavigateToLogin = {
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(AppDestinations.SIGNUP_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                navController = navController
            )
        }

        composable(AppDestinations.USER_SETTINGS_ROUTE) {
            UserSettingScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(AppDestinations.USER_SHOW_QR_ROUTE) {
            UserShowQrScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(AppDestinations.USER_HOME_ROUTE) {
            UserHomeScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(AppDestinations.WORKER_HOME_ROUTE) {
            WorkerHomeScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(AppDestinations.ADMIN_HOME_ROUTE) {
            AdminHomeScreen(navController = navController, authViewModel = authViewModel)

        }
    }
}

@Composable
private fun determineStartDestination(
    currentUser: FirebaseUser?,
    userProfileData: User?,
    isLoading: Boolean
): String {
    if (currentUser != null && (userProfileData == null || isLoading)) {
        return AppDestinations.LOGIN_ROUTE // Atau rute loading jika ada
    }

    return if (currentUser != null) {
        when (userProfileData?.role) {
            "administrator" -> AppDestinations.ADMIN_HOME_ROUTE
            "worker" -> AppDestinations.WORKER_HOME_ROUTE
            "user" -> AppDestinations.USER_HOME_ROUTE
            else -> AppDestinations.LOGIN_ROUTE //
        }
    } else {
        AppDestinations.LOGIN_ROUTE
    }
}

@Composable
fun UserHomeScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    MainUser(navController = navController, authViewModel = authViewModel)
}

@Composable
fun WorkerHomeScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    WorkerDashboardScreen(navController = navController, authViewModel = authViewModel)
}

@Composable
fun AdminHomeScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    AdminDashboardScreen(navController = navController, authViewModel = authViewModel)
}