package com.domio.app.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

class ThemeState(private val context: Context? = null) {
    private val prefs: SharedPreferences? = context?.applicationContext?.getSharedPreferences("domio_theme_prefs", Context.MODE_PRIVATE)

    var isDark by mutableStateOf(
        prefs?.getBoolean("is_dark_mode", false) ?: false
    )
        private set

    fun toggleTheme() {
        setTheme(!isDark)
    }

    fun setTheme(dark: Boolean) {
        isDark = dark
        prefs?.edit()?.putBoolean("is_dark_mode", dark)?.apply()
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
