// SignUp.kt
@file:Suppress("DEPRECATION")

package com.example.fastpark.auth

import android.app.Activity.RESULT_OK
import android.util.Log
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.fastpark.R
import com.example.fastpark.screens.theme.BrightRed
import com.example.fastpark.screens.theme.DeepRed
import com.example.fastpark.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


private const val YOUR_SIGNUP_WEB_CLIENT_ID = "122734914182-vioeetcrl9k7kmrks3sm2v1n1htplcfn.apps.googleusercontent.com"

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
    var noPlat by remember { mutableStateOf("") }


    val errorMessage by authViewModel.error.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val oneTapClient = remember { Identity.getSignInClient(context) }


    val googleSignUpLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val googleIdToken = credential.googleIdToken
                if (googleIdToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    coroutineScope.launch {
                        authViewModel.signInWithGoogleCredential(firebaseCredential)
                        onSignUpSuccess()
                    }
                }
            } catch (e: Exception) {
                Log.e("SignUp", "Google Sign-Up Error", e)
            }
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            OutlinedTextField(
                value = noPlat,
                onValueChange = { noPlat = it; authViewModel.clearError() },
                label = { Text("Nomor Plat Kendaraan") },
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() || noPlat.isBlank()) {
                        authViewModel.clearError()
                        return@Button
                    }
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        authViewModel.clearError()
                        return@Button
                    }
                    if (password.length < 6) {
                        authViewModel.clearError()
                        return@Button
                    }
                    if (password != confirmPassword) {
                        authViewModel.clearError()
                        return@Button
                    }

                    coroutineScope.launch {
                        authViewModel.signUpWithEmailPassword(email, password, username, noPlat)
                        if (authViewModel.error.value == null) {
                            onSignUpSuccess()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Sign Up")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val signUpRequest = BeginSignInRequest.builder()
                        .setGoogleIdTokenRequestOptions(
                            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(YOUR_SIGNUP_WEB_CLIENT_ID)
                                .setFilterByAuthorizedAccounts(false)
                                .build()
                        )
                        .setAutoSelectEnabled(false)
                        .build()

                    coroutineScope.launch {
                        try {
                            val result = oneTapClient.beginSignIn(signUpRequest).await()
                            googleSignUpLauncher.launch(
                                IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                            )
                        } catch (e: Exception) {
                            Log.e("SignUp", "Google Sign-Up Init Failed", e)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
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

@Preview(showBackground = true)
@Composable
fun PreviewSignUp() {
    SignUpScreen(
        onSignUpSuccess = {},
        navController = rememberNavController(),
        authViewModel = TODO(),
        onNavigateToLogin = {}
    )
}
