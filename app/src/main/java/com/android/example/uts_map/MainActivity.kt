package com.android.example.uts_map

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.android.example.uts_map.ui.theme.UTS_MAPTheme
import com.google.firebase.FirebaseApp


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // firebase init
        FirebaseApp.initializeApp(this)

        // dinyalakan untuk emulator
        if (BuildConfig.DEBUG) {
            connectToFirebaseEmulator()
        }

        setContent {
            UTS_MAPTheme {
                MyApp()
            }
        }
    }

}
