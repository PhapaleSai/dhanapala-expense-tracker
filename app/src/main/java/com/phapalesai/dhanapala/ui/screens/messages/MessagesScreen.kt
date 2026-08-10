package com.phapalesai.dhanapala.ui.screens.messages

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phapalesai.dhanapala.data.sms.RawSms
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(viewModel: MessagesViewModel = viewModel()) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var hasPermission by remember {
        mutableStateOf(
            context.checkSelfPermission(Manifest.permission.READ_SMS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) viewModel.loadInbox()
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) viewModel.loadInbox()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("SMS (read-only)") }) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when {
                !hasPermission -> PermissionRequest(
                    onRequest = { permissionLauncher.launch(Manifest.permission.READ_SMS) }
                )
                isLoading -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) { CircularProgressIndicator() }
                else -> MessageList(messages)
            }
        }
    }
}

@Composable
private fun PermissionRequest(onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "धनपाल needs to read your SMS to detect bank transactions.",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "This is strictly read-only. The app never edits, deletes, or sends SMS, " +
                "and never touches banking or UPI apps. Everything stays on this device.",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(onClick = onRequest) { Text("Grant SMS access") }
    }
}

@Composable
private fun MessageList(messages: List<RawSms>) {
    if (messages.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) { Text("No SMS found in inbox.") }
        return
    }
    val formatter = remember { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages, key = { it.id }) { sms ->
            Card {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = sms.address ?: "Unknown sender",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = formatter.format(Date(sms.dateMillis)),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = sms.body,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3
                    )
                }
            }
        }
    }
}
