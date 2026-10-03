package com.ridevision.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RadiantGoldPrimary,
    onPrimary = OnGoldPrimary,
    primaryContainer = RadiantGoldContainer,
    onPrimaryContainer = OnGoldContainer,
    secondary = MintSecondary,
    onSecondary = OnMintSecondary,
    secondaryContainer = MintSecondaryContainer,
    onSecondaryContainer = OnMintSecondaryContainer,
    tertiary = WarmAmberTertiary,
    onTertiary = OnGoldPrimary,
    tertiaryContainer = TertiaryContainer,
    background = EmeraldBackground,
    onBackground = TextOnSurface,
    surface = EmeraldBackground,
    onSurface = TextOnSurface,
    surfaceVariant = EmeraldSurfaceHigh,
    onSurfaceVariant = TextOnSurfaceVariant,
    error = HazardError,
    onError = OnError,
    errorContainer = HazardErrorContainer,
    outline = OutlineColor,
    outlineVariant = OutlineVariant
)

@Composable
fun RideVisionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
