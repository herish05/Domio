package com.domio.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.domio.app.core.ui.theme.*

private val DomioLightColorScheme = lightColorScheme(

    primary = LightPrimary,
    onPrimary = LightSurface,

    primaryContainer = LightPrimarySoft,
    onPrimaryContainer = LightPrimaryDark,

    secondary = AccentPurple,
    onSecondary = LightSurface,

    background = LightBackground,
    onBackground = LightText,

    surface = LightSurface,
    onSurface = LightText,

    surfaceVariant = LightPrimarySoft,
    onSurfaceVariant = LightTextSecondary,

    outline = LightBorder,

    error = Danger,
    onError = LightSurface
)

private val DomioDarkColorScheme = darkColorScheme(

    primary = DarkPrimary,
    onPrimary = DarkBackground,

    primaryContainer = DarkPrimarySoft,
    onPrimaryContainer = DarkPrimary,

    secondary = AccentPurpleDark,
    onSecondary = DarkBackground,

    background = DarkBackground,
    onBackground = DarkText,

    surface = DarkSurface,
    onSurface = DarkText,

    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,

    outline = DarkBorder,

    error = DangerDark,
    onError = DarkBackground
)

@Composable
fun DomioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colorScheme =
        if (darkTheme) {
            DomioDarkColorScheme
        } else {
            DomioLightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}