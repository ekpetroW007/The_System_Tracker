package com.personal.thesystem.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import com.personal.thesystem.R
import com.personal.thesystem.MainActivity
import com.personal.thesystem.data.SystemRepository
import com.personal.thesystem.model.DailyRecord
import com.personal.thesystem.model.DecisionStatus
import com.personal.thesystem.model.SystemLogic
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class SystemWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) =
        ids.forEach { WidgetRenderer.overview(context, manager, it) }

    companion object {
        fun updateAll(context: Context) = WidgetRenderer.updateAll(context)
    }
}

class WaterWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) =
        ids.forEach { WidgetRenderer.water(context, manager, it) }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val delta = when (intent.action) {
            ACTION_WATER_PLUS -> 1
            ACTION_WATER_MINUS -> -1
            else -> return
        }
        SystemRepository(context).adjustWater(LocalDate.now(), delta)
    }
}

class FocusWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) =
        ids.forEach { WidgetRenderer.focus(context, manager, it) }
}

class HseWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) =
        ids.forEach { WidgetRenderer.hse(context, manager, it) }
}

private const val ACTION_WATER_PLUS = "com.personal.thesystem.widget.WATER_PLUS"
private const val ACTION_WATER_MINUS = "com.personal.thesystem.widget.WATER_MINUS"
private val Ru = Locale.forLanguageTag("ru")

private object WidgetRenderer {
    private val providers = listOf(
        SystemWidgetProvider::class.java,
        WaterWidgetProvider::class.java,
        FocusWidgetProvider::class.java,
        HseWidgetProvider::class.java,
    )

    fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val repository = SystemRepository(context)
        providers.forEach { provider ->
            manager.getAppWidgetIds(ComponentName(context, provider)).forEach { id ->
                when (provider) {
                    SystemWidgetProvider::class.java -> overview(context, manager, id, repository)
                    WaterWidgetProvider::class.java -> water(context, manager, id, repository)
                    FocusWidgetProvider::class.java -> focus(context, manager, id, repository)
                    HseWidgetProvider::class.java -> hse(context, manager, id, repository)
                }
            }
        }
    }

    fun overview(context: Context, manager: AppWidgetManager, id: Int, repository: SystemRepository = SystemRepository(context)) {
        val record = repository.recordFor(LocalDate.now())
        val activeTasks = SystemLogic.activeTasks(LocalDate.now(), repository.settings)
        val statuses = activeTasks.map { SystemLogic.statusFor(record, it) }
        val allStatuses = listOf(record.morning, record.light, record.diet, record.water, record.sleep)
        val views = RemoteViews(context.packageName, R.layout.widget_overview).apply {
            setTextViewText(R.id.overview_date, todayLabel())
            setTextViewText(R.id.overview_progress, "${statuses.count { it == DecisionStatus.YES }}/${statuses.size}")
            setTextViewText(R.id.overview_task, currentLabel(record, repository))
            listOf(R.id.overview_morning, R.id.overview_light, R.id.overview_diet, R.id.overview_water, R.id.overview_sleep)
                .zip(allStatuses)
                .forEach { (viewId, status) -> setTextColor(viewId, statusColor(status)) }
            bindOpen(context, id, this, "today")
        }
        manager.updateAppWidget(id, views)
    }

    fun water(context: Context, manager: AppWidgetManager, id: Int, repository: SystemRepository = SystemRepository(context)) {
        val quarters = SystemLogic.waterQuarters(repository.recordFor(LocalDate.now())) ?: 0
        val percent = (quarters * 100 / SystemLogic.WATER_GOAL_QUARTERS).coerceIn(0, 100)
        val views = RemoteViews(context.packageName, R.layout.widget_water).apply {
            setTextViewText(R.id.water_value, SystemLogic.formatWaterLiters(quarters))
            setTextViewText(R.id.water_percent, "$percent%")
            setProgressBar(R.id.water_progress, 100, percent, false)
            setOnClickPendingIntent(R.id.water_minus, waterAction(context, id + 20_000, ACTION_WATER_MINUS))
            setOnClickPendingIntent(R.id.water_plus, waterAction(context, id + 30_000, ACTION_WATER_PLUS))
            bindOpen(context, id + 40_000, this, "water")
        }
        manager.updateAppWidget(id, views)
    }

    fun focus(context: Context, manager: AppWidgetManager, id: Int, repository: SystemRepository = SystemRepository(context)) {
        val record = repository.recordFor(LocalDate.now())
        val task = SystemLogic.recoveryTask(record, LocalTime.now(), repository.settings.digitalCutoff)
            ?: SystemLogic.currentTask(record, LocalTime.now(), repository.settings.digitalCutoff)
        val views = RemoteViews(context.packageName, R.layout.widget_focus).apply {
            setTextViewText(R.id.focus_title, task?.title ?: "ДЕЛО СДЕЛАНО")
            setTextViewText(R.id.focus_action, task?.recoveryAction ?: "Все пять задач закрыты. Супер!")
            setTextViewText(R.id.focus_time, LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
            bindOpen(context, id + 50_000, this, "today")
        }
        manager.updateAppWidget(id, views)
    }

    fun hse(context: Context, manager: AppWidgetManager, id: Int, repository: SystemRepository = SystemRepository(context)) {
        val settings = repository.settings
        val activeAssignments = repository.assignments.count { !it.completed }
        val views = RemoteViews(context.packageName, R.layout.widget_hse).apply {
            if (settings.hseModeEnabled) {
                val plan = repository.hseTransitPlan
                val leaveText = plan?.route?.leaveHomeTime?.takeIf { it.isNotBlank() }
                setTextViewText(R.id.hse_countdown, leaveText?.let { "Выйти в $it" } ?: "Маршрут обновится в приложении")
                setTextViewText(
                    R.id.hse_times,
                    plan?.let { "ПАРА ${SystemLogic.formatTime(it.targetTime)}  ·  АВТОБУС ${it.route.lines}" } ?: "ОТКРОЙ РЕЖИМ ВШЭ",
                )
                setTextViewText(R.id.hse_task, "Заданий: $activeAssignments")
                setViewVisibility(R.id.hse_dot, View.VISIBLE)
            } else {
                setTextViewText(R.id.hse_countdown, "Режим ВШЭ выключен")
                setTextViewText(R.id.hse_times, "Включается в настройках")
                setTextViewText(R.id.hse_task, "Пары, маршрут и задания — в одном месте")
                setViewVisibility(R.id.hse_dot, View.INVISIBLE)
            }
            bindOpen(context, id + 60_000, this, "hse")
        }
        manager.updateAppWidget(id, views)
    }

    private fun currentLabel(record: DailyRecord, repository: SystemRepository): String {
        val task = SystemLogic.currentTask(record, LocalTime.now(), repository.settings.digitalCutoff)
        return task?.let { "СЕЙЧАС · ${it.title}" } ?: "ДЕЛО СДЕЛАНО"
    }

    private fun todayLabel(): String {
        val date = LocalDate.now()
        val day = date.dayOfWeek.getDisplayName(TextStyle.FULL, Ru).uppercase(Ru)
        return "$day · ${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Ru).uppercase(Ru)}"
    }

    private fun statusColor(status: DecisionStatus?): Int = Color.parseColor(
        when (status) {
            DecisionStatus.YES -> "#E8EAEC"
            DecisionStatus.NO -> "#FF716C"
            null -> "#50575E"
        }
    )

    private fun waterAction(context: Context, requestCode: Int, action: String): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, WaterWidgetProvider::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun bindOpen(context: Context, requestCode: Int, views: RemoteViews, destination: String) {
        val launch = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_DESTINATION, destination)
        }
        views.setOnClickPendingIntent(
            R.id.widget_root,
            PendingIntent.getActivity(
                context,
                requestCode,
                launch,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )
    }
}
