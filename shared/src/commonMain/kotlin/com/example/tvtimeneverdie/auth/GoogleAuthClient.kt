package com.example.tvtimeneverdie.auth

/** Obtains a Google ID token from the platform's native sign-in flow, to feed into Firebase Auth. */
expect class GoogleAuthClient(context: PlatformContext) {
    suspend fun getGoogleIdToken(): String?
}
