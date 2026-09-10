package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SageDarkPrimary,
    onPrimary = SageDarkOnPrimary,
    primaryContainer = SageDarkPrimaryContainer,
    background = WarmBackgroundDark,
    onBackground = TextLight,
    surface = WarmSurfaceDark,
    onSurface = TextLight,
    error = DestructiveRed,
    outline = BorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = SageOnPrimary,
    primaryContainer = SagePrimaryContainer,
    background = WarmBackgroundLight,
    onBackground = TextDark,
    surface = WarmSurfaceLight,
    onSurface = TextDark,
    error = DestructiveRed,
    outline = BorderLight
)

@Composable
fun HomeoClinicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
