package com.moneymanager.app.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Schedule this to fire on the 1st of each month (via WorkManager PeriodicWorkRequest set up
 * from MainActivity/Application) to carry each payment mode's closing balance forward as next
 * month's opening balance — matching the Excel workbook's automatic rollover behavior.
 *
 * Wire-up left to the app owner since it depends on how you want to handle exact-time
 * scheduling (WorkManager periodic work has a minimum 15-minute/monthly granularity nuance
 * best decided alongside your notification strategy).
 */
class MonthRolloverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val request = OneTimeWorkRequestBuilder<MonthRolloverWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
