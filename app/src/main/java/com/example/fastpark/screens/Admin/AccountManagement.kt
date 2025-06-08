package com.example.fastpark.screens.admin

import android.util.Log
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.fastpark.data.User
import com.example.fastpark.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManagementScreen(authViewModel: AuthViewModel) {
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
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Manajemen Akun", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    selectedUser = null
                    dialogMode = "create"
                    showDialog = true
                },
                enabled = !isLoading
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Tambah Akun")
                Spacer(Modifier.width(8.dp))
                Text("Tambah Akun Baru")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(50.dp))
                Text("Memuat data...")
            } else if (users.isEmpty()) {
                Text("Tidak ada akun pengguna yang terdaftar.", color = Color.Gray)
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
                            onDelete = { deletedUser -> //
                                userToDelete = deletedUser
                                showDeleteConfirmDialog = true
                            }
                        )
                        Divider()
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
                scope.launch { // <<< Meluncurkan coroutine untuk memanggil suspend fun
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
                        scope.launch { // <<< Meluncurkan coroutine untuk memanggil suspend fun
                            userToDelete?.let {
                                authViewModel.deleteUser(it.uid)
                                Log.d("AccountManagement", "Menghapus user: ${it.displayName}")
                            }
                            showDeleteConfirmDialog = false
                            userToDelete = null
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        userToDelete = null
                    },
                    enabled = !isLoading
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

// UserAccountItem dan UserAccountDialog (tetap sama seperti yang Anda berikan)
// Pastikan Composable ini berada di dalam file yang sama, atau diimpor dengan benar.

@Composable
fun UserAccountItem(user: User, onEdit: (User) -> Unit, onDelete: (User) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Nama: ${user.displayName ?: "N/A"}", style = MaterialTheme.typography.titleMedium)
            Text("Email: ${user.email ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text("Role: ${user.role ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Row {
            IconButton(onClick = { onEdit(user) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { onDelete(user) }) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus")
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
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (mode == "create") "Buat Akun Baru" else "Edit Akun",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    enabled = mode == "create",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                if (mode == "create") {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
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
                            .fillMaxWidth()
                            .menuAnchor()
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

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val userToConfirm = User(
                            uid = user?.uid ?: "",
                            displayName = displayName,
                            email = email,
                            role = role
                        )
                        // Pastikan password hanya dilewatkan jika mode "create" dan password tidak kosong
                        val passwordToPass = if (mode == "create" && password.isNotEmpty()) password else null
                        onConfirm(userToConfirm, passwordToPass, role)
                    }) {
                        Text(if (mode == "create") "Buat" else "Simpan")
                    }
                }
            }
        }
    }
}