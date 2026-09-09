package com.domio.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.domio.app.core.ui.theme.*

private val DomioLightColorScheme = lightColorScheme(
    primary = FantasticPinkLight,
    onPrimary = LightSurface,
    primaryContainer = FantasticPinkSoft,
    onPrimaryContainer = FantasticPinkDark,

    secondary = AccentViolet,
    onSecondary = LightSurface,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightText,

    tertiary = AccentCyan,
    onTertiary = LightSurface,

    background = LightBackground,
    onBackground = LightText,

    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,

    outline = LightBorder,
    error = DangerColor,
    onError = LightSurface
)

private val DomioDarkColorScheme = darkColorScheme(
    primary = FantasticPink,
    onPrimary = DarkBackground,
    primaryContainer = FantasticPinkContainerDark,
    onPrimaryContainer = FantasticPinkSoft,

    secondary = AccentVioletLight,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkText,

    tertiary = AccentCyan,
    onTertiary = DarkBackground,

    background = DarkBackground,
    onBackground = DarkText,

    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,

    outline = DarkBorder,
    error = DangerColor,
    onError = DarkBackground
)

@Composable
fun DomioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DomioDarkColorScheme else DomioLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}