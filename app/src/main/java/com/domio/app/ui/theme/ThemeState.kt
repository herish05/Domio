package com.domio.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

class ThemeState(initialDark: Boolean = true) {
    var isDark by mutableStateOf(initialDark)
        private set

    fun toggleTheme() {
        isDark = !isDark
    }

    fun setTheme(dark: Boolean) {
        isDark = dark
    }
}

val LocalThemeState = staticCompositionLocalOf { ThemeState() }

@Composable
fun ProvideThemeState(
    themeState: ThemeState,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalThemeState provides themeState) {
        content()
    }
}
