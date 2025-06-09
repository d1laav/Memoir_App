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
//        if (BuildConfig.DEBUG) {
//            val host = if (isRunningOnEmulator()) "10.0.2.2" else "192.168.137.200"
//            connectToFirebaseEmulator(host)
//        }

        setContent {
            UTS_MAPTheme {
                MyApp()
            }
        }
    }
    // dinyalakan untuk emulator
//    fun isRunningOnEmulator(): Boolean {
//        return Build.FINGERPRINT.contains("generic")
//                || Build.MODEL.contains("Emulator")
//                || Build.BRAND.startsWith("generic")
//                || Build.DEVICE.startsWith("generic")
//                || Build.PRODUCT == "sdk_gphone64_arm64"
//    }

}
