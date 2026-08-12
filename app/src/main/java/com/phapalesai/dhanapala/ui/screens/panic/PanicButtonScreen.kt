package com.phapalesai.dhanapala.ui.screens.panic

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phapalesai.dhanapala.domain.BhaiMessageEngine
import com.phapalesai.dhanapala.domain.MoneyJokes
import com.phapalesai.dhanapala.domain.roastLanguageEnum
import com.phapalesai.dhanapala.ui.screens.quickbudget.QuickBudgetViewModel
import com.phapalesai.dhanapala.ui.theme.DhanapalaGold
import com.phapalesai.dhanapala.util.CurrencyFormat
import com.phapalesai.dhanapala.util.rememberBhaiVoice
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val COOLDOWN_SECONDS = 60

@Composable
fun PanicButtonScreen(viewModel: QuickBudgetViewModel = viewModel(), onDone: () -> Unit = {}) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var secondsLeft by remember { mutableIntStateOf(COOLDOWN_SECONDS) }
    var finished by remember { mutableStateOf(false) }
    val roast = remember(settings.roastLanguage) {
        BhaiMessageEngine.spendingReaction(
            amount = settings.largeExpenseThreshold,
            largeExpenseThreshold = settings.largeExpenseThreshold,
            language = settings.roastLanguageEnum,
            random = Random(System.currentTimeMillis())
        )
    }
    val budgetJoke = remember(settings.roastLanguage) {
        MoneyJokes.random(settings.roastLanguageEnum, Random(System.currentTimeMillis()))
    }
    val bhaiVoice = rememberBhaiVoice()

    // Siren burst the moment the screen opens — plain built-in tone, no audio asset needed.
    LaunchedEffect(Unit) {
        val toneGen = runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 90) }.getOrNull()
        if (toneGen != null) {
            toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1500)
            delay(1600)
            toneGen.release()
        }
    }

    // Speaks one budget-themed joke aloud, same opt-in toggle as the Home screen's voice roasts.
    LaunchedEffect(settings.voiceRoastsEnabled) {
        if (settings.voiceRoastsEnabled) bhaiVoice.speak(budgetJoke, settings.roastLanguageEnum)
    }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        finished = true
    }

    val progress by animateFloatAsState(
        targetValue = 1f - (secondsLeft / COOLDOWN_SECONDS.toFloat()),
        animationSpec = tween(900, easing = LinearEasing),
        label = "panicProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.error.copy(alpha = 0.18f), MaterialTheme.colorScheme.background)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🚨", style = MaterialTheme.typography.displayMedium)
            Text(
                "Hold up.",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = roast,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val errorColor = MaterialTheme.colorScheme.error
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    val strokeWidth = 12.dp.toPx()
                    drawArc(
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.12f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = if (finished) DhanapalaGold else errorColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = if (finished) "😤" else "$secondsLeft",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${CurrencyFormat.rupees(summary.remaining)} remaining this period",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedButton(
                onClick = {
                    viewModel.recordImpulseAvoided()
                    onDone()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("I'm good, cancel it") }

            Button(
                onClick = onDone,
                enabled = finished,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (finished) "Still want to spend" else "Wait for it…") }

            if (settings.impulsesAvoided > 0) {
                Text(
                    "You've talked yourself out of ${settings.impulsesAvoided} impulse buy${if (settings.impulsesAvoided == 1) "" else "s"} so far 💪",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
