package com.phapalesai.dhanapala.ui.lock

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phapalesai.dhanapala.DhanapalaApplication
import com.phapalesai.dhanapala.data.local.AppSettingsEntity

private const val AUTHENTICATORS =
    BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL

/** True if the device has a usable biometric sensor or a device PIN/pattern set up. */
fun canUseBiometricLock(context: Context): Boolean =
    BiometricManager.from(context).canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS

/**
 * Gates [content] behind a biometric/device-credential prompt when the user
 * has turned the setting on. Fails open (shows content) if the setting is
 * off, or if the device has no biometric/PIN set up at all -- this is a
 * privacy nicety, not a security boundary, and should never permanently
 * lock the user out of their own data.
 */
@Composable
fun BiometricLockGate(activity: FragmentActivity, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as DhanapalaApplication
    val settings by app.budgetRepository.observeSettings()
        .collectAsStateWithLifecycle(initialValue = null as AppSettingsEntity?)

    when (val current = settings) {
        null -> {
            // Settings not loaded yet -- show a blank frame rather than a
            // flash of real content while we don't yet know if lock is on.
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {}
        }
        else -> {
            val lockEnabled = current.biometricLockEnabled && canUseBiometricLock(context)
            var unlocked by remember { mutableStateOf(false) }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner, lockEnabled) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP && lockEnabled) {
                        unlocked = false
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            if (!lockEnabled || unlocked) {
                content()
            } else {
                LockedScreen(onUnlock = { showBiometricPrompt(activity) { unlocked = true } })
                LaunchedEffect(Unit) {
                    showBiometricPrompt(activity) { unlocked = true }
                }
            }
        }
    }
}

private fun showBiometricPrompt(activity: FragmentActivity, onSuccess: () -> Unit) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
        }
    )
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock Dhanpal")
        .setSubtitle("Your money, your privacy")
        .setAllowedAuthenticators(AUTHENTICATORS)
        .build()
    prompt.authenticate(promptInfo)
}

@Composable
private fun LockedScreen(onUnlock: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                "Dhanpal is locked",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            Text(
                "Unlock to see your money.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onUnlock, modifier = Modifier.padding(top = 24.dp)) { Text("Unlock") }
        }
    }
}
