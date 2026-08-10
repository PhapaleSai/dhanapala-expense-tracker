package com.phapalesai.dhanapala.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Always dark: this app's identity is a premium black look, matching
// fintech apps like CRED/Jupiter that stay dark regardless of system theme.
private val DhanapalaColors = darkColorScheme(
    primary = DhanapalaGreen,
    onPrimary = DhanapalaOnPrimary,
    secondary = DhanapalaGold,
    onSecondary = DhanapalaOnPrimary,
    error = DhanapalaRed,
    background = DhanapalaBackground,
    onBackground = DhanapalaOnSurface,
    surface = DhanapalaSurface,
    onSurface = DhanapalaOnSurface,
    surfaceVariant = DhanapalaSurfaceElevated,
    onSurfaceVariant = DhanapalaOnSurfaceMuted,
    outline = DhanapalaOutline
)

@Composable
fun DhanapalaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DhanapalaColors,
        typography = DhanapalaTypography,
        content = content
    )
}
