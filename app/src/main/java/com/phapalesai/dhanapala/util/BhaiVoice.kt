package com.phapalesai.dhanapala.util

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.phapalesai.dhanapala.domain.RoastLanguage
import java.util.Locale

/** On-device text-to-speech only — no audio ever leaves the device. */
class BhaiVoice(context: Context) {
    private var ready = false
    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        ready = status == TextToSpeech.SUCCESS
    }

    fun speak(text: String, language: RoastLanguage) {
        if (!ready || text.isBlank()) return
        val locale = when (language) {
            RoastLanguage.HI -> Locale("hi", "IN")
            RoastLanguage.MR -> Locale("mr", "IN")
            RoastLanguage.EN -> Locale.US
        }
        val result = runCatching { tts.setLanguage(locale) }.getOrNull()
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            runCatching { tts.language = Locale.getDefault() }
        }
        // Strip emoji before speaking — most TTS engines just skip them, but some read
        // out unwanted symbol names instead.
        val clean = text.replace(Regex("[\\p{So}\\p{Cn}]"), "").trim()
        if (clean.isNotBlank()) {
            tts.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "bhai_voice")
        }
    }

    fun shutdown() {
        runCatching {
            tts.stop()
            tts.shutdown()
        }
    }
}

@Composable
fun rememberBhaiVoice(): BhaiVoice {
    val context = LocalContext.current
    val voice = remember { BhaiVoice(context) }
    DisposableEffect(Unit) {
        onDispose { voice.shutdown() }
    }
    return voice
}
