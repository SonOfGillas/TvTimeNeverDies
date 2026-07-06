package com.example.tvtimeneverdie.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TvTimeYellow = Color(0xFFFFD400)
private val DarkGray = Color(0xFF2E2E32)
private val SurfaceGray = Color(0xFF242424)
private val OnAccentText = Color(0xFF1C1C1E)
private val HighContrastWhite = Color(0xFFFFFFFF)
private val MutedTextGray = Color(0xFF8E8E93)
private val AlertRed = Color(0xFFFF3B30)

private val TvTimeColorScheme = darkColorScheme(
    primary = TvTimeYellow,
    onPrimary = OnAccentText,
    secondary = TvTimeYellow,
    onSecondary = OnAccentText,
    tertiary = TvTimeYellow,
    onTertiary = OnAccentText,
    background = DarkGray,
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
