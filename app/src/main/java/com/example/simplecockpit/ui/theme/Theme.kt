package com.example.simplecockpit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = CockpitBlue,
    onPrimary = CockpitBackground,
    primaryContainer = CockpitBlueDark,
    background = CockpitBackground,
    onBackground = CockpitOnSurface,
    surface = CockpitSurface,
    onSurface = CockpitOnSurface,
    surfaceVariant = CockpitSurfaceVariant,
    onSurfaceVariant = CockpitOnSurfaceVariant,
    error = CockpitError,
    errorContainer = CockpitErrorContainer
)

@Composable
fun SimpleCockpitTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
