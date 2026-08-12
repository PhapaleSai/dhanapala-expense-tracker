package com.phapalesai.dhanapala.ui.screens.wrapped

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phapalesai.dhanapala.ui.theme.DhanapalaGold
import com.phapalesai.dhanapala.ui.theme.DhanapalaGreen
import com.phapalesai.dhanapala.util.CurrencyFormat
import com.phapalesai.dhanapala.util.WrappedImageGenerator

@Composable
fun WrappedScreen(viewModel: WrappedViewModel = viewModel()) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("🎁 Dhanpal Wrapped", style = MaterialTheme.typography.headlineMedium)
            Text(
                "A shareable recap of this period — generated on-device, nothing uploaded.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, Color(0x331B1207))),
                    )
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("धनपाल WRAPPED", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = DhanapalaGold)
                        Text(stats.periodLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        LabeledStat("Total spent", CurrencyFormat.rupees(stats.totalSpent), DhanapalaGreen, big = true)
                        LabeledStat(
                            "Top category",
                            "${stats.topCategory ?: "—"}  ${CurrencyFormat.rupees(stats.topCategoryAmount)}",
                            DhanapalaGold
                        )
                        LabeledStat(
                            "Biggest single hit",
                            "${stats.biggestExpenseCategory ?: "—"}  ${CurrencyFormat.rupees(stats.biggestExpenseAmount)}",
                            DhanapalaGold
                        )
                        Text("${stats.transactionCount} transactions logged", style = MaterialTheme.typography.bodyMedium)
                        if (stats.zeroSpendStreak >= 3) {
                            Text("🔥 ${stats.zeroSpendStreak}-day zero-spend streak", color = DhanapalaGreen, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            stats.highlightLine,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val uri = WrappedImageGenerator.generate(context, stats)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share your Wrapped"))
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("📤 Share Wrapped") }
        }
    }
}

@Composable
private fun LabeledStat(label: String, value: String, color: androidx.compose.ui.graphics.Color, big: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = if (big) MaterialTheme.typography.displaySmall else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
