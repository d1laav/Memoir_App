package com.android.example.uts_map.ui.screen.journey

import android.location.Geocoder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.example.uts_map.model.DiaryEntry
import com.android.example.uts_map.viewmodel.JourneyViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    viewModel: JourneyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedLatLng by remember { mutableStateOf(LatLng(-6.2, 106.8)) }
    var address by remember { mutableStateOf(getReadableLocation(context, selectedLatLng)) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLatLng, 10f)
    }

    // Menangani pencarian lokasi berdasarkan input pengguna
    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 4) {  // Menunggu hingga lebih dari 4 karakter
            val geocoder = Geocoder(context)
            val results = geocoder.getFromLocationName(searchQuery, 1)
            if (!results.isNullOrEmpty()) {
                val result = results[0]
                val newLatLng = LatLng(result.latitude, result.longitude)
                selectedLatLng = newLatLng
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(newLatLng, 15f))
            }
        }
    }

    // Memperbarui alamat saat pin lokasi dipindahkan
    LaunchedEffect(selectedLatLng) {
        address = getReadableLocation(context, selectedLatLng)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pilih Lokasi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column {
                // Kolom Pencarian Alamat
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Cari alamat...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            // Pencarian dilakukan secara langsung saat tombol search di-klik
                            val geocoder = Geocoder(context)
                            val results = geocoder.getFromLocationName(searchQuery, 1)
                            if (!results.isNullOrEmpty()) {
                                val result = results[0]
                                val newLatLng = LatLng(result.latitude, result.longitude)
                                selectedLatLng = newLatLng
                                cameraPositionState.move(
                                    CameraUpdateFactory.newLatLngZoom(newLatLng, 15f)
                                )
                            }
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Cari Lokasi")
                        }
                    }
                )

                // Menampilkan alamat yang terbaca
                Text(
                    text = "Alamat: $address",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                // Google Map dengan kemampuan drag pada peta (peta dapat digeser secara default)
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { selectedLatLng = it } // Memperbarui pin saat peta diklik
                ) {
                    Marker(
                        state = MarkerState(position = selectedLatLng),
                        title = "Lokasi Dipilih",
                        snippet = "Klik Simpan Lokasi",
                        draggable = false  // Marker tidak dapat dipindahkan secara langsung, tetapi peta bisa digeser
                    )
                }

                // Tombol Simpan Lokasi
                Button(
                    onClick = {
                        val locationString = "${selectedLatLng.latitude},${selectedLatLng.longitude}"
                        viewModel.setSelectedLocation(locationString)
                        onBack()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                ) {
                    Text("Simpan Lokasi")
                }
            }
        }
    }
}