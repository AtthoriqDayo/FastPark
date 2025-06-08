// SignUp.kt
@file:Suppress("DEPRECATION")

package com.example.fastpark.auth

import android.app.Activity.RESULT_OK
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.fastpark.R
import com.example.fastpark.screens.theme.BrightRed
import com.example.fastpark.screens.theme.DeepRed
import com.example.fastpark.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val errorMessage by authViewModel.error.observeAsState()
    val isLoading by authViewModel.isLoading.observeAsState(false)
    val currentUser by authViewModel.currentUser.observeAsState()

    val scope = rememberCoroutineScope()

    // --- FIX 1: This launcher now calls the correct PUBLIC function on the ViewModel ---
    val googleSignUpLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Call the correct public function. The ViewModel handles the internal logic.
            authViewModel.signInWithGoogleIntent(result.data)
        }
    }

    // --- FIX 2: This effect handles navigation safely after the user is confirmed. ---
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onSignUpSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepRed, BrightRed)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 200.dp)
                .background(Color.White, shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading == true) {
                CircularProgressIndicator()
            } else {
                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(100.dp))
                Text("Sign Up", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; authViewModel.clearError() },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; authViewModel.clearError() },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; authViewModel.clearError() },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; authViewModel.clearError() },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Client-side validation is good practice
                        if (username.isBlank() || email.isBlank() || password.isBlank()) {
                            // You might want to show a more specific error message here
                            return@Button
                        }
                        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            // Show invalid email error
                            return@Button
                        }
                        if (password != confirmPassword) {
                            // Show password mismatch error
                            return@Button
                        }

                        authViewModel.signUpWithEmailPassword(email, password, username)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Sign Up")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val signInRequest = authViewModel.getGoogleSignInRequest()
                            signInRequest?.let {
                                googleSignUpLauncher.launch(it)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Icon(painterResource(id = R.drawable.google), contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Continue with Google")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row {
                    Text("Sudah punya akun? ")
                    Text(
                        "Login",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }
    }
}