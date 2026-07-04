package com.example.tvtimeneverdie.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TvTimeYellow = Color(0xFFFFD400)
private val DeepCharcoal = Color(0xFF111111)
private val SurfaceGray = Color(0xFF242424)
private val HighContrastWhite = Color(0xFFFFFFFF)
private val MutedTextGray = Color(0xFF8E8E93)
private val AlertRed = Color(0xFFFF3B30)

private val TvTimeColorScheme = darkColorScheme(
    primary = TvTimeYellow,
    onPrimary = DeepCharcoal,
    secondary = TvTimeYellow,
    onSecondary = DeepCharcoal,
    tertiary = TvTimeYellow,
    onTertiary = DeepCharcoal,
    background = DeepCharcoal,
    onBackground = HighContrastWhite,
    surface = SurfaceGray,
    onSurface = HighContrastWhite,
    surfaceVariant = SurfaceGray,
    onSurfaceVariant = MutedTextGray,
    error = AlertRed,
    onError = HighContrastWhite,
)

@Composable
fun TvTimeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = TvTimeColorScheme, content = content)
}
