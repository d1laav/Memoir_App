@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.example.uts_map.ui.screen.journey

import android.net.Uri
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.example.uts_map.model.DiaryEntry
import com.android.example.uts_map.viewmodel.JourneyViewModel
import kotlinx.coroutines.launch

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

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
        }
    }

    val context = LocalContext.current
    val latLng = stringToLatLng(entry.location)
    val address = remember(latLng) { getReadableLocation(context, latLng) }

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
            // Tampilkan gambar jika ada
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

            // Isi
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
                    onClick = {
                        imagePickerLauncher.launch("image/*")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Media")
                    Spacer(Modifier.width(8.dp))
                    Text("Media")
                }

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = {
                        navController.navigate("map_picker/${entry.docId}")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Place, contentDescription = "Geotag")
                    Spacer(Modifier.width(8.dp))
                    Text("Geotag")
                }
            }
        }
    }
}
