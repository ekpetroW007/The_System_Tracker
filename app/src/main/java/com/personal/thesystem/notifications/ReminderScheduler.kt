package com.personal.thesystem.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
    WARNING(401, "Скоро пора завершать день", "Через 15 минут цифровой отбой. Спокойно закончи текущее и оставь новое на завтра."),
    CUTOFF(402, "Пора отдохнуть от экранов", "Закрой ноутбук и поставь телефон на зарядку. На сегодня ты уже сделал достаточно."),
    PREPARATION(403, "Немного заботы о себе", "Подготовься ко сну без спешки: душ, зубы и вещи на утро. Пусть вечер закончится спокойно."),
    BED(404, "Время отдыхать", "Ложись в кровать и выключай свет. Завтра будет новый день. Спокойной ночи!"),
    MORNING(405, "Доброе утро!", "Начни день с отжиманий в своём темпе, а потом отметь результат. У тебя получится!"),
    DIET(406, "Как прошло питание сегодня?", "Загляни в The System и отметь «ДА» или «НЕТ»: без сладкого и без чипсов. Газировку можно."),
}

object ReminderScheduler {
    const val CHANNEL_ID = "system_reminders"

    fun canScheduleExactly(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    fun exactAlarmPermissionIntent(context: Context): Intent =
        Intent(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            Uri.parse("package:${context.packageName}"),
        )

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Напоминания Системы",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Питание, цифровой отбой, подготовка ко сну и утренние отжимания"
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
        if (settings.dietEnabled) schedule(context, ReminderType.DIET, settings.dietTime)
    }

    private fun schedule(context: Context, type: ReminderType, time: LocalTime) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = pendingIntent(context, type)
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(time)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val triggerAt = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (!canScheduleExactly(context)) {
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
