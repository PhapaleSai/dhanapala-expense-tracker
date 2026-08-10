package com.phapalesai.dhanapala.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Scaffold { innerPadding: PaddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "धनपाल",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Protector of wealth 💰",
                style = MaterialTheme.typography.bodyMedium
            )

            Card(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Dashboard coming soon",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Phase 1 checkpoint: Compose + Material 3 + Navigation + Room are wired up.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
