package com.example.tvtimeneverdie.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))

        LogoAvatar()

        Spacer(Modifier.height(16.dp))
        Text(
            "TvTimeNeverDies",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Accedi per continuare a tracciare le tue serie",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(32.dp))

        AuthModeToggle(
            isRegisterMode = state.isRegisterMode,
            onModeSelected = { registerMode ->
                if (registerMode != state.isRegisterMode) viewModel.onToggleMode()
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp),
                )
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(20.dp),
        ) {
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Password") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    TextButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Text(
                            if (isPasswordVisible) "NASCONDI" else "MOSTRA",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.submit() }),
                modifier = Modifier.fillMaxWidth(),
            )
            state.errorMessage?.let { message ->
                Spacer(Modifier.height(12.dp))
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = viewModel::submit,
            enabled = !state.isLoading,
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text(
                if (state.isRegisterMode) "Registrati" else "Accedi",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
        }

        Spacer(Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.weight(1f),
            )
            Text(
                "oppure",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(24.dp))

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
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("Accedi con Google")
        }

        if (state.isLoading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun LogoAvatar(modifier: Modifier = Modifier) {
    val haloColor = Color(0xFFFFC947)
    val letterColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .width(120.dp)
            .height(120.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val haloCenter = Offset(size.width / 2f, size.height * 0.22f)
            val glowRadius = size.width * 0.32f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(haloColor.copy(alpha = 0.4f), Color.Transparent),
                    center = haloCenter,
                    radius = glowRadius,
                ),
                radius = glowRadius,
                center = haloCenter,
            )
            drawOval(
                color = haloColor,
                topLeft = Offset(size.width * 0.28f, size.height * 0.06f),
                size = Size(size.width * 0.44f, size.height * 0.16f),
                style = Stroke(width = size.width * 0.045f),
            )
        }
        Text(
            "T",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black),
            color = letterColor,
            modifier = Modifier.padding(bottom = 4.dp),
        )
    }
}

@Composable
private fun AuthModeToggle(
    isRegisterMode: Boolean,
    onModeSelected: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
            .padding(4.dp),
    ) {
        AuthModeSegment(
            label = "Accedi",
            isActive = !isRegisterMode,
            onClick = { onModeSelected(false) },
            modifier = Modifier.weight(1f),
        )
        AuthModeSegment(
            label = "Registrati",
            isActive = isRegisterMode,
            onClick = { onModeSelected(true) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AuthModeSegment(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = textColor,
            style = if (isActive) {
                MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            } else {
                MaterialTheme.typography.labelLarge
            },
        )
    }
}
