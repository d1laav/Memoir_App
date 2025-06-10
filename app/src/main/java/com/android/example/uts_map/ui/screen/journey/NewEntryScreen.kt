@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.example.uts_map.ui.screen.journey

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.example.uts_map.model.DiaryEntry
import com.android.example.uts_map.utils.getCurrentTimeString
import com.android.example.uts_map.utils.getTodayDateString
import com.android.example.uts_map.viewmodel.JourneyViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NewEntryScreen(
    viewModel: JourneyViewModel,
    navController: NavController,
    onNavigateBack: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var selectedDate by rememberSaveable { mutableStateOf(getTodayDateString()) }
    val time = remember { getCurrentTimeString() }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val location = viewModel.selectedLocation.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    // media permission
    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Izin galeri ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    // convert coordinate location
    val latLng = remember(location) { stringToLatLng(location) }
    val readableLocation = remember(latLng) {
        latLng?.let { getReadableLocation(context, it) } ?: "Lokasi tidak tersedia"
    }

    // camera
    val photoUri = remember {
        val file = File(context.cacheDir, "captured_image.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = photoUri
        }
    }

    // cam permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(photoUri)
        } else {
            Toast.makeText(context, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    var showMediaDialog by remember { mutableStateOf(false) }

    // location permission
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
            if (isGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { locationResult ->
                    locationResult?.let {
                        val lat = it.latitude
                        val lng = it.longitude
                        viewModel.setSelectedLocation("$lat,$lng")
                    } ?: Toast.makeText(context, "Gagal mengambil lokasi", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                Toast.makeText(context, "Akses lokasi ditolak: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    var showGeotagDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catatan Baru") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isUploading) return@IconButton
                            coroutineScope.launch {
                                isUploading = true
                                val userUid = Firebase.auth.currentUser?.uid.orEmpty()

                                if (title.isBlank() && content.isBlank()) {
                                    snackbarHostState.showSnackbar("Judul atau isi tidak boleh kosong")
                                    isUploading = false
                                    return@launch
                                }

                                // generate docId terlebih dahulu & masukkin ke collection firestore "entries"
                                val docRef = Firebase.firestore.collection("entries").document()
                                val docId = docRef.id

                                // ini buat save image ke storage nya
                                val imageUrl = try {
                                    imageUri?.let { uri ->
                                        val ref = Firebase.storage.reference.child("images/$docId.jpg")
                                        ref.putFile(uri).await()
                                        ref.downloadUrl.await().toString()
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Gagal upload gambar: ${e.message}")
                                    isUploading = false
                                    return@launch
                                }

                                // ini buat save ke firestore
                                val newEntry = DiaryEntry(
                                    docId = docId,
                                    date = selectedDate,
                                    time = time,
                                    title = title,
                                    content = content,
                                    imageUri = imageUrl,
                                    location = if (location.isNullOrBlank()) null else location,
                                    ownerUid = userUid
                                )

                                try {
                                    docRef.set(newEntry).await()
                                    snackbarHostState.showSnackbar("Catatan berhasil disimpan")
                                    viewModel.setSelectedLocation(null)
                                    onNavigateBack()
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Gagal menyimpan catatan: ${e.message}")
                                }

                                isUploading = false
                            }
                        },
                        enabled = !isUploading
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Simpan")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            imageUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            DateSelector(selectedDate) { selectedDate = it }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Judul") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Catatan") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { showMediaDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Media")
                    Spacer(Modifier.width(8.dp))
                    Text("Media")
                }

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = { showGeotagDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Place, contentDescription = "Geotag")
                    Spacer(Modifier.width(8.dp))
                    Text("Geotag")
                }
            }

            if (showMediaDialog) {
                AlertDialog(
                    onDismissRequest = { showMediaDialog = false },
                    title = { Text("Tambah Media") },
                    text = { Text("Pilih sumber media:") },
                    confirmButton = {
                        TextButton(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            } else {
                                cameraLauncher.launch(photoUri)
                            }
                        }) {
                            Text("Ambil Foto")
                        }

                    },
                    dismissButton = {
                        TextButton(onClick = {
                            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                android.Manifest.permission.READ_MEDIA_IMAGES
                            } else {
                                android.Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                            galleryPermissionLauncher.launch(permission)
                        }) {
                            Text("Pilih dari Galeri")
                        }
                    }
                )
            }

            if (showGeotagDialog) {
                AlertDialog(
                    onDismissRequest = { showGeotagDialog = false },
                    title = { Text("Pilih Lokasi") },
                    text = { Text("Gunakan lokasi saat ini atau pilih di peta") },
                    confirmButton = {
                        TextButton(onClick = {
                            showGeotagDialog = false
                            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        }) {
                            Text("Lokasi Saat Ini")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showGeotagDialog = false
                            navController.navigate("map_picker/new")
                        }) {
                            Text("Pilih di Peta")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))


            if (!location.isNullOrBlank()) {
                Text("📍 Lokasi: $readableLocation", style = MaterialTheme.typography.bodyMedium)
            }

        }
    }
}