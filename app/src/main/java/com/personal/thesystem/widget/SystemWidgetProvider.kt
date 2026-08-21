package com.personal.thesystem.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.personal.thesystem.R
import com.personal.thesystem.data.SystemRepository
import com.personal.thesystem.model.DecisionStatus
import com.personal.thesystem.model.SystemLogic
import java.time.LocalDate
import java.time.LocalTime

class SystemWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_ADD_WATER) {
            SystemRepository(context).adjustWater(LocalDate.now(), 1)
            updateAll(context)
        }
    }

    companion object {
        private const val ACTION_ADD_WATER = "com.personal.thesystem.widget.ADD_WATER"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, SystemWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { update(context, manager, it) }
        }

        private fun update(context: Context, manager: AppWidgetManager, id: Int) {
            val repository = SystemRepository(context)
            val record = repository.recordFor(LocalDate.now())
            val quarters = SystemLogic.waterQuarters(record) ?: 0
            val completed = listOf(record.morning, record.light, record.diet, record.water, record.sleep).count { it == DecisionStatus.YES }
            val current = SystemLogic.currentTask(record, LocalTime.now(), repository.settings.digitalCutoff)
            val views = RemoteViews(context.packageName, R.layout.system_widget).apply {
                setTextViewText(R.id.widget_progress, "$completed / 5")
                setTextViewText(R.id.widget_task, current?.let { "СЕЙЧАС · ${it.title}" } ?: "ДЕЛО СДЕЛАНО")
                setTextViewText(R.id.widget_water, "${SystemLogic.formatWaterLiters(quarters)} / 2,5 л")
                setOnClickPendingIntent(
                    R.id.widget_add_water,
                    PendingIntent.getBroadcast(
                        context,
                        id,
                        Intent(context, SystemWidgetProvider::class.java).setAction(ACTION_ADD_WATER),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    ),
                )
                context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { launch ->
                    setOnClickPendingIntent(
                        R.id.widget_root,
                        PendingIntent.getActivity(
                            context,
                            id + 10_000,
                            launch,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                        ),
                    )
                }
            }
            manager.updateAppWidget(id, views)
        }
    }
}
