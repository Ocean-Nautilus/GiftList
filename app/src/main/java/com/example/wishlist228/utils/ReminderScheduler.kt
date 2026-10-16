package com.example.wishlist228.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/** Starts and stops the background check of upcoming birthdays (WorkManager). */
object ReminderScheduler {

    private const val PERIODIC_WORK_NAME = "birthday_reminder_daily"

    /** Runs the check once a day. Calling it again keeps the existing schedule. */
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<BirthdayReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
    }

    /** Runs the check right now and always shows a notification (used by the "test" button). */
    fun runNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<BirthdayReminderWorker>()
            .setInputData(workDataOf(BirthdayReminderWorker.KEY_FORCE to true))
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
