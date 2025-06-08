// File: app/src/main/java/com/example/fastpark/screens/admin/ParkingManagementScreen.kt
package com.example.fastpark.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.fastpark.viewmodel.AuthViewModel

// Contoh data class untuk slot parkir (Anda mungkin punya ini di data/model)
data class ParkingSlot(val id: String, val name: String, val status: String, val capacity: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingManagementScreen(authViewModel: AuthViewModel) {
    val dummyParkingSlots = remember { mutableStateListOf(
        ParkingSlot("P001", "Area A - Lantai 1", "Tersedia", 50),
        ParkingSlot("P002", "Area B - Lantai 1", "Penuh", 40),
        ParkingSlot("P003", "Area C - Lantai 2", "Tersedia", 60)
    )}
    var showDialog by remember { mutableStateOf(false) }
    var selectedSlot by remember { mutableStateOf<ParkingSlot?>(null) }
    var dialogMode by remember { mutableStateOf<String>("") } // "create", "edit"
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var slotToDelete by remember { mutableStateOf<ParkingSlot?>(null) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Manajemen Halaman Parkir", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            selectedSlot = null
            dialogMode = "create"
            showDialog = true
        }) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Slot Parkir")
            Spacer(Modifier.width(8.dp))
            Text("Tambah Slot Parkir Baru")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (dummyParkingSlots.isEmpty()) {
            Text("Tidak ada slot parkir terdaftar.", color = Color.Gray)
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(dummyParkingSlots) { slot ->
                    ParkingSlotItem(slot = slot,
                        onEdit = {
                            selectedSlot = it
                            dialogMode = "edit"
                            showDialog = true
                        },
                        onDelete = {
                            slotToDelete = it
                            showDeleteConfirmDialog = true
                        }
                    )
                    Divider()
                }
            }
        }
    }

    if (showDialog) {
        ParkingSlotDialog(
            slot = selectedSlot,
            mode = dialogMode,
            onDismiss = { showDialog = false },
            onConfirm = { id, name, status, capacity ->
                if (dialogMode == "create") {
                    dummyParkingSlots.add(ParkingSlot(id, name, status, capacity))
                } else { // edit mode
                    val index = dummyParkingSlots.indexOfFirst { it.id == id }
                    if (index != -1) {
                        dummyParkingSlots[index] = ParkingSlot(id, name, status, capacity)
                    }
                }
                showDialog = false
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Hapus Slot Parkir?") },
            text = { Text("Apakah Anda yakin ingin menghapus slot parkir ${slotToDelete?.name ?: "ini"}?") },
            confirmButton = {
                Button(
                    onClick = {
                        slotToDelete?.let {
                            dummyParkingSlots.remove(it)
                            println("Menghapus slot parkir: ${it.name}")
                        }
                        showDeleteConfirmDialog = false
                        slotToDelete = null
                    }
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        slotToDelete = null
                    }
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun ParkingSlotItem(slot: ParkingSlot, onEdit: (ParkingSlot) -> Unit, onDelete: (ParkingSlot) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("ID: ${slot.id}", style = MaterialTheme.typography.titleMedium)
            Text("Nama: ${slot.name}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text("Status: ${slot.status}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text("Kapasitas: ${slot.capacity}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Row {
            IconButton(onClick = { onEdit(slot) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { onDelete(slot) }) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingSlotDialog(
    slot: ParkingSlot?,
    mode: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int) -> Unit
) {
    var id by remember { mutableStateOf(slot?.id ?: "") }
    var name by remember { mutableStateOf(slot?.name ?: "") }
    var status by remember { mutableStateOf(slot?.status ?: "Tersedia") }
    var capacity by remember { mutableStateOf(slot?.capacity?.toString() ?: "") }

    var expandedDropdown by remember { mutableStateOf(false) } // State untuk ExposedDropdownMenu

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (mode == "create") "Buat Slot Parkir Baru" else "Edit Slot Parkir",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it },
                    label = { Text("ID Slot") },
                    enabled = mode == "create", // ID tidak bisa diubah saat edit
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Slot") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                // Dropdown untuk status
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        listOf("Tersedia", "Penuh", "Perbaikan").forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    status = selectionOption
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { newValue ->
                        capacity = newValue.filter { it.isDigit() } // Hanya angka
                    },
                    label = { Text("Kapasitas") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

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
                        onConfirm(
                            id,
                            name,
                            status,
                            capacity.toIntOrNull() ?: 0
                        )
                    }) {
                        Text(if (mode == "create") "Buat" else "Simpan")
                    }
                }
            }
        }
    }
}