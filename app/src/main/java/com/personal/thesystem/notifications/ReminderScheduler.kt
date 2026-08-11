package com.personal.thesystem.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.personal.thesystem.data.SystemRepository
import com.personal.thesystem.model.SystemSettings
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

enum class ReminderType(
    val requestCode: Int,
    val title: String,
    val message: String,
) {
    WARNING(401, "Скоро цифровой отбой", "15 минут. Заверши последнее видео или игру — не начинай новое."),
    CUTOFF(402, "22:45 · Цифровой отбой", "Закрой ноутбук. Телефон — на зарядку вне спальни."),
    PREPARATION(403, "Подготовка ко сну", "Душ, зубы, одежда на утро. Экранов больше нет."),
    BED(404, "23:30 · В кровати", "Свет выключен. Сегодняшнее решение уже принято."),
    MORNING(405, "Утренний триггер", "Встал — сразу отжимания. Отметь результат в The System."),
}

object ReminderScheduler {
    const val CHANNEL_ID = "system_reminders"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Напоминания Системы",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Цифровой отбой, подготовка ко сну и утренние отжимания"
                    enableVibration(true)
                }
            )
        }
    }

    fun scheduleAll(context: Context, settings: SystemSettings) {
        createChannel(context)
        ReminderType.entries.forEach { cancel(context, it) }
        if (!settings.notificationsEnabled) return

        if (settings.warningEnabled) schedule(context, ReminderType.WARNING, settings.digitalCutoff.minusMinutes(15))
        if (settings.cutoffEnabled) schedule(context, ReminderType.CUTOFF, settings.digitalCutoff)
        if (settings.preparationEnabled) schedule(context, ReminderType.PREPARATION, settings.bedTime.minusMinutes(30))
        if (settings.bedEnabled) schedule(context, ReminderType.BED, settings.bedTime)
        if (settings.morningEnabled) schedule(context, ReminderType.MORNING, settings.morningTime)
    }

    private fun schedule(context: Context, type: ReminderType, time: LocalTime) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = pendingIntent(context, type)
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(time)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val triggerAt = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun cancel(context: Context, type: ReminderType) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context, type))
    }

    private fun pendingIntent(context: Context, type: ReminderType): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).putExtra(ReminderReceiver.EXTRA_TYPE, type.name)
        return PendingIntent.getBroadcast(
            context,
            type.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

class RescheduleReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        ReminderScheduler.scheduleAll(context, SystemRepository(context).settings)
    }
}
