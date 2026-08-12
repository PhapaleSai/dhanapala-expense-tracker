package com.phapalesai.dhanapala.ui.screens.splitgroups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phapalesai.dhanapala.data.local.SplitGroupEntity
import com.phapalesai.dhanapala.domain.DebtSettlement
import com.phapalesai.dhanapala.util.CurrencyFormat

@Composable
fun SplitGroupsScreen(viewModel: SplitGroupsViewModel = viewModel()) {
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val selectedGroupId by viewModel.selectedGroupId.collectAsStateWithLifecycle()
    val detail by viewModel.selectedGroupDetail.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var groupPendingDelete by remember { mutableStateOf<SplitGroupEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            if (selectedGroupId == null) {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "New group")
                }
            } else {
                FloatingActionButton(onClick = { showAddExpenseDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add expense")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedGroupId == null) {
                Text("🤝 Split Groups", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Track shared expenses across a trip or event, then see who owes who — settled with the fewest possible payments.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (groups.isEmpty()) {
                    Text("No groups yet. Tap + to start one.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    groups.forEach { group ->
                        GroupRow(
                            group,
                            onClick = { viewModel.selectGroup(group.id) },
                            onDelete = { groupPendingDelete = group }
                        )
                    }
                }
            } else {
                detail?.let { d ->
                    val context = LocalContext.current
                    GroupDetailView(
                        detail = d,
                        onBack = { viewModel.selectGroup(null) },
                        onShare = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, buildShareText(d))
                            }
                            context.startActivity(Intent.createChooser(intent, "Share settlement"))
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, participants ->
                viewModel.createGroup(name, participants)
                showCreateDialog = false
            }
        )
    }

    groupPendingDelete?.let { group ->
        AlertDialog(
            onDismissRequest = { groupPendingDelete = null },
            title = { Text("Delete \"${group.name}\"?") },
            text = { Text("This removes the group and every expense logged in it. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGroup(group.id)
                    groupPendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { groupPendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (showAddExpenseDialog) {
        detail?.let { d ->
            val participants = d.group.participants.split(",").filter { it.isNotBlank() }
            AddExpenseDialog(
                participants = participants,
                onDismiss = { showAddExpenseDialog = false },
                onAdd = { description, amount, paidBy, among ->
                    viewModel.addExpense(d.group.id, description, amount, paidBy, among)
                    showAddExpenseDialog = false
                }
            )
        }
    }
}

@Composable
private fun GroupRow(group: SplitGroupEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(group.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    group.participants.split(",").filter { it.isNotBlank() }.joinToString(", "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete ${group.name}", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun buildShareText(detail: GroupDetail): String = buildString {
    appendLine("💰 ${detail.group.name} — settlement summary")
    appendLine()
    appendLine("Balances:")
    detail.balances.entries.sortedByDescending { it.value }.forEach { (name, balance) ->
        val line = when {
            balance > 0.01 -> "  $name gets back ${CurrencyFormat.rupees(balance)}"
            balance < -0.01 -> "  $name owes ${CurrencyFormat.rupees(-balance)}"
            else -> "  $name is settled up"
        }
        appendLine(line)
    }
    if (detail.settlements.isNotEmpty()) {
        appendLine()
        appendLine("Settle up:")
        detail.settlements.forEach { payment ->
            appendLine("  ${payment.from} → ${payment.to}: ${CurrencyFormat.rupees(payment.amount)}")
        }
    }
    appendLine()
    append("— via Dhanpal")
}

@Composable
private fun GroupDetailView(detail: GroupDetail, onBack: () -> Unit, onShare: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("← Back to groups", modifier = Modifier.clickable(onClick = onBack), style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(detail.group.name, style = MaterialTheme.typography.headlineMedium)
            OutlinedButton(onClick = onShare) { Text("📤 Share") }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Balances", style = MaterialTheme.typography.titleMedium)
                detail.balances.entries.sortedByDescending { it.value }.forEach { (name, balance) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = when {
                                balance > 0.01 -> "gets back ${CurrencyFormat.rupees(balance)}"
                                balance < -0.01 -> "owes ${CurrencyFormat.rupees(-balance)}"
                                else -> "settled up"
                            },
                            color = when {
                                balance > 0.01 -> MaterialTheme.colorScheme.primary
                                balance < -0.01 -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        if (detail.settlements.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Settle up (fewest payments)", style = MaterialTheme.typography.titleMedium)
                    detail.settlements.forEach { payment -> SettlementRow(payment) }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Expenses", style = MaterialTheme.typography.titleMedium)
                if (detail.expenses.isEmpty()) {
                    Text("No expenses logged yet.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    detail.expenses.forEach { expense ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(expense.description, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "paid by ${expense.paidBy}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(CurrencyFormat.rupees(expense.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettlementRow(payment: DebtSettlement.Payment) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("${payment.from} → ${payment.to}", style = MaterialTheme.typography.bodyMedium)
        Text(CurrencyFormat.rupees(payment.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CreateGroupDialog(onDismiss: () -> Unit, onCreate: (String, List<String>) -> Unit) {
    var name by remember { mutableStateOf("") }
    var participantsText by remember { mutableStateOf("") }
    val participants = participantsText.split(",").map { it.trim() }.filter { it.isNotBlank() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New split group") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group name (e.g. Goa Trip)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = participantsText,
                    onValueChange = { participantsText = it },
                    label = { Text("Participants, comma-separated") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "e.g. Me, Rahul, Priya",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, participants) },
                enabled = name.isNotBlank() && participants.size >= 2
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddExpenseDialog(
    participants: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String, Double, String, List<String>) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf(participants.firstOrNull() ?: "") }
    var among by remember { mutableStateOf(participants.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("What was it for?") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Paid by", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    participants.forEach { person ->
                        FilterChip(selected = paidBy == person, onClick = { paidBy = person }, label = { Text(person) })
                    }
                }
                Text("Split among", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    participants.forEach { person ->
                        FilterChip(
                            selected = person in among,
                            onClick = { among = if (person in among) among - person else among + person },
                            label = { Text(person) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: return@TextButton
                    onAdd(description, amount, paidBy, among.toList())
                },
                enabled = description.isNotBlank() && amountText.toDoubleOrNull() != null && paidBy.isNotBlank() && among.isNotEmpty()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
