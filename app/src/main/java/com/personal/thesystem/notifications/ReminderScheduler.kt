package com.personal.thesystem.notifications

import android.app.AlarmManager
import android.app.ActivityOptions
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.personal.thesystem.data.SystemRepository
import com.personal.thesystem.model.DailyRecord
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
    ;

    fun shouldNotify(record: DailyRecord): Boolean = this != DIET || record.diet == null
}

object ReminderScheduler {
    const val CHANNEL_ID = "system_reminders"
    private const val MUSIC_REQUEST_CODE = 407
    private const val MUSIC_WATCHDOG_REQUEST_CODE = 408
    private val MUSIC_TIME: LocalTime = LocalTime.of(7, 30)

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
                    description = "Питание, цифровой отбой, подготовка ко сну, отжимания и утренняя музыка"
                    enableVibration(true)
                }
            )
        }
    }

    fun scheduleAll(context: Context, settings: SystemSettings) {
        createChannel(context)
        ReminderType.entries.forEach { cancel(context, it) }
        cancelMusic(context)
        if (settings.morningMusicEnabled) scheduleMusic(context)
        if (!settings.notificationsEnabled) return

        if (settings.warningEnabled) schedule(context, ReminderType.WARNING, settings.digitalCutoff.minusMinutes(15))
        if (settings.cutoffEnabled) schedule(context, ReminderType.CUTOFF, settings.digitalCutoff)
        if (settings.preparationEnabled) schedule(context, ReminderType.PREPARATION, settings.bedTime.minusMinutes(30))
        if (settings.bedEnabled) schedule(context, ReminderType.BED, settings.bedTime)
        if (settings.morningEnabled) schedule(context, ReminderType.MORNING, settings.morningTime)
        if (settings.dietEnabled) schedule(context, ReminderType.DIET, settings.dietTime)
    }

    fun scheduleNext(context: Context, type: ReminderType, settings: SystemSettings) {
        val time = when (type) {
            ReminderType.WARNING -> settings.digitalCutoff.minusMinutes(15).takeIf { settings.warningEnabled }
            ReminderType.CUTOFF -> settings.digitalCutoff.takeIf { settings.cutoffEnabled }
            ReminderType.PREPARATION -> settings.bedTime.minusMinutes(30).takeIf { settings.preparationEnabled }
            ReminderType.BED -> settings.bedTime.takeIf { settings.bedEnabled }
            ReminderType.MORNING -> settings.morningTime.takeIf { settings.morningEnabled }
            ReminderType.DIET -> settings.dietTime.takeIf { settings.dietEnabled }
        }
        if (settings.notificationsEnabled && time != null) schedule(context, type, time)
    }

    fun scheduleMusic(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(MUSIC_TIME)
        if (!next.isAfter(now)) next = next.plusDays(1)
        scheduleAlarm(context, alarmManager, next, musicPendingIntent(context))
        scheduleAlarm(context, alarmManager, next.plusMinutes(1), musicWatchdogPendingIntent(context))
    }

    fun cancelMusic(context: Context) {
        context.getSystemService(AlarmManager::class.java).apply {
            cancel(musicPendingIntent(context))
            cancel(musicWatchdogPendingIntent(context))
        }
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

    fun musicPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MorningMusicActivity::class.java)
        val options = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            ActivityOptions.makeBasic().apply {
                pendingIntentCreatorBackgroundActivityStartMode = ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
            }.toBundle()
        } else {
            null
        }
        return PendingIntent.getActivity(
            context,
            MUSIC_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            options,
        )
    }

    private fun musicWatchdogPendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        MUSIC_WATCHDOG_REQUEST_CODE,
        Intent(context, MorningMusicWatchdogReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        time: LocalDateTime,
        pendingIntent: PendingIntent,
    ) {
        val triggerAt = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (!canScheduleExactly(context)) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }
}

class RescheduleReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        ReminderScheduler.scheduleAll(context, SystemRepository(context).settings)
    }
}
