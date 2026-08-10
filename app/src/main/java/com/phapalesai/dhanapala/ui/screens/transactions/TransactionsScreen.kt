package com.phapalesai.dhanapala.ui.screens.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phapalesai.dhanapala.data.local.Category
import com.phapalesai.dhanapala.data.local.TransactionEntity
import com.phapalesai.dhanapala.data.local.TransactionType
import com.phapalesai.dhanapala.ui.categoryEmoji
import com.phapalesai.dhanapala.util.CurrencyFormat
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel = viewModel()) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val availableMonths by viewModel.availableMonths.collectAsStateWithLifecycle()

    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) { Text("+") }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            OutlinedTextField(
                value = filter.query,
                onValueChange = viewModel::setQuery,
                label = { Text("Search merchant / description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter.type == null,
                    onClick = { viewModel.setTypeFilter(null) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = filter.type == TransactionType.DEBIT,
                    onClick = { viewModel.setTypeFilter(TransactionType.DEBIT) },
                    label = { Text("Debit") }
                )
                FilterChip(
                    selected = filter.type == TransactionType.CREDIT,
                    onClick = { viewModel.setTypeFilter(TransactionType.CREDIT) },
                    label = { Text("Credit") }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DropdownFilter(
                    label = "Category",
                    selected = filter.category ?: "All",
                    options = listOf("All") + Category.ALL,
                    onSelect = { viewModel.setCategoryFilter(if (it == "All") null else it) },
                    modifier = Modifier.weight(1f)
                )
                DropdownFilter(
                    label = "Month",
                    selected = filter.month?.toString() ?: "All",
                    options = listOf("All") + availableMonths.map { it.toString() },
                    onSelect = { selection ->
                        viewModel.setMonthFilter(if (selection == "All") null else java.time.YearMonth.parse(selection))
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            if (transactions.isEmpty()) {
                Text(
                    "No transactions match these filters.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionRow(
                            tx = tx,
                            onEditCategory = { editingTransaction = tx },
                            onDelete = { viewModel.delete(tx) }
                        )
                    }
                }
            }
        }
    }

    editingTransaction?.let { tx ->
        CategoryPickerDialog(
            current = tx.category,
            onDismiss = { editingTransaction = null },
            onSelect = { category ->
                viewModel.updateCategory(tx.id, category)
                editingTransaction = null
            }
        )
    }

    if (showAddDialog) {
        AddManualTransactionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { amount, type, category, description ->
                viewModel.addManual(amount, type, category, description, System.currentTimeMillis())
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TransactionRow(tx: TransactionEntity, onEditCategory: () -> Unit, onDelete: () -> Unit) {
    val formatter = remember { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${categoryEmoji(tx.category)} ${tx.category}", style = MaterialTheme.typography.titleMedium)
                Text(formatter.format(Date(tx.dateMillis)), style = MaterialTheme.typography.labelSmall)
                tx.description?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                }
                if (tx.isManual) {
                    Text("Manual entry", style = MaterialTheme.typography.labelSmall)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                val sign = if (tx.type == TransactionType.DEBIT) "-" else "+"
                Text(
                    text = "$sign${CurrencyFormat.rupees(tx.amount)}",
                    color = if (tx.type == TransactionType.DEBIT) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    TextButton(onClick = onEditCategory) { Text("Edit") }
                    IconButton(onClick = onDelete) { Text("🗑") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownFilter(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onSelect(option)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun CategoryPickerDialog(current: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change category") },
        text = {
            Column {
                Category.ALL.forEach { category ->
                    TextButton(onClick = { onSelect(category) }) {
                        Text("${categoryEmoji(category)} $category")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddManualTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (amount: Double, type: TransactionType, category: String, description: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.DEBIT) }
    var category by remember { mutableStateOf(Category.UNCATEGORIZED) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add manual transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == TransactionType.DEBIT,
                        onClick = { type = TransactionType.DEBIT },
                        label = { Text("Debit") }
                    )
                    FilterChip(
                        selected = type == TransactionType.CREDIT,
                        onClick = { type = TransactionType.CREDIT },
                        label = { Text("Credit") }
                    )
                }
                DropdownFilter(
                    label = "Category",
                    selected = category,
                    options = Category.ALL,
                    onSelect = { category = it }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amountText.toDoubleOrNull()?.let { onSave(it, type, category, description) }
                },
                enabled = amountText.toDoubleOrNull() != null
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
