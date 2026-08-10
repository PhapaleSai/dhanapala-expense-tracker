package com.phapalesai.dhanapala.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = DhanapalaGreen,
    secondary = DhanapalaGold,
    error = DhanapalaRed,
    background = DhanapalaBackground,
    surface = DhanapalaSurface,
    onSurface = DhanapalaOnSurface
)

private val DarkColors = darkColorScheme(
    primary = DhanapalaGreenLight,
    secondary = DhanapalaGold,
    error = DhanapalaRed
)

@Composable
fun DhanapalaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = DhanapalaTypography,
        content = content
    )
}
