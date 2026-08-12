package com.phapalesai.dhanapala.ui.call

import android.content.Intent
import android.media.RingtoneManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phapalesai.dhanapala.MainActivity
import com.phapalesai.dhanapala.domain.BhaiMessageEngine
import com.phapalesai.dhanapala.domain.roastLanguageEnum
import com.phapalesai.dhanapala.domain.roastLevelEnum
import com.phapalesai.dhanapala.ui.screens.quickbudget.QuickBudgetViewModel
import com.phapalesai.dhanapala.ui.theme.DhanapalaGold
import com.phapalesai.dhanapala.ui.theme.DhanapalaGreen
import com.phapalesai.dhanapala.ui.theme.DhanapalaRed
import com.phapalesai.dhanapala.util.CurrencyFormat
import com.phapalesai.dhanapala.util.rememberBhaiVoice
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

@Composable
fun BhaiCallScreen(viewModel: QuickBudgetViewModel = viewModel(), onFinish: () -> Unit) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var answered by remember { mutableStateOf(false) }

    val roast = remember(settings.roastLanguage) {
        BhaiMessageEngine.budgetReaction(summary, settings.roastLanguageEnum, settings.roastLevelEnum, Random(System.currentTimeMillis()))
    }
    val bhaiVoice = rememberBhaiVoice()

    LaunchedEffect(answered) {
        if (answered) return@LaunchedEffect
        val uri = runCatching { RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE) }.getOrNull()
            ?: return@LaunchedEffect
        val ringtone = runCatching { RingtoneManager.getRingtone(context, uri) }.getOrNull() ?: return@LaunchedEffect
        try {
            while (isActive) {
                ringtone.play()
                delay(2500)
                ringtone.stop()
                delay(400)
            }
        } finally {
            ringtone.stop()
        }
    }

    LaunchedEffect(answered) {
        if (answered && settings.voiceRoastsEnabled) bhaiVoice.speak(roast, settings.roastLanguageEnum)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1B0B0B), MaterialTheme.colorScheme.background))),
        contentAlignment = Alignment.Center
    ) {
        if (!answered) {
            RingingContent(
                onAnswer = { answered = true },
                onDecline = onFinish
            )
        } else {
            ConnectedContent(
                remaining = summary.remaining,
                roast = roast,
                onOpenApp = {
                    context.startActivity(
                        Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    )
                    onFinish()
                },
                onHangUp = onFinish
            )
        }
    }
}

@Composable
private fun RingingContent(onAnswer: () -> Unit, onDecline: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulseScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize().padding(vertical = 80.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("📞", style = MaterialTheme.typography.displayLarge, modifier = Modifier.scale(pulse))
            Text("Bhai is calling…", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Budget check-in", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.7f))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CallActionButton(emoji = "❌", color = DhanapalaRed, label = "Decline", onClick = onDecline)
            CallActionButton(emoji = "✅", color = DhanapalaGreen, label = "Answer", onClick = onAnswer)
        }
    }
}

@Composable
private fun CallActionButton(emoji: String, color: Color, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(color)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, style = MaterialTheme.typography.headlineMedium)
        }
        Text(label, color = Color.White, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ConnectedContent(remaining: Double, roast: String, onOpenApp: () -> Unit, onHangUp: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        Text("💰", style = MaterialTheme.typography.displayMedium)
        Text(
            CurrencyFormat.rupees(remaining),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            color = if (remaining < 0) DhanapalaRed else DhanapalaGold
        )
        Text("remaining this period", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
        Text(
            roast,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Button(
            onClick = onOpenApp,
            colors = ButtonDefaults.buttonColors(containerColor = DhanapalaGold),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Open Dhanpal") }
        OutlinedButton(onClick = onHangUp, modifier = Modifier.fillMaxWidth()) { Text("Hang up") }
    }
}
