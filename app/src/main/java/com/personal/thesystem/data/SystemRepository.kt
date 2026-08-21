package com.personal.thesystem.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.personal.thesystem.model.DailyRecord
import com.personal.thesystem.model.DecisionStatus
import com.personal.thesystem.model.DietViolationReason
import com.personal.thesystem.model.ExperimentFeedback
import com.personal.thesystem.model.SystemSettings
import com.personal.thesystem.model.SystemLogic
import com.personal.thesystem.model.ViolationReason
import com.personal.thesystem.widget.SystemWidgetProvider
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

class SystemRepository(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var records by mutableStateOf(loadRecords())
        private set

    var settings by mutableStateOf(loadSettings())
        private set

    var experimentFeedback by mutableStateOf(loadExperimentFeedback())
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

    fun setLight(date: LocalDate, status: DecisionStatus) {
        val current = recordFor(date)
        saveRecord(current.copy(light = status))
    }

    fun setDiet(date: LocalDate, status: DecisionStatus, reason: DietViolationReason? = null) {
        val current = recordFor(date)
        saveRecord(current.copy(diet = status, dietReason = reason.takeIf { status == DecisionStatus.NO }))
    }

    fun setWater(date: LocalDate, status: DecisionStatus) {
        val current = recordFor(date)
        saveRecord(
            current.copy(
                water = status,
                waterQuarterLiters = if (status == DecisionStatus.YES) SystemLogic.WATER_GOAL_QUARTERS else 0,
            )
        )
    }

    fun adjustWater(date: LocalDate, deltaQuarters: Int): Boolean {
        val current = recordFor(date)
        val before = SystemLogic.waterQuarters(current) ?: 0
        val after = SystemLogic.adjustWaterQuarters(before, deltaQuarters)
        saveRecord(
            current.copy(
                water = SystemLogic.waterStatus(after, date.isBefore(LocalDate.now())),
                waterQuarterLiters = after,
            )
        )
        return before < SystemLogic.WATER_GOAL_QUARTERS && after >= SystemLogic.WATER_GOAL_QUARTERS
    }

    fun clearSleep(date: LocalDate) {
        val current = recordFor(date)
        saveRecord(current.copy(sleep = null, sleepReason = null))
    }

    fun clearMorning(date: LocalDate) {
        val current = recordFor(date)
        saveRecord(current.copy(morning = null, morningReason = null))
    }

    fun clearLight(date: LocalDate) {
        val current = recordFor(date)
        saveRecord(current.copy(light = null))
    }

    fun clearDiet(date: LocalDate) {
        val current = recordFor(date)
        saveRecord(current.copy(diet = null, dietReason = null))
    }

    fun clearWater(date: LocalDate) {
        val current = recordFor(date)
        saveRecord(current.copy(water = null, waterQuarterLiters = null))
    }

    fun setStudyTask(date: LocalDate, task: String) {
        saveRecord(recordFor(date).copy(studyTask = task.take(120)))
    }

    fun reload() {
        records = loadRecords()
        settings = loadSettings()
        experimentFeedback = loadExperimentFeedback()
    }

    fun updateSettings(transform: (SystemSettings) -> SystemSettings) {
        settings = transform(settings)
        preferences.edit()
            .putString(KEY_CUTOFF, settings.digitalCutoff.toString())
            .putString(KEY_BED, settings.bedTime.toString())
            .putString(KEY_MORNING, settings.morningTime.toString())
            .putString(KEY_DIET_TIME, settings.dietTime.toString())
            .putBoolean(KEY_NOTIFICATIONS, settings.notificationsEnabled)
            .putBoolean(KEY_WARNING, settings.warningEnabled)
            .putBoolean(KEY_CUTOFF_ENABLED, settings.cutoffEnabled)
            .putBoolean(KEY_PREPARATION, settings.preparationEnabled)
            .putBoolean(KEY_BED_ENABLED, settings.bedEnabled)
            .putBoolean(KEY_MORNING_ENABLED, settings.morningEnabled)
            .putBoolean(KEY_DIET_ENABLED, settings.dietEnabled)
            .putString(KEY_ADMISSION_START, settings.admissionStart.toString())
            .putString(KEY_LIGHT_START, settings.lightStart.toString())
            .putBoolean(KEY_HSE_MODE, settings.hseModeEnabled)
            .putString(KEY_HSE_FIRST_CLASS, settings.hseFirstClassTime.toString())
            .putInt(KEY_HSE_COMMUTE, settings.hseCommuteMinutes)
            .apply()
        SystemWidgetProvider.updateAll(appContext)
    }

    fun setExperimentFeedback(weekStart: LocalDate, feedback: ExperimentFeedback) {
        experimentFeedback = experimentFeedback + (weekStart to feedback)
        preferences.edit().putString(EXPERIMENT_PREFIX + weekStart, feedback.name).apply()
    }

    private fun saveRecord(record: DailyRecord) {
        val isEmpty = record.sleep == null && record.morning == null && record.light == null && record.diet == null &&
            record.water == null && record.waterQuarterLiters == null && record.studyTask.isBlank()
        val updated = records.toMutableMap().apply {
            if (isEmpty) remove(record.date) else put(record.date, record)
        }
        records = updated
        if (isEmpty) {
            preferences.edit().remove(RECORD_PREFIX + record.date).apply()
            SystemWidgetProvider.updateAll(appContext)
            return
        }
        val json = JSONObject().apply {
            put("sleep", record.sleep?.name)
            put("morning", record.morning?.name)
            put("light", record.light?.name)
            put("diet", record.diet?.name)
            put("water", record.water?.name)
            put("waterQuarterLiters", record.waterQuarterLiters)
            put("sleepReason", record.sleepReason?.id)
            put("morningReason", record.morningReason?.id)
            put("dietReason", record.dietReason?.id)
            put("studyTask", record.studyTask)
        }
        preferences.edit().putString(RECORD_PREFIX + record.date, json.toString()).apply()
        SystemWidgetProvider.updateAll(appContext)
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
                val light = json.optString("light").takeIf { it.isNotBlank() && it != "null" }
                    ?.let(DecisionStatus::valueOf)
                val diet = json.optString("diet").takeIf { it.isNotBlank() && it != "null" }
                    ?.let(DecisionStatus::valueOf)
                val water = json.optString("water").takeIf { it.isNotBlank() && it != "null" }
                    ?.let(DecisionStatus::valueOf)
                val storedWaterQuarters = if (json.has("waterQuarterLiters") && !json.isNull("waterQuarterLiters")) {
                    json.optInt("waterQuarterLiters").coerceIn(0, SystemLogic.WATER_MAX_QUARTERS)
                } else {
                    null
                }
                val waterQuarterLiters = storedWaterQuarters ?: when (water) {
                    DecisionStatus.YES -> SystemLogic.WATER_GOAL_QUARTERS
                    DecisionStatus.NO -> 0
                    null -> null
                }
                val resolvedWater = if (storedWaterQuarters != null) {
                    SystemLogic.waterStatus(storedWaterQuarters, date.isBefore(LocalDate.now()))
                } else {
                    water
                }
                put(
                    date,
                    DailyRecord(
                        date = date,
                        sleep = sleep,
                        morning = morning,
                        light = light,
                        diet = diet,
                        water = resolvedWater,
                        waterQuarterLiters = waterQuarterLiters,
                        sleepReason = ViolationReason.fromId(json.optString("sleepReason")),
                        morningReason = ViolationReason.fromId(json.optString("morningReason")),
                        dietReason = DietViolationReason.fromId(json.optString("dietReason")),
                        studyTask = json.optString("studyTask"),
                    )
                )
            }
        }
    }

    private fun loadExperimentFeedback(): Map<LocalDate, ExperimentFeedback> = buildMap {
        preferences.all.forEach { (key, value) ->
            if (!key.startsWith(EXPERIMENT_PREFIX) || value !is String) return@forEach
            runCatching {
                put(LocalDate.parse(key.removePrefix(EXPERIMENT_PREFIX)), ExperimentFeedback.valueOf(value))
            }
        }
    }

    private fun loadSettings(): SystemSettings = SystemSettings(
        digitalCutoff = preferences.getString(KEY_CUTOFF, null)?.let(LocalTime::parse) ?: LocalTime.of(22, 45),
        bedTime = preferences.getString(KEY_BED, null)?.let(LocalTime::parse) ?: LocalTime.of(23, 30),
        morningTime = preferences.getString(KEY_MORNING, null)?.let(LocalTime::parse) ?: LocalTime.of(7, 30),
        dietTime = preferences.getString(KEY_DIET_TIME, null)?.let(LocalTime::parse) ?: LocalTime.of(21, 0),
        notificationsEnabled = preferences.getBoolean(KEY_NOTIFICATIONS, false),
        warningEnabled = preferences.getBoolean(KEY_WARNING, true),
        cutoffEnabled = preferences.getBoolean(KEY_CUTOFF_ENABLED, true),
        preparationEnabled = preferences.getBoolean(KEY_PREPARATION, true),
        bedEnabled = preferences.getBoolean(KEY_BED_ENABLED, true),
        morningEnabled = preferences.getBoolean(KEY_MORNING_ENABLED, true),
        dietEnabled = preferences.getBoolean(KEY_DIET_ENABLED, true),
        admissionStart = preferences.getString(KEY_ADMISSION_START, null)?.let(LocalDate::parse) ?: LocalDate.now(),
        lightStart = preferences.getString(KEY_LIGHT_START, null)?.let(LocalDate::parse) ?: LocalDate.now().also {
            preferences.edit().putString(KEY_LIGHT_START, it.toString()).apply()
        },
        hseModeEnabled = preferences.getBoolean(KEY_HSE_MODE, false),
        hseFirstClassTime = preferences.getString(KEY_HSE_FIRST_CLASS, null)?.let(LocalTime::parse) ?: LocalTime.of(9, 30),
        hseCommuteMinutes = preferences.getInt(KEY_HSE_COMMUTE, 45).coerceIn(10, 180),
    )

    companion object {
        private const val PREFS_NAME = "the_system"
        private const val RECORD_PREFIX = "record_"
        private const val EXPERIMENT_PREFIX = "experiment_feedback_"
        private const val KEY_CUTOFF = "digital_cutoff"
        private const val KEY_BED = "bed_time"
        private const val KEY_MORNING = "morning_time"
        private const val KEY_DIET_TIME = "diet_time"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_WARNING = "warning_enabled"
        private const val KEY_CUTOFF_ENABLED = "cutoff_enabled"
        private const val KEY_PREPARATION = "preparation_enabled"
        private const val KEY_BED_ENABLED = "bed_enabled"
        private const val KEY_MORNING_ENABLED = "morning_enabled"
        private const val KEY_DIET_ENABLED = "diet_enabled"
        private const val KEY_ADMISSION_START = "admission_start"
        private const val KEY_LIGHT_START = "light_start"
        private const val KEY_HSE_MODE = "hse_mode"
        private const val KEY_HSE_FIRST_CLASS = "hse_first_class"
        private const val KEY_HSE_COMMUTE = "hse_commute"
    }
}
