package com.example.fastpark.screens.admin

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.fastpark.data.User
import com.example.fastpark.screens.theme.BrightRed
import com.example.fastpark.screens.theme.DeepRed
import com.example.fastpark.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManagementScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val users by authViewModel.allUsers.observeAsState(initial = emptyList())
    val error by authViewModel.error.observeAsState()
    val isLoading by authViewModel.isLoading.observeAsState(false)

    var showDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var dialogMode by remember { mutableStateOf<String>("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Definisikan gradien untuk TopAppBar
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(DeepRed, BrightRed)
    )

    LaunchedEffect(error) {
        error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = it,
                    actionLabel = "Tutup"
                )
                authViewModel.clearError()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // PERBAIKAN DI SINI: Ganti CenterAlignedTopAppBar menjadi TopAppBar
            TopAppBar(
                title = {
                    Text(
                        "Manajemen Akun",
                        // Tambahkan modifier fillMaxWidth dan textAlign.Start untuk rata kiri
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start, // <-- Perbaikan untuk rata kiri
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                modifier = Modifier.background(gradientBrush),
                colors = TopAppBarDefaults.topAppBarColors( // <-- Gunakan topAppBarColors
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    selectedUser = null
                    dialogMode = "create"
                    showDialog = true
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = DeepRed), // PERBAIKAN DI SINI
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Tambah Akun", tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Tambah Akun Baru", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = DeepRed
                )
                Text("Memuat data...", color = Color.Gray)
            } else if (users.isEmpty()) {
                Text(
                    "Tidak ada akun pengguna yang terdaftar.",
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(users) { user ->
                        UserAccountItem(
                            user = user,
                            onEdit = { editedUser ->
                                selectedUser = editedUser
                                dialogMode = "edit"
                                showDialog = true
                            },
                            onDelete = { deletedUser ->
                                userToDelete = deletedUser
                                showDeleteConfirmDialog = true
                            }
                        )
                        Divider(color = Color.LightGray, thickness = 1.dp)
                    }
                }
            }
        }
    }

    if (showDialog) {
        UserAccountDialog(
            user = selectedUser,
            mode = dialogMode,
            onDismiss = { showDialog = false },
            onConfirm = { userToProcess, password, role ->
                scope.launch {
                    if (dialogMode == "create") {
                        if (userToProcess.email.isNullOrEmpty() || password.isNullOrEmpty() || userToProcess.displayName.isNullOrEmpty()) {
                            snackbarHostState.showSnackbar(
                                message = "Email, password, dan nama lengkap tidak boleh kosong.",
                                actionLabel = "Tutup"
                            )
                        } else {
                            authViewModel.createUserWithEmailAndPassword(userToProcess.email, password, userToProcess.displayName, role)
                            showDialog = false
                        }
                    } else { // edit mode
                        if (userToProcess.displayName.isNullOrEmpty()) {
                            snackbarHostState.showSnackbar(
                                message = "Nama lengkap tidak boleh kosong.",
                                actionLabel = "Tutup"
                            )
                        } else {
                            authViewModel.updateUserProfile(userToProcess.uid, userToProcess.displayName, role)
                            showDialog = false
                        }
                    }
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Hapus Akun?") },
            text = { Text("Apakah Anda yakin ingin menghapus akun ${userToDelete?.displayName ?: "ini"}?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            userToDelete?.let {
                                authViewModel.deleteUser(it.uid)
                                Log.d("AccountManagement", "Menghapus user: ${it.displayName}")
                            }
                            showDeleteConfirmDialog = false
                            userToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightRed), // PERBAIKAN DI SINI
                    enabled = !isLoading
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        userToDelete = null
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray) // PERBAIKAN DI SINI
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun UserAccountItem(user: User, onEdit: (User) -> Unit, onDelete: (User) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Nama: ${user.displayName ?: "N/A"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Text(
                    "Email: ${user.email ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    "Role: ${user.role ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { onEdit(user) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { onDelete(user) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = BrightRed
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAccountDialog(
    user: User?,
    mode: String, // "create" or "edit"
    onDismiss: () -> Unit,
    onConfirm: (User, String?, String) -> Unit // User, password (nullable for edit), role
) {
    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(user?.role ?: "user") }

    var expandedDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (mode == "create") "Buat Akun Baru" else "Edit Akun",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepRed,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    enabled = mode == "create",
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(Modifier.height(12.dp))
                if (mode == "create") {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Peran") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        listOf("user", "worker", "administrator").forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    role = selectionOption
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = Color.Gray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val userToConfirm = User(
                                uid = user?.uid ?: "",
                                displayName = displayName,
                                email = email,
                                role = role
                            )
                            val passwordToPass = if (mode == "create" && password.isNotEmpty()) password else null
                            onConfirm(userToConfirm, passwordToPass, role)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepRed), // PERBAIKAN DI SINI
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (mode == "create") "Buat" else "Simpan", color = Color.White)
                    }
                }
            }
        }
    }
}