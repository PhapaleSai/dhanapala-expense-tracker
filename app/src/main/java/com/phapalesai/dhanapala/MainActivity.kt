package com.phapalesai.dhanapala

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.phapalesai.dhanapala.navigation.DhanapalaNavHost
import com.phapalesai.dhanapala.ui.theme.DhanapalaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DhanapalaApp()
        }
    }
}

@Composable
fun DhanapalaApp() {
    DhanapalaTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DhanapalaNavHost()
        }
    }
}
