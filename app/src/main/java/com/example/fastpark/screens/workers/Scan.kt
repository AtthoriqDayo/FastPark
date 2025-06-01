package com.example.fastpark.screens.workers

// Import your AuthViewModel if needed to get worker details for logging, etc.
// import com.example.fastpark.viewmodel.AuthViewModel
// import androidx.lifecycle.viewmodel.compose.viewModel

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory

// It's highly recommended to move Firestore logic (like processScannedUserId)
// into a ViewModel for better architecture (e.g., ParkingViewModel).
// import com.example.fastpark.viewmodel.ParkingViewModel
// import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun ScanScreen(
    // parkingViewModel: ParkingViewModel = viewModel() // Example if using a ViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var scannedValue by remember { mutableStateOf<String?>(null) } // Holds the most recently scanned unique value
    var processingScan by remember { mutableStateOf(false) } // To prevent concurrent processing
    var parkingStatusMessage by remember { mutableStateOf<String?>(null) }
    val db = Firebase.firestore // Or get from ViewModel

    // Create and remember the DecoratedBarcodeView instance
    val decoratedBarcodeView = remember {
        DecoratedBarcodeView(context).apply {
            // Configure the view here if some configurations are static
            barcodeView.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
            setStatusText("") // Optional: remove default status text like "Place a barcode inside the viewfinder rectangle to scan it."
            // You can customize viewfinder features here if needed
            // e.g., viewFinder.setLaserVisibility(true)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                parkingStatusMessage = "Camera permission is required to scan QR codes."
                Toast.makeText(context, "Camera permission denied.", Toast.LENGTH_SHORT).show()
            } else {
                // Permission granted, the view will resume via lifecycle observer or initial setup
                Log.d("ScanScreen", "Camera permission granted.")
                // Explicitly resume if the view was already added but paused due to lack of permission
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    decoratedBarcodeView.resume()
                }
            }
        }
    )

    // Request permission when the composable enters the composition if not already granted
    LaunchedEffect(key1 = Unit) { // key1 = Unit ensures this runs once on initial composition
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Lifecycle management for the DecoratedBarcodeView
    DisposableEffect(key1 = lifecycleOwner, key2 = hasCameraPermission) {
        val observer = LifecycleEventObserver { _, event ->
            if (hasCameraPermission) { // Only manage lifecycle if permission is granted
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        Log.d("ScanScreen", "Lifecycle ON_RESUME, resuming scanner.")
                        decoratedBarcodeView.resume()
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        Log.d("ScanScreen", "Lifecycle ON_PAUSE, pausing scanner.")
                        decoratedBarcodeView.pause()
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        // While pause in onDispose is good, this handles Activity/Fragment destruction
                        Log.d("ScanScreen", "Lifecycle ON_DESTROY, pausing scanner.")
                        decoratedBarcodeView.pause()
                    }
                    else -> { /* Other lifecycle events */ }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            Log.d("ScanScreen", "DisposableEffect onDispose, pausing scanner and removing observer.")
            decoratedBarcodeView.pause() // Ensure paused on dispose
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (hasCameraPermission) {
            Text("Point camera at a QR code.", modifier = Modifier.padding(bottom = 8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Adjust scanner size as needed
                    .padding(vertical = 10.dp)
            ) {
                AndroidView(
                    factory = { decoratedBarcodeView },
                    modifier = Modifier.fillMaxSize(), // Fills the Box
                    update = { view ->
                        // This block is called when the view needs to be updated,
                        // for DecoratedBarcodeView, most setup is in factory or via remembered instance.
                        // If permission was granted after initial composition, ensure it resumes.
                        if (hasCameraPermission && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            view.resume()
                        }
                    }
                )
                // Setup the callback for continuous scanning
                // Doing it here ensures it's setup after the view is created and potentially re-setup if needed,
                // though with 'remember' for decoratedBarcodeView, factory is usually enough.
                // For safety, can also be placed in a LaunchedEffect keyed to decoratedBarcodeView.
                DisposableEffect(key1 = decoratedBarcodeView) { // Re-apply callback if view instance were to change
                    decoratedBarcodeView.decodeContinuous { result ->
                        if (processingScan) return@decodeContinuous // Don't process if already processing

                        result.text?.let { barCodeOrQr ->
                            // Process only if the new scan is different from the last one
                            // or if you want to allow re-processing (e.g. after a delay or user action)
                            if (barCodeOrQr != scannedValue || scannedValue == null) { // Process if new or first scan
                                processingScan = true
                                scannedValue = barCodeOrQr
                                Log.d("EmbeddedScan", "Scanned Raw: $barCodeOrQr")

                                processScannedUserId(barCodeOrQr, db) { success, message ->
                                    parkingStatusMessage = message
                                    if (success) {
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                        Log.d("ParkingProcess", "Success: $message")
                                        // Optionally, to allow re-scanning the same code after a success:
                                        // scannedValue = null
                                    } else {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        Log.e("ParkingProcess", "Failure: $message")
                                        // If an error occurs, allow trying to scan the same code again
                                        // scannedValue = null
                                    }
                                    processingScan = false // Release lock
                                }
                            }
                        }
                    }
                    onDispose {
                        // If you need to clear the callback, but typically not needed as view is paused/destroyed
                    }
                }
            }
            scannedValue?.let {
                if(!processingScan) { // Only show "Last Scanned" if not actively processing
                    Text("Last Processed User ID: $it", modifier = Modifier.padding(top = 8.dp))
                }
            }

        } else {
            Text("Camera permission is required for QR scanning.", modifier = Modifier.padding(bottom = 8.dp))
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Grant Camera Permission")
            }
        }

        parkingStatusMessage?.let {
            Spacer(modifier = Modifier.height(10.dp))
            Text(it)
        }
    }
}

@SuppressLint("DefaultLocale")
fun processScannedUserId(
    userId: String,
    db: FirebaseFirestore, // Pass Firestore instance
    callback: (Boolean, String) -> Unit
) {
    Log.d("FirestoreProcess", "Processing User ID: $userId")
    // 1. Check if user has an active parking session
    db.collection("parking_sessions")
        .whereEqualTo("userId", userId)
        .whereEqualTo("status", "active")
        .limit(1)
        .get()
        .addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                // Active session found - This is an EXIT
                val activeSessionDoc = documents.documents[0]
                val sessionId = activeSessionDoc.id
                val entryTimestamp = activeSessionDoc.getTimestamp("entryTimestamp")
                val exitTimestamp = Timestamp.now() // Use Firebase Timestamp

                if (entryTimestamp != null) {
                    val durationMillis = exitTimestamp.toDate().time - entryTimestamp.toDate().time
                    val durationMinutes = durationMillis / (1000 * 60)

                    // Placeholder: Fetch actual pricePerHour dynamically
                    // This might come from parking_areas collection or stored in the session document itself.
                    val pricePerHour = activeSessionDoc.getDouble("pricePerHour") ?: 10.0 // Example: Default to 10 if not found
                    val feeCalculated = (durationMinutes / 60.0) * pricePerHour

                    db.collection("parking_sessions").document(sessionId)
                        .update(
                            mapOf(
                                "exitTimestamp" to exitTimestamp,
                                "durationMinutes" to durationMinutes,
                                "feeCalculated" to feeCalculated,
                                "status" to "completed"
                            )
                        )
                        .addOnSuccessListener {
                            Log.d("Firestore", "Parking session $sessionId completed for user $userId.")
                            // IMPORTANT: Deducting fee from user's balance should ideally be handled by a secure Cloud Function.
                            // This client-side callback is just for status update.
                            callback(true, "User $userId exited. Fee: $${String.format("%.2f", feeCalculated)}. Session: $sessionId")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error completing parking session $sessionId", e)
                            callback(false, "Error completing session: ${e.message}")
                        }
                } else {
                    Log.e("Firestore", "Active session $sessionId found but entryTimestamp is missing.")
                    callback(false, "Error: Active session found but entry time is missing.")
                }

            } else {
                // No active session - This is an ENTRY
                // Placeholder: Get parkingArea and pricePerHour for the new session
                // This might come from worker input, device location, or a default.
                val parkingArea = "MainLot-A1" // Example
                val pricePerHourForNewSession = 10.0 // Example: This should be dynamic

                val newSession = hashMapOf(
                    "userId" to userId,
                    "parkingArea" to parkingArea,
                    "pricePerHour" to pricePerHourForNewSession, // Store price for accurate exit calculation
                    "entryTimestamp" to Timestamp.now(), // Use Firebase Timestamp
                    "exitTimestamp" to null,
                    "durationMinutes" to null,
                    "feeCalculated" to null,
                    "status" to "active"
                )
                db.collection("parking_sessions")
                    .add(newSession)
                    .addOnSuccessListener { documentReference ->
                        Log.d("Firestore", "Parking session ${documentReference.id} started for user $userId.")
                        callback(true, "User $userId entered. Session ID: ${documentReference.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error starting parking session for user $userId", e)
                        callback(false, "Error starting session: ${e.message}")
                    }
            }
        }
        .addOnFailureListener { exception ->
            Log.w("Firestore", "Error getting active parking session for user $userId", exception)
            callback(false, "Error checking parking session: ${exception.message}")
        }
}