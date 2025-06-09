package com.android.example.uts_map

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

fun connectToFirebaseEmulator(host: String) {
    Firebase.firestore.useEmulator(host, 8080)
    Firebase.auth.useEmulator(host, 9099)
    Firebase.storage.useEmulator(host, 9199)
    Log.d("FIREBASE_EMULATOR", "Connected to Firebase Emulator at $host")
}
