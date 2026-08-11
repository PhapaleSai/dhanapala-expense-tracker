package com.phapalesai.dhanapala.ui.screens.split

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.phapalesai.dhanapala.domain.SplitCalculator
import com.phapalesai.dhanapala.util.CurrencyFormat

private data class SplitPerson(val name: String, val customAmount: String)

@Composable
fun SplitCalculatorScreen() {
    val context = LocalContext.current
    var totalText by remember { mutableStateOf("") }
    var splitEqually by remember { mutableStateOf(true) }
    var people by remember {
        mutableStateOf(listOf(SplitPerson("Person 1", ""), SplitPerson("Person 2", "")))
    }

    val total = totalText.toDoubleOrNull() ?: 0.0
    val equalShares = SplitCalculator.splitEqually(total, people.size)
    val shares: List<Double> = if (splitEqually) {
        equalShares
    } else {
        people.map { it.customAmount.toDoubleOrNull() ?: 0.0 }
    }
    val customTotal = shares.sum()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Split a Bill", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Even split, or set custom shares -- share the summary straight to WhatsApp.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = totalText,
                onValueChange = { totalText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Total bill (₹)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Split equally", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = splitEqually, onCheckedChange = { splitEqually = it })
            }

            people.forEachIndexed { index, person ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = person.name,
                        onValueChange = { newName ->
                            people = people.toMutableList().also { it[index] = it[index].copy(name = newName) }
                        },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (splitEqually) {
                        Text(
                            CurrencyFormat.rupees(equalShares.getOrElse(index) { 0.0 }),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    } else {
                        OutlinedTextField(
                            value = person.customAmount,
                            onValueChange = { newAmount ->
                                people = people.toMutableList().also {
                                    it[index] = it[index].copy(customAmount = newAmount.filter { c -> c.isDigit() || c == '.' })
                                }
                            },
                            label = { Text("₹") },
                            singleLine = true,
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                    IconButton(
                        onClick = { people = people.toMutableList().also { it.removeAt(index) } },
                        enabled = people.size > 1
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove ${person.name}", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            OutlinedButton(
                onClick = { people = people + SplitPerson("Person ${people.size + 1}", "") }
            ) { Text("+ Add person") }

            if (!splitEqually && total > 0) {
                val diff = total - customTotal
                Text(
                    if (kotlin.math.abs(diff) < 0.01) {
                        "Custom shares match the total ✅"
                    } else if (diff > 0) {
                        "₹${"%.2f".format(diff)} not yet assigned."
                    } else {
                        "Shares exceed the total by ₹${"%.2f".format(-diff)}."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (kotlin.math.abs(diff) < 0.01) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Summary", style = MaterialTheme.typography.titleMedium)
                    people.forEachIndexed { index, person ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(person.name.ifBlank { "Person ${index + 1}" })
                            Text(CurrencyFormat.rupees(shares.getOrElse(index) { 0.0 }), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val summary = buildString {
                        append("🧾 Split Bill\n")
                        append("Total: ${CurrencyFormat.rupees(total)}\n\n")
                        people.forEachIndexed { index, person ->
                            append("${person.name.ifBlank { "Person ${index + 1}" }}: ${CurrencyFormat.rupees(shares.getOrElse(index) { 0.0 })}\n")
                        }
                        append("\nSplit via Dhanpal")
                    }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, summary)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share split"))
                },
                enabled = total > 0,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Share Summary") }
        }
    }
}
