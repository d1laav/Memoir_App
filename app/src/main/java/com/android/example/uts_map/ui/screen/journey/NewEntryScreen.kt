@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.example.uts_map.ui.screen.journey

import android.net.Uri
import android.os.Build
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.example.uts_map.model.DiaryEntry
import com.android.example.uts_map.utils.getCurrentTimeString
import com.android.example.uts_map.utils.getTodayDateString
import com.android.example.uts_map.viewmodel.JourneyViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

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

    val location = viewModel.selectedLocation.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

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

                                // Generate docId terlebih dahulu
                                val docRef = Firebase.firestore.collection("entries").document()
                                val docId = docRef.id

                                // Upload gambar jika ada (nama file menggunakan doc id)
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

                                // Simpan ke Firestore
                                val newEntry = DiaryEntry(
                                    docId = docId,
                                    date = selectedDate,
                                    time = time,
                                    title = title,
                                    content = content,
                                    imageUri = imageUrl,
                                    location = location.orEmpty(),
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
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Media")
                    Spacer(Modifier.width(8.dp))
                    Text("Media")
                }

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = { navController.navigate("map_picker/new") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Place, contentDescription = "Geotag")
                    Spacer(Modifier.width(8.dp))
                    Text("Geotag")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!location.isNullOrBlank()) {
                Text("📍 Lokasi: $location", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}