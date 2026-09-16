package com.domio.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.fragment.app.FragmentActivity
import com.domio.app.core.ads.AdManager
import com.domio.app.core.security.SecurityManager
import com.domio.app.ui.theme.DomioTheme
import com.domio.app.ui.theme.LocalThemeState
import com.domio.app.ui.theme.ProvideThemeState
import com.domio.app.ui.theme.ThemeState

class MainActivity : FragmentActivity() {

    lateinit var securityManager: SecurityManager
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        securityManager = SecurityManager(this)
        AdManager.initialize(this)

        setContent {
            val themeState = remember { ThemeState(this) }

            ProvideThemeState(themeState = themeState) {
                DomioTheme(darkTheme = LocalThemeState.current.isDark) {
                    DomioApp(securityManager = securityManager)
                }
            }
        }
    }

}