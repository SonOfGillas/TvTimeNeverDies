package com.example.tvtimeneverdie.data.repository

import com.example.tvtimeneverdie.domain.model.AuthUser
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository {
    private val auth = Firebase.auth

    val currentUser: Flow<AuthUser?> = auth.authStateChanged.map { it?.toAuthUser() }

    suspend fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
    }

    suspend fun signInWithGoogleIdToken(idToken: String) {
        val credential = GoogleAuthProvider.credential(idToken, null)
        auth.signInWithCredential(credential)
    }

    suspend fun signOut() {
        auth.signOut()
    }

    private fun FirebaseUser.toAuthUser() = AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
    )
}
