package com.phapalesai.dhanapala.ui.screens.brokeometer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phapalesai.dhanapala.domain.BhaiMessageEngine
import com.phapalesai.dhanapala.domain.RoastLevel
import com.phapalesai.dhanapala.domain.roastLanguageEnum
import com.phapalesai.dhanapala.ui.screens.quickbudget.QuickBudgetViewModel
import com.phapalesai.dhanapala.ui.theme.DhanapalaGold
import com.phapalesai.dhanapala.ui.theme.DhanapalaGreen
import com.phapalesai.dhanapala.ui.theme.DhanapalaRed
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun BrokeOMeterScreen(viewModel: QuickBudgetViewModel = viewModel(), onDismiss: () -> Unit = {}) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val verdict = remember(summary.percentUsed, settings.roastLanguage) {
        // Always Savage here, regardless of the user's chosen roast level — this
        // screen exists purely for a fun gut-check, not a routine notification.
        BhaiMessageEngine.budgetReaction(
            summary = summary,
            language = settings.roastLanguageEnum,
            level = RoastLevel.SAVAGE,
            random = Random(System.currentTimeMillis())
        )
    }

    val gaugeColor = when {
        summary.percentUsed >= 90 -> DhanapalaRed
        summary.percentUsed >= 60 -> DhanapalaGold
        else -> DhanapalaGreen
    }

    val animatedPercent by animateFloatAsState(
        targetValue = (summary.percentUsed / 100.0).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(900),
        label = "brokeOMeter"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("📳 Broke-o-Meter", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    val strokeWidth = 18.dp.toPx()
                    drawArc(
                        color = Color.White.copy(alpha = 0.1f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = gaugeColor,
                        startAngle = 135f,
                        sweepAngle = 270f * animatedPercent,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${(summary.percentUsed).roundToInt()}%",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = gaugeColor
                    )
                    Text("of budget gone", style = MaterialTheme.typography.labelSmall)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.radialGradient(listOf(gaugeColor.copy(alpha = 0.14f), Color.Transparent))
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = verdict,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                "Tap anywhere to dismiss",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
