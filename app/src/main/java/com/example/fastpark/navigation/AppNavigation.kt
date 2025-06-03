// File: AppNavigation.kt
package com.example.fastpark.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fastpark.auth.SignInScreen
import com.example.fastpark.auth.SignUpScreen
import com.example.fastpark.data.User
import com.example.fastpark.screens.workers.WorkerDashboardScreen
import com.example.fastpark.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseUser


object AppDestinations {
    const val LOGIN_ROUTE = "signin"
    const val SIGNUP_ROUTE = "signup"
    const val USER_HOME_ROUTE = "user_home"
    const val WORKER_HOME_ROUTE = "worker_home"
    const val ADMIN_HOME_ROUTE = "admin_home"
}

@Composable
fun MainScreen(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val currentUserSnapshot by authViewModel.currentUser.observeAsState()
    val userDataSnapshot by authViewModel.userData.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(currentUserSnapshot, userDataSnapshot, navController.currentBackStackEntry) {
        val currentUser = currentUserSnapshot
        val userData = userDataSnapshot

        if (currentUser != null) {
            if (userData == null || userData.uid != currentUser.uid) {
                authViewModel.fetchUserData(currentUser.uid)
            }

            userData?.let { userProfile ->
                val currentRoute = navController.currentDestination?.route
                val targetRoute = when (userProfile.role) {
                    "administrator" -> AppDestinations.ADMIN_HOME_ROUTE
                    "worker" -> AppDestinations.WORKER_HOME_ROUTE
                    else -> AppDestinations.USER_HOME_ROUTE
                }
                if (currentRoute != targetRoute) {
                    navController.navigate(targetRoute) {
                        popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                    }
                }
            }
        } else {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != AppDestinations.LOGIN_ROUTE && currentRoute != AppDestinations.SIGNUP_ROUTE) {
                navController.navigate(AppDestinations.LOGIN_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = determineStartDestination(
            currentUser = currentUserSnapshot,
            userProfileData = userDataSnapshot
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
                    }
                },
                navController = navController
            )
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
    userProfileData: User?
): String {
    return if (currentUser != null) {
        when (userProfileData?.role) {
            "administrator" -> AppDestinations.ADMIN_HOME_ROUTE
            "worker" -> AppDestinations.WORKER_HOME_ROUTE
            "user" -> AppDestinations.USER_HOME_ROUTE
            else -> AppDestinations.LOGIN_ROUTE
        }
    } else {
        AppDestinations.LOGIN_ROUTE
    }
}

@Composable
fun UserHomeScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    WorkerDashboardScreen(navController = navController, authViewModel = authViewModel)

}

@Composable
fun WorkerHomeScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val userData by authViewModel.userData.observeAsState()
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Selamat Datang, Worker ${userData?.displayName ?: ""}!")
        Text("Role Anda: ${userData?.role ?: "Memuat..."}")
        Spacer(modifier = Modifier.height(16.dp))

    }
}

@Composable
fun AdminHomeScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val userData by authViewModel.userData.observeAsState()
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Selamat Datang, Admin ${userData?.displayName ?: ""}!")
        Text("Role Anda: ${userData?.role ?: "Memuat..."}")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            authViewModel.signOut()
        }) {
            Text("Logout")
        }
    }
}
