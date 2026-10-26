package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MinimalistDarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueDim,
    onPrimaryContainer = TextPrimary,
    secondary = SoftIndigo,
    onSecondary = Color.White,
    secondaryContainer = SoftIndigoDim,
    onSecondaryContainer = TextPrimary,
    tertiary = ImperialGold,
    onTertiary = ObsidianBg,
    tertiaryContainer = ImperialGoldDim,
    onTertiaryContainer = TextPrimary,
    error = CrimsonAlert,
    errorContainer = CrimsonAlertDim,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = SlateSurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SlateBorderStrong
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MinimalistDarkColorScheme,
        typography = Typography,
        content = content
    )
}
