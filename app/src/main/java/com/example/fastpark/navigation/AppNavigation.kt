package com.example.fastpark.navigation

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fastpark.auth.SignInScreen
import com.example.fastpark.auth.SignUpScreen
import com.example.fastpark.screens.admin.AdminDashboardScreen
import com.example.fastpark.screens.components.SettingScreen.AboutAppScreen
import com.example.fastpark.screens.components.SettingScreen.AccountSecurityScreen
import com.example.fastpark.screens.components.SettingScreen.EditProfileScreen
import com.example.fastpark.screens.users.MainUser
import com.example.fastpark.screens.components.SettingScreen.NotificationSettingsScreen
import com.example.fastpark.screens.users.UserSettingScreen
import com.example.fastpark.screens.users.UserShowQrScreen
import com.example.fastpark.screens.workers.WorkerDashboardScreen
import com.example.fastpark.viewmodel.AuthViewModel
import com.example.fastpark.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch


object AppDestinations {
    const val LOGIN_ROUTE = "signin"
    const val SIGNUP_ROUTE = "signup"

    const val USER_HOME_ROUTE = "user_home"
    const val USER_SETTINGS_ROUTE = "user_settings"
    const val USER_SHOW_QR_ROUTE = "user_show_qr"
    const val EDIT_PROFILE_ROUTE = "edit_profile"
    const val NOTIFICATION_SETTINGS_ROUTE = "notification_settings"
    const val ACCOUNT_SECURITY_ROUTE = "account_security"
    const val ABOUT_APP_ROUTE = "about_app"

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

    // Amati state dari ViewModel untuk menampilkan dialog
    val showPlateDialog by authViewModel.googleSignInRequiresProfileCompletion.collectAsState()
    val currentUserSnapshot by authViewModel.currentUser.observeAsState()
    val userDataSnapshot by authViewModel.userData.observeAsState()

    // Tampilkan dialog jika state-nya true.
    // Dialog ini akan muncul di atas layar manapun.
    if (showPlateDialog) {
        PlateNumberEntryDialog(
            authViewModel = authViewModel,
            onDismiss = {
                // Jika pengguna batal, reset state dan kembali ke halaman login
                authViewModel.resetGoogleSignUpCompletionState()
                navController.navigate(AppDestinations.LOGIN_ROUTE) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            },
            onConfirm = {
                // Jika konfirmasi, cukup reset state.
                // LaunchedEffect akan menangani navigasi ke halaman yang benar.
                authViewModel.resetGoogleSignUpCompletionState()
            }
        )
    }

    // LaunchedEffect ini menjadi pusat kontrol navigasi otomatis
    LaunchedEffect(currentUserSnapshot, userDataSnapshot, showPlateDialog) {
        // Jangan lakukan navigasi apapun jika dialog sedang ditampilkan
        if (showPlateDialog) return@LaunchedEffect

        val currentUser = currentUserSnapshot
        val userData = userDataSnapshot

        if (currentUser != null && userData != null) {
            // Jika user sudah login dan datanya lengkap, arahkan sesuai role
            val currentRoute = navController.currentDestination?.route
            val targetRoute = when (userData.role) {
                "administrator" -> AppDestinations.ADMIN_HOME_ROUTE
                "worker" -> AppDestinations.WORKER_HOME_ROUTE
                else -> AppDestinations.USER_HOME_ROUTE
            }

            if (currentRoute != targetRoute) {
                navController.navigate(targetRoute) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        } else if (currentUser == null) {
            // Jika user tidak login, pastikan berada di halaman login/signup
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != AppDestinations.LOGIN_ROUTE && currentRoute != AppDestinations.SIGNUP_ROUTE) {
                navController.navigate(AppDestinations.LOGIN_ROUTE) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN_ROUTE // Selalu mulai dari login
    ) {
        composable(AppDestinations.LOGIN_ROUTE) {
            SignInScreen(
                authViewModel = authViewModel,
                onSignInSuccess = {}, // Dikosongkan, ditangani LaunchedEffect
                onNavigateToSignUp = { navController.navigate(AppDestinations.SIGNUP_ROUTE) },
                navController = navController
            )
        }
        composable(AppDestinations.SIGNUP_ROUTE) {
            SignUpScreen(
                authViewModel = authViewModel,
                onSignUpSuccess = {}, // Dikosongkan, ditangani LaunchedEffect
                onNavigateToLogin = {
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(AppDestinations.SIGNUP_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                navController = navController
            )
        }

        // --- Rute Pengguna (User) ---
        composable(AppDestinations.USER_HOME_ROUTE) {
            UserHomeScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(AppDestinations.USER_SETTINGS_ROUTE) {
            UserSettingScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(AppDestinations.USER_SHOW_QR_ROUTE) {
            UserShowQrScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(AppDestinations.EDIT_PROFILE_ROUTE) {
            EditProfileScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(AppDestinations.ABOUT_APP_ROUTE) {
            AboutAppScreen(navController = navController)
        }
        composable(AppDestinations.ACCOUNT_SECURITY_ROUTE) {
            AccountSecurityScreen(navController = navController)
        }
        composable(AppDestinations.NOTIFICATION_SETTINGS_ROUTE) {
            NotificationSettingsScreen(navController = navController)
        }

        // --- Rute Pekerja (Worker) ---
        composable(AppDestinations.WORKER_HOME_ROUTE) {
            WorkerHomeScreen(navController = navController, authViewModel = authViewModel)
        }

        // --- Rute Admin ---
        composable(AppDestinations.ADMIN_HOME_ROUTE) {
            AdminHomeScreen(navController = navController, authViewModel = authViewModel)
        }
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

@Composable
fun PlateNumberEntryDialog(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var noPlat by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = {
            // Pengguna tidak bisa menutup dialog ini tanpa aksi
        },
        title = { Text("Satu Langkah Lagi!") },
        text = {
            Column {
                Text("Untuk menyelesaikan pendaftaran, silakan masukkan nomor plat kendaraan Anda.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = noPlat,
                    onValueChange = { noPlat = it.uppercase(); errorText = null },
                    label = { Text("Nomor Plat Kendaraan") },
                    isError = errorText != null
                )
                errorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (noPlat.isBlank()) {
                        errorText = "Nomor plat tidak boleh kosong."
                    } else {
                        // Panggil fungsi ViewModel untuk menyimpan data
                        authViewModel.completeGoogleSignUp(noPlat)
                        onConfirm() // Panggil callback untuk menutup dialog
                    }
                }
            ) {
                Text("Simpan & Masuk")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    authViewModel.signOut() // Logout pengguna jika mereka membatalkan
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Batal")
            }
        }
    )
}