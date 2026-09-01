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
import com.personal.thesystem.model.DailyRecord
import com.personal.thesystem.model.SystemSettings
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

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
    WEEKLY_REVIEW(412, "Подведем итоги!", "Ответь на четыре вопроса о следующей неделе."),
    ;

    val channelId: String
        get() = when (this) {
            WARNING, CUTOFF, PREPARATION, BED -> ReminderScheduler.CHANNEL_EVENING
            MORNING, DIET -> ReminderScheduler.CHANNEL_CHECK_INS
            WEEKLY_REVIEW -> ReminderScheduler.CHANNEL_STUDY
        }

    val destination: String
        get() = when (this) {
            WARNING, CUTOFF, PREPARATION, BED -> "sleep"
            MORNING -> "morning"
            DIET -> "diet"
            WEEKLY_REVIEW -> "weekly_review"
        }

    fun shouldNotify(record: DailyRecord): Boolean = when (this) {
        WARNING, CUTOFF, PREPARATION, BED -> record.sleep == null
        MORNING -> record.morning == null
        DIET -> record.diet == null
        WEEKLY_REVIEW -> true
    }
}

object ReminderScheduler {
    const val CHANNEL_EVENING = "system_evening"
    const val CHANNEL_CHECK_INS = "system_check_ins"
    const val CHANNEL_MUSIC = "system_music"
    const val CHANNEL_MUSIC_ENGINE = "system_music_engine"
    const val CHANNEL_STUDY = "system_study"
    private const val OBSOLETE_MORNING_MUSIC_REQUEST_CODE = 407
    private const val OBSOLETE_MORNING_WATCHDOG_REQUEST_CODE = 408
    private const val CUTOFF_MUSIC_REQUEST_CODE = 409
    private val CUTOFF_MUSIC_OFFSETS = listOf(0L, 5L, 10L)

