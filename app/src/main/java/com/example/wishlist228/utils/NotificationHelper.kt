package com.example.wishlist228.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.wishlist228.MainActivity
import com.example.wishlist228.R

/** Creates the notification channel and shows the birthday reminders. */
object NotificationHelper {

    private const val CHANNEL_ID = "birthday_reminders"

    /** From Android 13 the user has to allow notifications explicitly. */
    fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания о днях рождения",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Подсказывает, что пора выбрать подарок, пока не прошёл день рождения друга"
        }
        manager.createNotificationChannel(channel)
    }

    /** Shows one notification. Does nothing if the user did not allow notifications. */
    @SuppressLint("MissingPermission") // checked by canPostNotifications()
    fun show(context: Context, notificationId: Int, title: String, text: String) {
        if (!canPostNotifications(context)) return
        ensureChannel(context)

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
