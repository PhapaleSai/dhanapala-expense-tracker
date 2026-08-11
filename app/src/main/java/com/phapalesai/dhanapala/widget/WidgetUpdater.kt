package com.phapalesai.dhanapala.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

/** Fired after any mutation that changes what the home screen widget shows. */
object WidgetUpdater {
    suspend fun refresh(context: Context) {
        DhanpalWidget().updateAll(context)
    }
}
