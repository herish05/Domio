package com.domio.app.core.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecurityManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("domio_security_prefs", Context.MODE_PRIVATE)

    private val _isAppLocked = MutableStateFlow(isMasterLockEnabled)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _isDocumentVaultLocked = MutableStateFlow(isDocumentVaultLockEnabled)
    val isDocumentVaultLocked: StateFlow<Boolean> = _isDocumentVaultLocked.asStateFlow()

    var isMasterLockEnabled: Boolean
        get() = prefs.getBoolean("master_app_lock_enabled", false)
        set(value) {
            prefs.edit().putBoolean("master_app_lock_enabled", value).apply()
            _isAppLocked.value = value
        }

    var isDocumentVaultLockEnabled: Boolean
        get() = prefs.getBoolean("document_vault_lock_enabled", false)
        set(value) {
            prefs.edit().putBoolean("document_vault_lock_enabled", value).apply()
            _isDocumentVaultLocked.value = value
        }

    fun unlockAppSession() {
        _isAppLocked.value = false
    }

    fun lockAppSession() {
        if (isMasterLockEnabled) {
            _isAppLocked.value = true
        }
    }

    fun unlockDocumentVaultSession() {
        _isDocumentVaultLocked.value = false
    }

    fun lockDocumentVaultSession() {
        if (isDocumentVaultLockEnabled) {
            _isDocumentVaultLocked.value = true
        }
    }

    fun isBiometricHardwareAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        description: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Authentication failed. Try again.")
            }
        }

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)

        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        promptInfoBuilder.setAllowedAuthenticators(authenticators)

        try {
            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfoBuilder.build())
        } catch (e: Exception) {
            onError(e.message ?: "Authentication unavailable")
        }
    }
}
