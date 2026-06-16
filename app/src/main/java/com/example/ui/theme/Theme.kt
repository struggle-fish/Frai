package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SleekColorScheme = lightColorScheme(
    primary = SleekPrimary,
    onPrimary = SleekSurface,
    secondary = SleekSecondary,
    onSecondary = SleekOnSecondary,
    tertiary = PremiumGold,
    background = SleekBackground,
    onBackground = SleekText,
    surface = SleekSurface,
    onSurface = SleekText,
    surfaceVariant = SleekSurfaceVariant,
    onSurfaceVariant = SleekOnSecondary,
    outline = SleekBorder,
    error = SleekGachaButton
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Use our sleek light workspace theme as requested
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SleekColorScheme,
        typography = Typography,
        content = content
    )
}
