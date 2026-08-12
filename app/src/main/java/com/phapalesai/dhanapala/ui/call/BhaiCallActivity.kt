package com.phapalesai.dhanapala.ui.call

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.phapalesai.dhanapala.ui.theme.DhanapalaTheme

class BhaiCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DhanapalaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BhaiCallScreen(onFinish = { finish() })
                }
            }
        }
    }
}
