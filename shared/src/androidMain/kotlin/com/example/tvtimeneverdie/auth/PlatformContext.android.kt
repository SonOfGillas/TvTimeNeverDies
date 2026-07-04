package com.example.tvtimeneverdie.auth

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class PlatformContext(val context: Context)

@Composable
actual fun rememberPlatformContext(): PlatformContext {
    val context = LocalContext.current
    return remember(context) { PlatformContext(context) }
}
