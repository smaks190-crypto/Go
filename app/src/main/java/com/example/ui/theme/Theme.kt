package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CleanMintLightColorScheme = lightColorScheme(
    primary = MintDark,
    onPrimary = Color.White,
    primaryContainer = MintLight,
    onPrimaryContainer = MintDark,
    secondary = MintElectric,
    onSecondary = TextPrimaryDark,
    secondaryContainer = MintLight,
    onSecondaryContainer = MintDark,
    tertiary = NordicBlue,
    onTertiary = Color.White,
    tertiaryContainer = NordicBlueLight,
    onTertiaryContainer = NordicBlue,
    background = CleanWhiteBg,
    onBackground = TextPrimaryDark,
    surface = CleanWhiteSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CleanWhiteSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = CleanWhiteBorder,
    outlineVariant = CleanWhiteBorderSubtle,
    error = NordicCoral,
    onError = Color.White,
    errorContainer = NordicCoralLight,
    onErrorContainer = NordicCoral
)

@Composable
fun BudgetTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CleanMintLightColorScheme,
        typography = Typography,
        content = content
    )
}

