package com.domio.app.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.domio.app.ui.theme.LocalThemeState

@Composable
fun ThemeToggleIconButton(
    modifier: Modifier = Modifier
) {
    val themeState = LocalThemeState.current

    IconButton(
        onClick = { themeState.toggleTheme() },
        modifier = modifier
    ) {
        Crossfade(targetState = themeState.isDark, label = "ThemeToggle") { isDark ->
            if (isDark) {
                Icon(
                    imageVector = Icons.Outlined.WbSunny,
                    contentDescription = "Switch to Light Mode",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.DarkMode,
                    contentDescription = "Switch to Dark Mode",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
