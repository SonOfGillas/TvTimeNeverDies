package com.example.tvtimeneverdie.auth

import androidx.compose.runtime.Composable

expect class PlatformContext

@Composable
expect fun rememberPlatformContext(): PlatformContext
