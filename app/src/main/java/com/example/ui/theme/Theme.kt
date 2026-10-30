package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = RosePrimaryDark,
    onPrimary = RoseOnPrimaryDark,
    primaryContainer = RosePrimaryContainerDark,
    onPrimaryContainer = RoseOnPrimaryContainerDark,
    secondary = BlushSecondaryDark,
    onSecondary = BlushOnSecondaryDark,
    secondaryContainer = BlushSecondaryContainerDark,
    onSecondaryContainer = BlushOnSecondaryContainerDark,
    tertiary = AmberTertiaryDark,
    onTertiary = AmberOnTertiaryDark,
    tertiaryContainer = AmberTertiaryContainerDark,
    onTertiaryContainer = AmberOnTertiaryContainerDark,
    background = WarmBackgroundDark,
    surface = WarmSurfaceDark,
    surfaceVariant = WarmSurfaceVariantDark,
    onBackground = WarmOnSurfaceDark,
    onSurface = WarmOnSurfaceDark,
    onSurfaceVariant = WarmOnSurfaceVariantDark,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = RosePrimary,
    onPrimary = RoseOnPrimary,
    primaryContainer = RosePrimaryContainer,
    onPrimaryContainer = RoseOnPrimaryContainer,
    secondary = BlushSecondary,
    onSecondary = BlushOnSecondary,
    secondaryContainer = BlushSecondaryContainer,
    onSecondaryContainer = BlushOnSecondaryContainer,
    tertiary = AmberTertiary,
    onTertiary = AmberOnTertiary,
    tertiaryContainer = AmberTertiaryContainer,
    onTertiaryContainer = AmberOnTertiaryContainer,
    background = WarmBackgroundLight,
    surface = WarmSurfaceLight,
    surfaceVariant = WarmSurfaceVariantLight,
    onBackground = WarmOnSurfaceLight,
    onSurface = WarmOnSurfaceLight,
    onSurfaceVariant = WarmOnSurfaceVariantLight,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep intentional romantic couple aesthetic
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
