package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BudgetDarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = DarkBackground,
    primaryContainer = NeonIndigo,
    onPrimaryContainer = TextPrimary,
    secondary = NeonIndigo,
    onSecondary = TextPrimary,
    tertiary = Sky400,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = NeonRose,
    onError = TextPrimary
)

@Composable
fun BudgetTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BudgetDarkColorScheme,
        typography = Typography,
        content = content
    )
}
