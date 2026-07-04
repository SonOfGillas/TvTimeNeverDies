package com.example.tvtimeneverdie.auth

/**
 * Non ancora implementato su iOS: richiede l'integrazione nativa del GoogleSignIn SDK
 * (CocoaPods/SPM) da fare su Mac con Xcode. L'accesso email/password rimane pienamente
 * funzionante su iOS tramite Firebase Auth.
 */
actual class GoogleAuthClient actual constructor(private val context: PlatformContext) {
    actual suspend fun getGoogleIdToken(): String? {
        throw NotImplementedError(
            "Google Sign-In su iOS non e' ancora implementato: richiede setup nativo su Mac/Xcode.",
        )
    }
}
