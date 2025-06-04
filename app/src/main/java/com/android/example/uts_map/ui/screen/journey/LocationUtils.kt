package com.android.example.uts_map.ui.screen.journey

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng

fun getReadableLocation(context: Context, latLng: LatLng): String {
    val geocoder = Geocoder(context)
    val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
    val address = addresses?.firstOrNull()  // Menggunakan safe call dan mengambil elemen pertama jika ada, jika tidak null
    val addressLine = address?.getAddressLine(0) ?: "Alamat tidak ditemukan"

    return addressLine

}

// function untuk mengonversi string lokasi menjadi LatLng
fun stringToLatLng(location: String?): LatLng {
    val latLngArray = location?.split(",") ?: return LatLng(-6.2, 106.8) // default jika null
    val latitude = latLngArray.getOrNull(0)?.toDoubleOrNull() ?: -6.2
    val longitude = latLngArray.getOrNull(1)?.toDoubleOrNull() ?: 106.8
    return LatLng(latitude, longitude)
}