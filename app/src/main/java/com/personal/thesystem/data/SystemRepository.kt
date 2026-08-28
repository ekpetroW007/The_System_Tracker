package com.personal.thesystem.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.personal.thesystem.model.DailyRecord
import com.personal.thesystem.model.DecisionStatus
import com.personal.thesystem.model.DietViolationReason
import com.personal.thesystem.model.ExperimentFeedback
import com.personal.thesystem.model.MoneyCategory
import com.personal.thesystem.model.MoneyCommitment
import com.personal.thesystem.model.MoneyTransaction
import com.personal.thesystem.model.SystemSettings
import com.personal.thesystem.model.SystemLogic
import com.personal.thesystem.model.AssignmentPriority
import com.personal.thesystem.model.StudyAssignment
import com.personal.thesystem.model.ViolationReason
import com.personal.thesystem.model.SleepViolationPart
import com.personal.thesystem.widget.SystemWidgetProvider
import com.personal.thesystem.notifications.ReminderScheduler
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

class SystemRepository(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val database = SystemDatabase(appContext)

    init {
        migrateLegacyData()
    }

    var records by mutableStateOf(database.loadRecords())
        private set

    var settings by mutableStateOf(loadSettings())
        private set

    var experimentFeedback by mutableStateOf(database.loadExperimentFeedback())
        private set

    var assignments by mutableStateOf(database.loadAssignments())
        private set

    var hseTransitPlan by mutableStateOf(loadHseTransitPlan())
        private set

    var moneyTransactions by mutableStateOf(database.loadMoneyTransactions())
        private set

    var moneyReceivedPeriods by mutableStateOf(database.loadMoneyTransfers())
        private set

    var moneyCommitments by mutableStateOf(database.loadMoneyCommitments())
        private set

    fun recordFor(date: LocalDate): DailyRecord = records[date] ?: DailyRecord(date)

    fun shouldShowPreviousDayReminder(today: LocalDate): Boolean =
        SystemLogic.shouldShowPreviousDayReminder(
            today = today,
            admissionStart = settings.admissionStart,
            previousRecord = recordFor(today.minusDays(1)),
            lastShownOn = preferences.getString(KEY_PREVIOUS_DAY_REMINDER, null)?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            },
            lightStart = settings.lightStart,
        )

    fun markPreviousDayReminderShown(today: LocalDate) {
        preferences.edit().putString(KEY_PREVIOUS_DAY_REMINDER, today.toString()).apply()
    }

    fun setSleep(
        date: LocalDate,
        status: DecisionStatus,
        reason: ViolationReason? = null,
        violationPart: SleepViolationPart? = null,
    ) {
        val current = recordFor(date)
        saveRecord(
            current.copy(
                sleep = status,
                sleepReason = reason.takeIf { status == DecisionStatus.NO },
                sleepViolationPart = violationPart.takeIf { status == DecisionStatus.NO },
            )
        )
    }

    fun setMorning(date: LocalDate, status: DecisionStatus, reason: ViolationReason? = null) {
        val current = recordFor(date)
        saveRecord(
            current.copy(
                morning = status,
                morningReason = reason.takeIf { status == DecisionStatus.NO },
                morningRepetitions = current.morningRepetitions.takeIf { status == DecisionStatus.YES },
            )
        )
    }

    fun setMorningRepetitions(date: LocalDate, repetitions: Int, target: Int): Boolean {
        val current = recordFor(date)
        val safeRepetitions = repetitions.coerceIn(0, 100)
        val completed = safeRepetitions >= target
        saveRecord(
            current.copy(
                morning = if (completed) DecisionStatus.YES else null,
                morningReason = null,
                morningRepetitions = safeRepetitions,
            )
        )
        return completed && current.morning != DecisionStatus.YES
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
        saveRecord(current.copy(sleep = null, sleepReason = null, sleepViolationPart = null))
    }

    fun clearMorning(date: LocalDate) {
        val current = recordFor(date)
        saveRecord(current.copy(morning = null, morningReason = null, morningRepetitions = null))
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

    fun confirmMoneyTransfer(periodStart: LocalDate) {
        if (periodStart.isBefore(SystemLogic.MONEY_START_DATE)) return
        moneyReceivedPeriods = moneyReceivedPeriods + periodStart
        database.setMoneyTransfer(periodStart, true)
    }

    fun revokeMoneyTransfer(periodStart: LocalDate) {
        moneyReceivedPeriods = moneyReceivedPeriods - periodStart
        database.setMoneyTransfer(periodStart, false)
    }

    fun addMoneyExpense(amountRubles: Long, category: MoneyCategory, planned: Boolean, date: LocalDate = LocalDate.now()) {
        if (amountRubles !in 1L..1_000_000L || date.isBefore(SystemLogic.MONEY_START_DATE)) return
        val transaction = MoneyTransaction(
            id = nextId(),
            date = date,
            amountRubles = amountRubles,
            category = category,
            planned = planned,
        )
        moneyTransactions = (moneyTransactions + transaction).sortedByDescending { it.id }
        database.saveMoneyTransaction(transaction)
    }

    fun updateMoneyExpense(transaction: MoneyTransaction) {
        if (transaction.amountRubles !in 1L..1_000_000L) return
        database.saveMoneyTransaction(transaction)
        moneyTransactions = database.loadMoneyTransactions()
    }

    fun deleteMoneyExpense(id: Long) {
        moneyTransactions = moneyTransactions.filterNot { it.id == id }
        database.deleteMoneyTransaction(id)
    }

    fun addMoneyCommitment(title: String, amountRubles: Long, category: MoneyCategory) {
        if (title.isBlank() || amountRubles !in 1L..1_000_000L) return
        database.saveMoneyCommitment(MoneyCommitment(nextId(), title.trim().take(60), amountRubles, category))
        moneyCommitments = database.loadMoneyCommitments()
    }

    fun deleteMoneyCommitment(id: Long) {
        database.deleteMoneyCommitment(id)
        moneyCommitments = database.loadMoneyCommitments()
    }

    fun reload() {
        records = database.loadRecords()
        settings = loadSettings()
        experimentFeedback = database.loadExperimentFeedback()
        assignments = database.loadAssignments()
        hseTransitPlan = loadHseTransitPlan()
        moneyTransactions = database.loadMoneyTransactions()
        moneyReceivedPeriods = database.loadMoneyTransfers()
        moneyCommitments = database.loadMoneyCommitments()
    }

    fun addAssignment(title: String, subject: String, dueDate: LocalDate, priority: AssignmentPriority) {
        if (title.isBlank()) return
        val assignment = StudyAssignment(
            id = nextId(),
            title = title.trim().take(100),
            subject = subject.trim().take(60),
            dueDate = dueDate,
            priority = priority,
        )
        saveAssignment(assignment)
    }

    fun toggleAssignment(id: Long) {
        assignments.firstOrNull { it.id == id }?.let { saveAssignment(it.copy(completed = !it.completed)) }
    }

    fun deleteAssignment(id: Long) {
        assignments = assignments.filterNot { it.id == id }
        database.deleteAssignment(id)
    }

    fun updateAssignment(assignment: StudyAssignment) {
        if (assignment.title.isBlank()) return
        saveAssignment(
            assignment.copy(
                title = assignment.title.trim().take(100),
                subject = assignment.subject.trim().take(60),
            )
        )
    }

    fun updateSettings(transform: (SystemSettings) -> SystemSettings) {
        val previousSettings = settings
        settings = transform(settings)
        if (
            previousSettings.hseHomeAddress != settings.hseHomeAddress ||
            previousSettings.hseUniversityAddress != settings.hseUniversityAddress
        ) {
            hseTransitPlan = null
            preferences.edit().remove(KEY_HSE_TRANSIT_PLAN).apply()
        }
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
            .putBoolean(KEY_MORNING_MUSIC_ENABLED, settings.morningMusicEnabled)
            .putBoolean(KEY_DIET_ENABLED, settings.dietEnabled)
            .putBoolean(KEY_REDUCE_MOTION, settings.reduceMotion)
            .putString(KEY_ADMISSION_START, settings.admissionStart.toString())
            .putString(KEY_LIGHT_START, settings.lightStart.toString())
            .putBoolean(KEY_HSE_MODE, settings.hseModeEnabled)
            .putString(KEY_HSE_FIRST_CLASS, settings.hseFirstClassTime.toString())
            .putInt(KEY_HSE_COMMUTE, settings.hseCommuteMinutes)
            .putString(KEY_HSE_HOME, settings.hseHomeAddress)
            .putString(KEY_HSE_UNIVERSITY, settings.hseUniversityAddress)
            .putLong(KEY_HSE_CALENDAR_ID, settings.hseCalendarId ?: -1L)
            .putString(KEY_HSE_CALENDAR_NAME, settings.hseCalendarName)
            .putLong(KEY_MONEY_TRANSFER_AMOUNT, settings.moneyTransferRubles)
            .putLong(KEY_MONEY_RESERVE_AMOUNT, settings.moneyReservePerTransferRubles)
            .apply()
        SystemWidgetProvider.updateAll(appContext)
    }

    fun saveHseTransitPlan(plan: HseTransitPlan) {
        hseTransitPlan = plan
        val route = plan.route
        val json = JSONObject().apply {
            put("targetDate", plan.targetDate.toString())
            put("targetTime", plan.targetTime.toString())
            put("generatedAt", plan.generatedAtEpochMillis)
            put("homeAddress", plan.homeAddress)
            put("universityAddress", plan.universityAddress)
            put("lines", route.lines)
            put("totalMinutes", route.totalMinutes)
            put("boardingStop", route.boardingStop)
            put("exitStop", route.exitStop)
            put("busArrivalTime", route.busArrivalTime)
            put("walkToStopMeters", route.walkToStopMeters)
            put("walkToUniversityMeters", route.walkToUniversityMeters)
            put("leaveHomeTime", route.leaveHomeTime)
        }
        preferences.edit().putString(KEY_HSE_TRANSIT_PLAN, json.toString()).apply()
    }

    fun setExperimentFeedback(weekStart: LocalDate, feedback: ExperimentFeedback) {
        experimentFeedback = experimentFeedback + (weekStart to feedback)
        database.saveExperimentFeedback(weekStart, feedback)
    }

    private fun saveRecord(record: DailyRecord) {
        val isEmpty = record.sleep == null && record.morning == null && record.light == null && record.diet == null &&
            record.water == null && record.waterQuarterLiters == null
        val updated = records.toMutableMap().apply {
            if (isEmpty) remove(record.date) else put(record.date, record)
        }
        records = updated
        if (isEmpty) {
            database.deleteRecord(record.date)
            SystemWidgetProvider.updateAll(appContext)
            return
        }
        database.saveRecord(record)
        ReminderScheduler.dismissAnswered(appContext, record)
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
                        sleepViolationPart = json.optString("sleepViolationPart")
                            .takeIf { it.isNotBlank() && it != "null" }
                            ?.let { runCatching { SleepViolationPart.valueOf(it) }.getOrNull() },
                        morningRepetitions = if (json.has("morningRepetitions") && !json.isNull("morningRepetitions")) {
                            json.optInt("morningRepetitions").coerceIn(0, 100)
                        } else null,
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

    private fun saveAssignment(assignment: StudyAssignment) {
        assignments = (assignments.filterNot { it.id == assignment.id } + assignment)
            .sortedWith(compareBy<StudyAssignment> { it.completed }.thenBy { it.dueDate }.thenByDescending { it.priority })
        database.saveAssignment(assignment)
    }

    private fun loadAssignments(): List<StudyAssignment> = preferences.all.mapNotNull { (key, value) ->
        if (!key.startsWith(ASSIGNMENT_PREFIX) || value !is String) return@mapNotNull null
        runCatching {
            val json = JSONObject(value)
            StudyAssignment(
                id = key.removePrefix(ASSIGNMENT_PREFIX).toLong(),
                title = json.getString("title"),
                subject = json.optString("subject"),
                dueDate = LocalDate.parse(json.getString("dueDate")),
                priority = runCatching { AssignmentPriority.valueOf(json.optString("priority")) }
                    .getOrDefault(AssignmentPriority.NORMAL),
                completed = json.optBoolean("completed"),
            )
        }.getOrNull()
    }.sortedWith(compareBy<StudyAssignment> { it.completed }.thenBy { it.dueDate }.thenByDescending { it.priority })

    private fun loadMoneyTransactions(): List<MoneyTransaction> = preferences.all.mapNotNull { (key, value) ->
        if (!key.startsWith(MONEY_TRANSACTION_PREFIX) || value !is String) return@mapNotNull null
        runCatching {
            val json = JSONObject(value)
            MoneyTransaction(
                id = key.removePrefix(MONEY_TRANSACTION_PREFIX).toLong(),
                date = LocalDate.parse(json.getString("date")),
                amountRubles = json.getLong("amountRubles").coerceIn(1L, 1_000_000L),
                category = MoneyCategory.fromId(json.optString("category")),
                planned = json.optBoolean("planned", true),
            )
        }.getOrNull()
    }.sortedByDescending { it.id }

    private fun loadMoneyReceivedPeriods(): Set<LocalDate> = preferences.all.mapNotNull { (key, value) ->
        if (!key.startsWith(MONEY_TRANSFER_PREFIX) || value != true) return@mapNotNull null
        runCatching { LocalDate.parse(key.removePrefix(MONEY_TRANSFER_PREFIX)) }.getOrNull()
    }.toSet()

    private fun loadHseTransitPlan(): HseTransitPlan? = preferences
        .getString(KEY_HSE_TRANSIT_PLAN, null)
        ?.let { value ->
            runCatching {
                val json = JSONObject(value)
                HseTransitPlan(
                    route = TransitOption(
                        lines = json.getString("lines"),
                        totalMinutes = json.getInt("totalMinutes"),
                        boardingStop = json.getString("boardingStop"),
                        exitStop = json.getString("exitStop"),
                        busArrivalTime = json.getString("busArrivalTime"),
                        walkToStopMeters = json.getInt("walkToStopMeters"),
                        walkToUniversityMeters = json.getInt("walkToUniversityMeters"),
                        leaveHomeTime = json.optString("leaveHomeTime"),
                    ),
                    targetDate = LocalDate.parse(json.getString("targetDate")),
                    targetTime = json.optString("targetTime")
                        .takeIf { it.isNotBlank() }
                        ?.let { LocalTime.parse(it) }
                        ?: com.personal.thesystem.data.HSE_ROUTE_TIME,
                    homeAddress = json.getString("homeAddress"),
                    universityAddress = json.getString("universityAddress"),
                    generatedAtEpochMillis = json.optLong("generatedAt", 0L),
                )
            }.getOrNull()
        }

    private fun loadSettings(): SystemSettings = SystemSettings(
        digitalCutoff = preferenceTime(KEY_CUTOFF, LocalTime.of(22, 45)),
        bedTime = preferenceTime(KEY_BED, LocalTime.of(23, 30)),
        morningTime = preferenceTime(KEY_MORNING, LocalTime.of(7, 30)),
        dietTime = preferenceTime(KEY_DIET_TIME, LocalTime.of(21, 0)),
        notificationsEnabled = preferences.getBoolean(KEY_NOTIFICATIONS, false),
        warningEnabled = preferences.getBoolean(KEY_WARNING, true),
        cutoffEnabled = preferences.getBoolean(KEY_CUTOFF_ENABLED, true),
        preparationEnabled = preferences.getBoolean(KEY_PREPARATION, true),
        bedEnabled = preferences.getBoolean(KEY_BED_ENABLED, true),
        morningEnabled = preferences.getBoolean(KEY_MORNING_ENABLED, true),
        morningMusicEnabled = preferences.getBoolean(KEY_MORNING_MUSIC_ENABLED, false),
        dietEnabled = preferences.getBoolean(KEY_DIET_ENABLED, true),
        reduceMotion = preferences.getBoolean(KEY_REDUCE_MOTION, false),
        admissionStart = preferenceDate(KEY_ADMISSION_START) ?: LocalDate.now().also {
            preferences.edit().putString(KEY_ADMISSION_START, it.toString()).apply()
        },
        lightStart = preferenceDate(KEY_LIGHT_START) ?: LocalDate.now().also {
            preferences.edit().putString(KEY_LIGHT_START, it.toString()).apply()
        },
        hseModeEnabled = preferences.getBoolean(KEY_HSE_MODE, false),
        hseFirstClassTime = preferenceTime(KEY_HSE_FIRST_CLASS, LocalTime.of(9, 30)),
        hseCommuteMinutes = preferences.getInt(KEY_HSE_COMMUTE, 45).coerceIn(10, 180),
        hseHomeAddress = preferences.getString(KEY_HSE_HOME, "").orEmpty(),
        hseUniversityAddress = preferences.getString(KEY_HSE_UNIVERSITY, "Покровский бульвар, 11с4")
            .orEmpty().ifBlank { "Покровский бульвар, 11с4" },
        hseCalendarId = preferences.getLong(KEY_HSE_CALENDAR_ID, -1L).takeIf { it >= 0L },
        hseCalendarName = preferences.getString(KEY_HSE_CALENDAR_NAME, "").orEmpty(),
        moneyTransferRubles = preferences.getLong(KEY_MONEY_TRANSFER_AMOUNT, 20_000L).coerceIn(1_000L, 1_000_000L),
        moneyReservePerTransferRubles = preferences.getLong(KEY_MONEY_RESERVE_AMOUNT, 2_000L).coerceIn(0L, 1_000_000L),
    )

    private fun preferenceTime(key: String, fallback: LocalTime): LocalTime =
        preferences.getString(key, null)?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: fallback

    private fun preferenceDate(key: String): LocalDate? =
        preferences.getString(key, null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    fun exportJson(): String = JSONObject().apply {
        put("schema", EXPORT_SCHEMA)
        put("exportedAt", java.time.Instant.now().toString())
        put("records", JSONArray().apply {
            records.values.sortedBy { it.date }.forEach { record ->
                put(JSONObject().apply {
                    put("date", record.date.toString())
                    put("sleep", record.sleep?.name)
                    put("morning", record.morning?.name)
                    put("light", record.light?.name)
                    put("diet", record.diet?.name)
                    put("water", record.water?.name)
                    put("waterQuarterLiters", record.waterQuarterLiters)
                    put("sleepReason", record.sleepReason?.id)
                    put("morningReason", record.morningReason?.id)
                    put("dietReason", record.dietReason?.id)
                    put("sleepViolationPart", record.sleepViolationPart?.name)
                    put("morningRepetitions", record.morningRepetitions)
                })
            }
        })
        put("assignments", JSONArray().apply {
            assignments.forEach { assignment ->
                put(JSONObject().apply {
                    put("id", assignment.id)
                    put("title", assignment.title)
                    put("subject", assignment.subject)
                    put("dueDate", assignment.dueDate.toString())
                    put("priority", assignment.priority.name)
                    put("completed", assignment.completed)
                })
            }
        })
        put("moneyTransactions", JSONArray().apply {
            moneyTransactions.forEach { transaction ->
                put(JSONObject().apply {
                    put("id", transaction.id)
                    put("date", transaction.date.toString())
                    put("amountRubles", transaction.amountRubles)
                    put("category", transaction.category.id)
                    put("planned", transaction.planned)
                })
            }
        })
        put("moneyTransfers", JSONArray(moneyReceivedPeriods.sorted().map(LocalDate::toString)))
        put("moneyCommitments", JSONArray().apply {
            moneyCommitments.forEach { commitment ->
                put(JSONObject().apply {
                    put("id", commitment.id)
                    put("title", commitment.title)
                    put("amountRubles", commitment.amountRubles)
                    put("category", commitment.category.id)
                })
            }
        })
        put("experimentFeedback", JSONObject().apply {
            experimentFeedback.forEach { (date, feedback) -> put(date.toString(), feedback.name) }
        })
        put("settings", settingsToJson(settings))
    }.toString(2)

    fun importJson(value: String): Boolean = runCatching {
        val root = JSONObject(value)
        require(root.optInt("schema") in 1..EXPORT_SCHEMA)
        val importedRecords = root.getJSONArray("records").objects().map { json ->
            val date = LocalDate.parse(json.getString("date"))
            DailyRecord(
                date = date,
                sleep = json.enumOrNull("sleep"),
                morning = json.enumOrNull("morning"),
                light = json.enumOrNull("light"),
                diet = json.enumOrNull("diet"),
                water = json.enumOrNull("water"),
                waterQuarterLiters = json.intOrNull("waterQuarterLiters")?.coerceIn(0, SystemLogic.WATER_MAX_QUARTERS),
                sleepReason = ViolationReason.fromId(json.optString("sleepReason")),
                morningReason = ViolationReason.fromId(json.optString("morningReason")),
                dietReason = DietViolationReason.fromId(json.optString("dietReason")),
                sleepViolationPart = json.enumOrNull("sleepViolationPart"),
                morningRepetitions = json.intOrNull("morningRepetitions")?.coerceIn(0, 100),
            )
        }
        val importedAssignments = root.optJSONArray("assignments")?.objects().orEmpty().map { json ->
            StudyAssignment(
                id = json.getLong("id"),
                title = json.getString("title"),
                subject = json.optString("subject"),
                dueDate = LocalDate.parse(json.getString("dueDate")),
                priority = json.enumOrNull<AssignmentPriority>("priority") ?: AssignmentPriority.NORMAL,
                completed = json.optBoolean("completed"),
            )
        }
        val importedTransactions = root.optJSONArray("moneyTransactions")?.objects().orEmpty().map { json ->
            MoneyTransaction(
                id = json.getLong("id"),
                date = LocalDate.parse(json.getString("date")),
                amountRubles = json.getLong("amountRubles"),
                category = MoneyCategory.fromId(json.optString("category")),
                planned = json.optBoolean("planned", true),
            )
        }
        val importedTransfers = root.optJSONArray("moneyTransfers")?.strings().orEmpty().map(LocalDate::parse)
        val importedCommitments = root.optJSONArray("moneyCommitments")?.objects().orEmpty().map { json ->
            MoneyCommitment(
                id = json.getLong("id"),
                title = json.getString("title"),
                amountRubles = json.getLong("amountRubles"),
                category = MoneyCategory.fromId(json.optString("category")),
            )
        }
        database.clearUserData()
        importedRecords.forEach(database::saveRecord)
        importedAssignments.forEach(database::saveAssignment)
        importedTransactions.forEach(database::saveMoneyTransaction)
        importedTransfers.forEach { database.setMoneyTransfer(it, true) }
        importedCommitments.forEach(database::saveMoneyCommitment)
        root.optJSONObject("experimentFeedback")?.let { feedback ->
            feedback.keys().forEach { date ->
                runCatching {
                    database.saveExperimentFeedback(LocalDate.parse(date), ExperimentFeedback.valueOf(feedback.getString(date)))
                }
            }
        }
        root.optJSONObject("settings")?.let { imported -> updateSettings { settingsFromJson(imported, it) } }
        reload()
    }.isSuccess

    fun moneyCsv(): String = buildString {
        appendLine("date;amount_rubles;category;planned")
        moneyTransactions.sortedWith(compareBy<MoneyTransaction> { it.date }.thenBy { it.id }).forEach { transaction ->
            appendLine("${transaction.date};${transaction.amountRubles};${transaction.category.label};${transaction.planned}")
        }
    }

    private fun migrateLegacyData() {
        if (preferences.getBoolean(KEY_DATABASE_MIGRATED, false)) return
        loadRecords().values.forEach(database::saveRecord)
        loadAssignments().forEach(database::saveAssignment)
        loadMoneyTransactions().forEach(database::saveMoneyTransaction)
        loadMoneyReceivedPeriods().forEach { database.setMoneyTransfer(it, true) }
        loadExperimentFeedback().forEach(database::saveExperimentFeedback)
        preferences.edit().putBoolean(KEY_DATABASE_MIGRATED, true).commit()
    }

    private fun nextId(): Long {
        val now = System.currentTimeMillis()
        val previous = preferences.getLong(KEY_LAST_ID, 0L)
        return maxOf(now, previous + 1L).also { preferences.edit().putLong(KEY_LAST_ID, it).apply() }
    }

    private fun settingsToJson(value: SystemSettings): JSONObject = JSONObject().apply {
        put("digitalCutoff", value.digitalCutoff.toString())
        put("bedTime", value.bedTime.toString())
        put("morningTime", value.morningTime.toString())
        put("dietTime", value.dietTime.toString())
        put("notificationsEnabled", value.notificationsEnabled)
        put("warningEnabled", value.warningEnabled)
        put("cutoffEnabled", value.cutoffEnabled)
        put("preparationEnabled", value.preparationEnabled)
        put("bedEnabled", value.bedEnabled)
        put("morningEnabled", value.morningEnabled)
        put("dietEnabled", value.dietEnabled)
        put("morningMusicEnabled", value.morningMusicEnabled)
        put("reduceMotion", value.reduceMotion)
        put("admissionStart", value.admissionStart.toString())
        put("lightStart", value.lightStart.toString())
        put("hseModeEnabled", value.hseModeEnabled)
        put("hseFirstClassTime", value.hseFirstClassTime.toString())
        put("hseCommuteMinutes", value.hseCommuteMinutes)
        put("hseHomeAddress", value.hseHomeAddress)
        put("hseUniversityAddress", value.hseUniversityAddress)
        put("hseCalendarId", value.hseCalendarId)
        put("hseCalendarName", value.hseCalendarName)
        put("moneyTransferRubles", value.moneyTransferRubles)
        put("moneyReservePerTransferRubles", value.moneyReservePerTransferRubles)
    }

    private fun settingsFromJson(json: JSONObject, fallback: SystemSettings): SystemSettings = fallback.copy(
        digitalCutoff = json.safeTime("digitalCutoff", fallback.digitalCutoff),
        bedTime = json.safeTime("bedTime", fallback.bedTime),
        morningTime = json.safeTime("morningTime", fallback.morningTime),
        dietTime = json.safeTime("dietTime", fallback.dietTime),
        notificationsEnabled = json.optBoolean("notificationsEnabled", fallback.notificationsEnabled),
        warningEnabled = json.optBoolean("warningEnabled", fallback.warningEnabled),
        cutoffEnabled = json.optBoolean("cutoffEnabled", fallback.cutoffEnabled),
        preparationEnabled = json.optBoolean("preparationEnabled", fallback.preparationEnabled),
        bedEnabled = json.optBoolean("bedEnabled", fallback.bedEnabled),
        morningEnabled = json.optBoolean("morningEnabled", fallback.morningEnabled),
        dietEnabled = json.optBoolean("dietEnabled", fallback.dietEnabled),
        morningMusicEnabled = json.optBoolean("morningMusicEnabled", fallback.morningMusicEnabled),
        reduceMotion = json.optBoolean("reduceMotion", fallback.reduceMotion),
        admissionStart = json.safeDate("admissionStart", fallback.admissionStart),
        lightStart = json.safeDate("lightStart", fallback.lightStart),
        hseModeEnabled = json.optBoolean("hseModeEnabled", fallback.hseModeEnabled),
        hseFirstClassTime = json.safeTime("hseFirstClassTime", fallback.hseFirstClassTime),
        hseCommuteMinutes = json.optInt("hseCommuteMinutes", fallback.hseCommuteMinutes).coerceIn(10, 180),
        hseHomeAddress = json.optString("hseHomeAddress", fallback.hseHomeAddress),
        hseUniversityAddress = json.optString("hseUniversityAddress", fallback.hseUniversityAddress),
        hseCalendarId = json.longOrNull("hseCalendarId"),
        hseCalendarName = json.optString("hseCalendarName", fallback.hseCalendarName),
        moneyTransferRubles = json.optLong("moneyTransferRubles", fallback.moneyTransferRubles).coerceIn(1_000L, 1_000_000L),
        moneyReservePerTransferRubles = json.optLong("moneyReservePerTransferRubles", fallback.moneyReservePerTransferRubles).coerceIn(0L, 1_000_000L),
    )

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).mapNotNull { optJSONObject(it) }
    private fun JSONArray.strings(): List<String> = (0 until length()).mapNotNull { optString(it).takeIf(String::isNotBlank) }
    private inline fun <reified T : Enum<T>> JSONObject.enumOrNull(key: String): T? =
        optString(key).takeIf { it.isNotBlank() && it != "null" }?.let { value -> enumValues<T>().firstOrNull { it.name == value } }
    private fun JSONObject.intOrNull(key: String): Int? = if (has(key) && !isNull(key)) optInt(key) else null
    private fun JSONObject.longOrNull(key: String): Long? = if (has(key) && !isNull(key)) optLong(key) else null
    private fun JSONObject.safeDate(key: String, fallback: LocalDate): LocalDate =
        runCatching { LocalDate.parse(getString(key)) }.getOrDefault(fallback)
    private fun JSONObject.safeTime(key: String, fallback: LocalTime): LocalTime =
        runCatching { LocalTime.parse(getString(key)) }.getOrDefault(fallback)

    companion object {
        private const val PREFS_NAME = "the_system"
        private const val RECORD_PREFIX = "record_"
        private const val EXPERIMENT_PREFIX = "experiment_feedback_"
        private const val ASSIGNMENT_PREFIX = "assignment_"
        private const val MONEY_TRANSACTION_PREFIX = "money_transaction_"
        private const val MONEY_TRANSFER_PREFIX = "money_transfer_"
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
        private const val KEY_MORNING_MUSIC_ENABLED = "morning_music_enabled"
        private const val KEY_DIET_ENABLED = "diet_enabled"
        private const val KEY_REDUCE_MOTION = "reduce_motion"
        private const val KEY_ADMISSION_START = "admission_start"
        private const val KEY_LIGHT_START = "light_start"
        private const val KEY_HSE_MODE = "hse_mode"
        private const val KEY_HSE_FIRST_CLASS = "hse_first_class"
        private const val KEY_HSE_COMMUTE = "hse_commute"
        private const val KEY_HSE_HOME = "hse_home"
        private const val KEY_HSE_UNIVERSITY = "hse_university"
        private const val KEY_HSE_CALENDAR_ID = "hse_calendar_id"
        private const val KEY_HSE_CALENDAR_NAME = "hse_calendar_name"
        private const val KEY_MONEY_TRANSFER_AMOUNT = "money_transfer_amount"
        private const val KEY_MONEY_RESERVE_AMOUNT = "money_reserve_amount"
        private const val KEY_HSE_TRANSIT_PLAN = "hse_transit_plan"
        private const val KEY_PREVIOUS_DAY_REMINDER = "previous_day_reminder"
        private const val KEY_DATABASE_MIGRATED = "database_migrated_v1"
        private const val KEY_LAST_ID = "last_generated_id"
        private const val EXPORT_SCHEMA = 2
    }
}
