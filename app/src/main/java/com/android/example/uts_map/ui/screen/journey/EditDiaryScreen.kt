@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.example.uts_map.ui.screen.journey

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.example.uts_map.model.DiaryEntry
import com.android.example.uts_map.viewmodel.JourneyViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun EditDiaryScreen(
    entry: DiaryEntry,
    onDelete: () -> Unit,
    onSave: (DiaryEntry) -> Unit,
    onNavigateBack: () -> Unit,
    navController: NavController,
    journeyViewModel: JourneyViewModel,
) {
    var title by remember { mutableStateOf(entry.title) }
    var content by remember { mutableStateOf(entry.content) }
    var imageUri by remember { mutableStateOf(entry.imageUri?.let { Uri.parse(it) }) }
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
        }
    }

    var showMediaDialog by remember { mutableStateOf(false) }

    val photoUri = remember {
        val file = File(context.cacheDir, "captured_edit_image.jpg")
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = photoUri
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(photoUri)
        } else {
            Toast.makeText(context, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Izin galeri ditolak", Toast.LENGTH_SHORT).show()
        }
    }

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
                        val updatedEntry = entry.copy(location = "$lat,$lng")
                        journeyViewModel.updateEntry(updatedEntry) { success, error ->
                            coroutineScope.launch {
                                if (success) {
                                    snackbarHostState.showSnackbar("Lokasi berhasil diperbarui")
                                } else {
                                    snackbarHostState.showSnackbar("Gagal menyimpan lokasi: $error")
                                }
                            }
                        }
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

    val latLng = stringToLatLng(entry.location)
    val address = remember(latLng) {
        latLng?.let { getReadableLocation(context, it) } ?: "Lokasi tidak tersedia"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(entry.date) },
                navigationIcon = {
                    IconButton(onClick = {
                        val updatedEntry = entry.copy(
                            title = title,
                            content = content,
                            imageUri = imageUri?.toString()
                        )

                        journeyViewModel.updateEntry(updatedEntry) { success, error ->
                            coroutineScope.launch {
                                if (success) {
                                    snackbarHostState.showSnackbar("Catatan disimpan")
                                    onNavigateBack()
                                } else {
                                    snackbarHostState.showSnackbar("Gagal menyimpan: $error")
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Simpan")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onDelete()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Catatan dihapus ")
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // tampilkan gambar jika ada
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

            // judul notes
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Edit Judul") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // isi
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Edit Catatan") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!entry.location.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "📍 Lokasi: $address",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tombol bawah
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
                            showMediaDialog = false
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
                            showMediaDialog = false
                        }) {
                            Text("Pilih dari Galeri")
                        }
                    }
                )
            }
            if (showGeotagDialog) {
                AlertDialog(
                    onDismissRequest = { showGeotagDialog = false },
                    title = { Text("Perbarui Lokasi") },
                    text = { Text("Pilih metode untuk memperbarui lokasi:") },
                    confirmButton = {
                        TextButton(onClick = {
                            val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
                            locationPermissionLauncher.launch(permission)
                            showGeotagDialog = false
                        }) {
                            Text("Gunakan Lokasi Saat Ini")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            navController.navigate("map_picker/${entry.docId}")
                            showGeotagDialog = false
                        }) {
                            Text("Pilih di Peta")
                        }
                    }
                )
            }
        }
    }
}
