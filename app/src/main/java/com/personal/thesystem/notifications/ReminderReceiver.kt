package com.personal.thesystem.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.personal.thesystem.MainActivity
import com.personal.thesystem.R
import com.personal.thesystem.data.SystemRepository
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val type = intent?.getStringExtra(EXTRA_TYPE)?.let {
            runCatching { ReminderType.valueOf(it) }.getOrNull()
        } ?: return

        val repository = SystemRepository(context)
        if (!type.shouldNotify(repository.recordFor(LocalDate.now()))) {
            ReminderScheduler.scheduleNext(context, type, repository.settings)
            return
        }

        ReminderScheduler.createChannel(context)
        val openApp = PendingIntent.getActivity(
            context,
            500,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(type.title)
            .setContentText(type.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(type.message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()

        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (permissionGranted) {
            NotificationManagerCompat.from(context).notify(type.requestCode, notification)
        }

        ReminderScheduler.scheduleNext(context, type, repository.settings)
    }

    companion object {
        const val EXTRA_TYPE = "reminder_type"
    }
}
