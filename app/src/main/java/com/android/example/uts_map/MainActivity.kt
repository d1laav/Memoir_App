package com.android.example.uts_map

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.android.example.uts_map.ui.theme.UTS_MAPTheme
//import com.google.firebase.BuildConfig
import com.android.example.uts_map.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // firebase init
        FirebaseApp.initializeApp(this)

        if (BuildConfig.DEBUG) {
            val firestore = Firebase.firestore
            val auth = Firebase.auth
            firestore.useEmulator("10.0.2.2", 8000)
            auth.useEmulator("10.0.2.2", 9099)
        }
        setContent {
            UTS_MAPTheme {
                MyApp()
            }
        }
    }
}
