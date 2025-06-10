package com.android.example.uts_map.utils

import android.content.Context
import android.widget.Toast
import com.android.example.uts_map.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

// Fungsi untuk membuat GoogleSignInClient
fun createGoogleSignInClient(context: Context, clientId: String): GoogleSignInClient {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(clientId)
        .requestEmail()
        .build()

    return GoogleSignIn.getClient(context, gso)
}

// Fungsi untuk menangani hasil Sign-In Google
fun handleGoogleSignInResult(
    context: Context,
    result: Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount>,
    viewModel: AuthViewModel,
    onGoogleLoginSuccess: () -> Unit
) {
    try {
        val account = result.getResult(ApiException::class.java)
        val idToken = account?.idToken
        if (idToken != null) {
            viewModel.firebaseAuthWithGoogle(idToken) { success, error ->
                if (success) {
                    onGoogleLoginSuccess()
                } else {
                    Toast.makeText(context, "Login gagal: $error", Toast.LENGTH_LONG).show()
                }
            }
        }
    } catch (e: ApiException) {
        Toast.makeText(context, "Login gagal: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
