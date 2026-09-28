package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TacticalCyberColorScheme = darkColorScheme(
  primary = NeonCyan,
  onPrimary = CyberBackground,
  primaryContainer = CyberSurfaceHigh,
  onPrimaryContainer = NeonCyan,
  secondary = NeonCrimson,
  onSecondary = CyberBackground,
  secondaryContainer = CyberSurfaceHigh,
  onSecondaryContainer = NeonCrimson,
  tertiary = MatrixGreen,
  onTertiary = CyberBackground,
  tertiaryContainer = CyberSurfaceHigh,
  onTertiaryContainer = MatrixGreen,
  background = CyberBackground,
  onBackground = TextPrimary,
  surface = CyberSurface,
  onSurface = TextPrimary,
  surfaceVariant = CyberSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = BorderGlow
)

@Composable
fun TacticalLegendTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = TacticalCyberColorScheme,
    typography = Typography,
    content = content
  )
}

