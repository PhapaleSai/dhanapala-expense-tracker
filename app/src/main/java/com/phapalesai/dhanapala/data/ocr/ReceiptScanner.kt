package com.phapalesai.dhanapala.data.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Runs on-device ML Kit text recognition over a captured receipt photo -- nothing leaves the device. */
class ReceiptScanner(private val context: Context) {

    suspend fun recognizeText(imageUri: Uri): String = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromFilePath(context, imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText -> continuation.resume(visionText.text) }
                .addOnFailureListener { error -> continuation.resumeWithException(error) }
        } catch (error: Exception) {
            continuation.resumeWithException(error)
        }
    }
}
