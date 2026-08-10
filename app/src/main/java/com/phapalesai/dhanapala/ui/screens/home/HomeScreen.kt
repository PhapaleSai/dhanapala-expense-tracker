package com.phapalesai.dhanapala.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.phapalesai.dhanapala.R

@Composable
fun HomeScreen(modifier: Modifier = Modifier, onOpenMessages: () -> Unit = {}) {
    Scaffold { innerPadding: PaddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Protector of wealth 💰",
                style = MaterialTheme.typography.bodyMedium
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Dashboard coming soon",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Phase 2 checkpoint: read-only SMS access is wired up.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(onClick = onOpenMessages) {
                Text("View SMS (read-only)")
            }
        }
    }
}
