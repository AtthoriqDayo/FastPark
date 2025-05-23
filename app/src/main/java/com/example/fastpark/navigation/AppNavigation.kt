package com.example.fastpark.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fastpark.screens.MainScreen
import com.example.fastpark.screens.auth.SignInScreen
import com.example.fastpark.screens.auth.SignUpScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val isLoggedIn = remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn.value) "Main" else "login"
    ) {
        composable("login") {
            SignInScreen(
                onSignInSuccess = {
                    isLoggedIn.value = true
                    navController.navigate("Main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                navController = navController
            )
        }
        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = {
                    isLoggedIn.value = true
                    navController.navigate("Main") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                navController = navController
            )
        }
        composable("Main") {
            MainScreen()
        }
    }
}
