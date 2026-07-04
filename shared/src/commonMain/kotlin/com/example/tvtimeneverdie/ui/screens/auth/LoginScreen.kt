package com.example.tvtimeneverdie.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tvtimeneverdie.auth.GoogleAuthClient
import com.example.tvtimeneverdie.auth.rememberPlatformContext
import com.example.tvtimeneverdie.ui.rememberViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen() {
    val viewModel = rememberViewModel { LoginViewModel() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val platformContext = rememberPlatformContext()
    val googleAuthClient = remember { GoogleAuthClient(platformContext) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("TvTimeNeverDies", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { viewModel.submit() }),
            modifier = Modifier.fillMaxWidth(),
        )
        state.errorMessage?.let { message ->
            Spacer(Modifier.height(8.dp))
            Text(message, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = viewModel::submit,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.isRegisterMode) "Registrati" else "Accedi")
        }
        TextButton(onClick = viewModel::onToggleMode) {
            Text(if (state.isRegisterMode) "Hai gia' un account? Accedi" else "Non hai un account? Registrati")
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                coroutineScope.launch {
                    try {
                        val idToken = googleAuthClient.getGoogleIdToken()
                        if (idToken != null) {
                            viewModel.signInWithGoogleIdToken(idToken)
                        } else {
                            viewModel.onGoogleSignInFailed("Accesso con Google annullato")
                        }
                    } catch (e: NotImplementedError) {
                        viewModel.onGoogleSignInFailed(
                            e.message ?: "Accesso con Google non disponibile su questa piattaforma",
                        )
                    } catch (e: Exception) {
                        viewModel.onGoogleSignInFailed(e.message ?: "Errore durante l'accesso con Google")
                    }
                }
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Accedi con Google")
        }
        if (state.isLoading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}
