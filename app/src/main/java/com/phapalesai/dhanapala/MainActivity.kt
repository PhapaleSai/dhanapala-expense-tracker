package com.phapalesai.dhanapala

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.phapalesai.dhanapala.navigation.DhanapalaNavHost
import com.phapalesai.dhanapala.ui.lock.BiometricLockGate
import com.phapalesai.dhanapala.ui.theme.DhanapalaTheme

class MainActivity : FragmentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            DhanapalaApp(activity = this)
        }
    }
}

@Composable
fun DhanapalaApp(activity: FragmentActivity) {
    DhanapalaTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            BiometricLockGate(activity = activity) {
                DhanapalaNavHost()
            }
        }
    }
}
