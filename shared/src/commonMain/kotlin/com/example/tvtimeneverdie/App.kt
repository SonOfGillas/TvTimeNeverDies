package com.example.tvtimeneverdie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tvtimeneverdie.di.AppContainer
import com.example.tvtimeneverdie.ui.navigation.MainScreen
import com.example.tvtimeneverdie.ui.screens.auth.LoginScreen
import com.example.tvtimeneverdie.ui.theme.TvTimeTheme

@Composable
@Preview
fun App() {
    TvTimeTheme {
        val authUser by AppContainer.authRepository.currentUser.collectAsStateWithLifecycle(initialValue = null)
        val user = authUser
        if (user == null) {
            LoginScreen()
        } else {
            MainScreen(user)
        }
    }
}
