package com.example.tvtimeneverdie.auth

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

actual class GoogleAuthClient actual constructor(context: PlatformContext) {

    private val androidContext = context.context

    actual suspend fun getGoogleIdToken(): String? {
        val credentialManager = CredentialManager.create(androidContext)
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId())
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        return try {
            val result = credentialManager.getCredential(androidContext, request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                GoogleIdTokenCredential.createFrom(credential.data).idToken
            } else {
                null
            }
        } catch (e: GetCredentialException) {
            null
        }
    }

    /**
     * The Web Client ID string resource is generated in the app module by the google-services
     * Gradle plugin (from google-services.json), so it's looked up by name here rather than via
     * a generated R class reference (the shared module can't depend on the app module's R class).
     */
    private fun webClientId(): String {
        val resId = androidContext.resources.getIdentifier(
            "default_web_client_id",
            "string",
            androidContext.packageName,
        )
        check(resId != 0) {
            "default_web_client_id non trovato: assicurati di aver applicato il plugin google-services " +
                "e di avere google-services.json in androidApp/"
        }
        return androidContext.getString(resId)
    }
}
