package com.personal.thesystem.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.personal.thesystem.model.DailyRecord
import com.personal.thesystem.model.DecisionStatus
import com.personal.thesystem.model.SystemSettings
import com.personal.thesystem.model.ViolationReason
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

class SystemRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var records by mutableStateOf(loadRecords())
        private set

    var settings by mutableStateOf(loadSettings())
        private set

    fun recordFor(date: LocalDate): DailyRecord = records[date] ?: DailyRecord(date)

    fun setSleep(date: LocalDate, status: DecisionStatus, reason: ViolationReason? = null) {
        val current = recordFor(date)
        saveRecord(current.copy(sleep = status, sleepReason = reason.takeIf { status == DecisionStatus.NO }))
    }

    fun setMorning(date: LocalDate, status: DecisionStatus, reason: ViolationReason? = null) {
        val current = recordFor(date)
        saveRecord(current.copy(morning = status, morningReason = reason.takeIf { status == DecisionStatus.NO }))
    }

    fun updateSettings(transform: (SystemSettings) -> SystemSettings) {
        settings = transform(settings)
        preferences.edit()
            .putString(KEY_CUTOFF, settings.digitalCutoff.toString())
            .putString(KEY_BED, settings.bedTime.toString())
            .putString(KEY_MORNING, settings.morningTime.toString())
            .putBoolean(KEY_NOTIFICATIONS, settings.notificationsEnabled)
            .putBoolean(KEY_WARNING, settings.warningEnabled)
            .putBoolean(KEY_CUTOFF_ENABLED, settings.cutoffEnabled)
            .putBoolean(KEY_PREPARATION, settings.preparationEnabled)
            .putBoolean(KEY_BED_ENABLED, settings.bedEnabled)
            .putBoolean(KEY_MORNING_ENABLED, settings.morningEnabled)
            .putString(KEY_ADMISSION_START, settings.admissionStart.toString())
            .apply()
    }

    private fun saveRecord(record: DailyRecord) {
        val updated = records.toMutableMap().apply { put(record.date, record) }
        records = updated
        val json = JSONObject().apply {
            put("sleep", record.sleep?.name)
            put("morning", record.morning?.name)
            put("sleepReason", record.sleepReason?.id)
            put("morningReason", record.morningReason?.id)
        }
        preferences.edit().putString(RECORD_PREFIX + record.date, json.toString()).apply()
    }

    private fun loadRecords(): Map<LocalDate, DailyRecord> = buildMap {
        preferences.all.forEach { (key, value) ->
            if (!key.startsWith(RECORD_PREFIX) || value !is String) return@forEach
            runCatching {
                val date = LocalDate.parse(key.removePrefix(RECORD_PREFIX))
                val json = JSONObject(value)
                val sleep = json.optString("sleep").takeIf { it.isNotBlank() && it != "null" }
                    ?.let(DecisionStatus::valueOf)
                val morning = json.optString("morning").takeIf { it.isNotBlank() && it != "null" }
                    ?.let(DecisionStatus::valueOf)
                put(
                    date,
                    DailyRecord(
                        date = date,
                        sleep = sleep,
                        morning = morning,
                        sleepReason = ViolationReason.fromId(json.optString("sleepReason")),
                        morningReason = ViolationReason.fromId(json.optString("morningReason")),
                    )
                )
            }
        }
    }

    private fun loadSettings(): SystemSettings = SystemSettings(
        digitalCutoff = preferences.getString(KEY_CUTOFF, null)?.let(LocalTime::parse) ?: LocalTime.of(22, 45),
        bedTime = preferences.getString(KEY_BED, null)?.let(LocalTime::parse) ?: LocalTime.of(23, 30),
        morningTime = preferences.getString(KEY_MORNING, null)?.let(LocalTime::parse) ?: LocalTime.of(7, 30),
        notificationsEnabled = preferences.getBoolean(KEY_NOTIFICATIONS, false),
        warningEnabled = preferences.getBoolean(KEY_WARNING, true),
        cutoffEnabled = preferences.getBoolean(KEY_CUTOFF_ENABLED, true),
        preparationEnabled = preferences.getBoolean(KEY_PREPARATION, true),
        bedEnabled = preferences.getBoolean(KEY_BED_ENABLED, true),
        morningEnabled = preferences.getBoolean(KEY_MORNING_ENABLED, true),
        admissionStart = preferences.getString(KEY_ADMISSION_START, null)?.let(LocalDate::parse) ?: LocalDate.now(),
    )

    companion object {
        private const val PREFS_NAME = "the_system"
        private const val RECORD_PREFIX = "record_"
        private const val KEY_CUTOFF = "digital_cutoff"
        private const val KEY_BED = "bed_time"
        private const val KEY_MORNING = "morning_time"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_WARNING = "warning_enabled"
        private const val KEY_CUTOFF_ENABLED = "cutoff_enabled"
        private const val KEY_PREPARATION = "preparation_enabled"
        private const val KEY_BED_ENABLED = "bed_enabled"
        private const val KEY_MORNING_ENABLED = "morning_enabled"
        private const val KEY_ADMISSION_START = "admission_start"
    }
}