    fun canScheduleExactly(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    fun exactAlarmPermissionIntent(context: Context): Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Intent(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            Uri.parse("package:${context.packageName}"),
        )
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            listOf(
                Triple(CHANNEL_EVENING, "Вечер и сон", "Цифровой отбой и подготовка ко сну"),
                Triple(CHANNEL_CHECK_INS, "Ежедневные отметки", "Утро, питание и незакрытые задачи"),
                Triple(CHANNEL_MUSIC, "Музыка", "Плейлист для пробежки"),
                Triple(CHANNEL_STUDY, "Учёба", "Расписание, дорога, занятия и планирование недели"),
            ).forEach { (id, name, description) ->
                manager.createNotificationChannel(
                    NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH).apply {
                        this.description = description
                        enableVibration(true)
                    }
                )
            }
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_MUSIC_ENGINE, "Воспроизведение", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Цифровой отбой и плейлист для пробежки"
                    setSound(null, null)
                    enableVibration(false)
                }
            )
        }
    }

    fun scheduleAll(context: Context, settings: SystemSettings) {
        createChannel(context)
        ReminderType.entries.forEach { cancel(context, it) }
        cancelObsoleteMorningMusic(context)
        cancelCutoffMusic(context)
        if (settings.cutoffEnabled) scheduleCutoffMusic(context, settings.digitalCutoff)
        if (!settings.notificationsEnabled) return

        if (settings.warningEnabled) schedule(context, ReminderType.WARNING, settings.digitalCutoff.minusMinutes(15))
        if (settings.cutoffEnabled) schedule(context, ReminderType.CUTOFF, settings.digitalCutoff)
        if (settings.preparationEnabled) schedule(context, ReminderType.PREPARATION, settings.bedTime.minusMinutes(30))
        if (settings.bedEnabled) schedule(context, ReminderType.BED, settings.bedTime)
        if (settings.morningEnabled) schedule(context, ReminderType.MORNING, settings.morningTime)
        if (settings.dietEnabled) schedule(context, ReminderType.DIET, settings.dietTime)
        scheduleWeeklyReview(context)
    }

    fun scheduleNext(context: Context, type: ReminderType, settings: SystemSettings) {
        if (type == ReminderType.WEEKLY_REVIEW) {
            if (settings.notificationsEnabled) scheduleWeeklyReview(context)
            return
        }
        val time = when (type) {
            ReminderType.WARNING -> settings.digitalCutoff.minusMinutes(15).takeIf { settings.warningEnabled }
            ReminderType.CUTOFF -> settings.digitalCutoff.takeIf { settings.cutoffEnabled }
            ReminderType.PREPARATION -> settings.bedTime.minusMinutes(30).takeIf { settings.preparationEnabled }
            ReminderType.BED -> settings.bedTime.takeIf { settings.bedEnabled }
            ReminderType.MORNING -> settings.morningTime.takeIf { settings.morningEnabled }
            ReminderType.DIET -> settings.dietTime.takeIf { settings.dietEnabled }
            ReminderType.WEEKLY_REVIEW -> null
        }
        if (settings.notificationsEnabled && time != null) schedule(context, type, time)
    }

    fun scheduleCutoffMusic(context: Context, digitalCutoff: LocalTime) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val now = LocalDateTime.now()
        cutoffMusicTimes(digitalCutoff).forEachIndexed { index, time ->
            var next = now.toLocalDate().atTime(time)
            if (!next.isAfter(now)) next = next.plusDays(1)
            scheduleAlarm(context, alarmManager, next, cutoffMusicPendingIntent(context, index), alarmClock = true)
        }
    }

    fun cancelCutoffMusic(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        CUTOFF_MUSIC_OFFSETS.indices.forEach { index ->
            alarmManager.cancel(cutoffMusicPendingIntent(context, index))
            alarmManager.cancel(legacyCutoffMusicPendingIntent(context, index))
        }
    }

    internal fun cutoffMusicTimes(digitalCutoff: LocalTime): List<LocalTime> =
        CUTOFF_MUSIC_OFFSETS.map(digitalCutoff::plusMinutes)

    internal fun nextWeeklyReviewAt(now: LocalDateTime): LocalDateTime {
        var next = now.toLocalDate()
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            .atTime(10, 0)
        if (!next.isAfter(now)) next = next.plusWeeks(1)
        return next
    }

    private fun scheduleWeeklyReview(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        scheduleAlarm(
            context,
            alarmManager,
            nextWeeklyReviewAt(LocalDateTime.now()),
            pendingIntent(context, ReminderType.WEEKLY_REVIEW),
        )
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

    private fun cutoffMusicPendingIntent(context: Context, index: Int): PendingIntent {
        val intent = Intent(context, MusicAlarmReceiver::class.java).setAction(MusicAlarmReceiver.ACTION_PLAY_CUTOFF)
        return PendingIntent.getBroadcast(
            context,
            CUTOFF_MUSIC_REQUEST_CODE + index,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun legacyCutoffMusicPendingIntent(context: Context, index: Int): PendingIntent = PendingIntent.getBroadcast(
        context,
        CUTOFF_MUSIC_REQUEST_CODE + index,
        Intent(context, MusicAlarmReceiver::class.java).putExtra("music_mode", "cutoff"),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        time: LocalDateTime,
        pendingIntent: PendingIntent,
        alarmClock: Boolean = false,
    ) {
        val triggerAt = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (!canScheduleExactly(context)) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else if (alarmClock) {
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, pendingIntent), pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun cancelObsoleteMorningMusic(context: Context) {
        val flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        PendingIntent.getBroadcast(
            context,
            OBSOLETE_MORNING_MUSIC_REQUEST_CODE,
            Intent(context, MusicAlarmReceiver::class.java),
            flags,
        )?.let(alarmManager::cancel)
        PendingIntent.getBroadcast(
            context,
            OBSOLETE_MORNING_WATCHDOG_REQUEST_CODE,
            Intent().setClassName(context.packageName, "${context.packageName}.notifications.MorningMusicWatchdogReceiver"),
            flags,
        )?.let(alarmManager::cancel)
    }

    fun recordMusicDiagnostic(context: Context, message: String) {
        context.getSharedPreferences("music_runtime", Context.MODE_PRIVATE)
            .edit()
            .putString("last_diagnostic", "${java.time.LocalDateTime.now()} · $message")
            .apply()
    }

    fun musicDiagnostic(context: Context): String? =
        context.getSharedPreferences("music_runtime", Context.MODE_PRIVATE)
            .getString("last_diagnostic", null)

    fun scheduleRelevantChanged(before: SystemSettings, after: SystemSettings): Boolean =
        before.digitalCutoff != after.digitalCutoff ||
            before.bedTime != after.bedTime ||
            before.morningTime != after.morningTime ||
            before.dietTime != after.dietTime ||
            before.notificationsEnabled != after.notificationsEnabled ||
            before.warningEnabled != after.warningEnabled ||
            before.cutoffEnabled != after.cutoffEnabled ||
            before.preparationEnabled != after.preparationEnabled ||
            before.bedEnabled != after.bedEnabled ||
            before.morningEnabled != after.morningEnabled ||
            before.dietEnabled != after.dietEnabled

    fun dismissAnswered(context: Context, record: DailyRecord) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (record.morning != null) manager.cancel(ReminderType.MORNING.requestCode)
        if (record.diet != null) manager.cancel(ReminderType.DIET.requestCode)
        if (record.sleep != null) {
            listOf(ReminderType.WARNING, ReminderType.CUTOFF, ReminderType.PREPARATION, ReminderType.BED)
                .forEach { manager.cancel(it.requestCode) }
        }
    }
}

class RescheduleReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val supportedActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
        )
        if (intent?.action !in supportedActions) return
        ReminderScheduler.scheduleAll(context, SystemRepository(context).settings)
    }
}
