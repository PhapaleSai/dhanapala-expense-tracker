package com.phapalesai.dhanapala.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.phapalesai.dhanapala.ui.screens.wrapped.WrappedStats
import java.io.File
import java.io.FileOutputStream

/** Draws the Dhanpal Wrapped share card with plain android.graphics — no Compose capture APIs needed. */
object WrappedImageGenerator {
    private const val WIDTH = 1080
    private const val HEIGHT = 1920

    fun generate(context: Context, stats: WrappedStats): Uri {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, HEIGHT.toFloat(),
                Color.parseColor("#0B0C0F"), Color.parseColor("#1B1207"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), bgPaint)

        val gold = Paint().apply { color = Color.parseColor("#FFC857"); isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD }
        val green = Paint().apply { color = Color.parseColor("#00E5A0"); isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD }
        val white = Paint().apply { color = Color.WHITE; isAntiAlias = true }
        val muted = Paint().apply { color = Color.parseColor("#A0A0A8"); isAntiAlias = true }
        val left = 64f
        val maxWidth = WIDTH - 128f

        var y = 180f
        gold.textSize = 46f
        canvas.drawText("धनपाल WRAPPED", left, y, gold)
        y += 56f
        muted.textSize = 30f
        canvas.drawText(stats.periodLabel, left, y, muted)

        y += 150f
        white.textSize = 30f
        canvas.drawText("Total spent", left, y, white)
        y += 96f
        green.textSize = 84f
        canvas.drawText(CurrencyFormat.rupees(stats.totalSpent), left, y, green)

        y += 110f
        white.textSize = 30f
        canvas.drawText("Top category", left, y, white)
        y += 58f
        gold.textSize = 42f
        canvas.drawText("${stats.topCategory ?: "—"}   ${CurrencyFormat.rupees(stats.topCategoryAmount)}", left, y, gold)

        y += 100f
        white.textSize = 30f
        canvas.drawText("Biggest single hit", left, y, white)
        y += 58f
        gold.textSize = 42f
        canvas.drawText("${stats.biggestExpenseCategory ?: "—"}   ${CurrencyFormat.rupees(stats.biggestExpenseAmount)}", left, y, gold)

        y += 100f
        white.textSize = 30f
        canvas.drawText("${stats.transactionCount} transactions logged", left, y, white)

        if (stats.zeroSpendStreak >= 3) {
            y += 60f
            green.textSize = 30f
            canvas.drawText("🔥 ${stats.zeroSpendStreak}-day zero-spend streak", left, y, green)
        }

        y += 140f
        muted.textSize = 34f
        y = drawWrapped(canvas, stats.highlightLine, left, y, maxWidth, muted)

        muted.textSize = 26f
        canvas.drawText("Made with Dhanpal", left, HEIGHT - 80f, muted)

        val file = File(context.cacheDir, "dhanpal-wrapped-${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun drawWrapped(canvas: Canvas, text: String, x: Float, startY: Float, maxWidth: Float, paint: Paint): Float {
        var y = startY
        var line = StringBuilder()
        text.split(" ").forEach { word ->
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                canvas.drawText(line.toString(), x, y, paint)
                y += 44f
                line = StringBuilder(word)
            } else {
                line = StringBuilder(candidate)
            }
        }
        if (line.isNotEmpty()) canvas.drawText(line.toString(), x, y, paint)
        return y
    }
}
