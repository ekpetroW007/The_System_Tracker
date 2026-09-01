package com.personal.thesystem.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.DayOfWeek
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

enum class DecisionStatus { YES, NO }

enum class SleepViolationPart(val label: String) {
    CUTOFF("Цифровой отбой"),
    BED("Время в кровати"),
    BOTH("Оба правила"),
}

enum class DailyTask(val title: String, val recoveryAction: String) {
    MORNING("УТРО", "Сделай отжимания сразу после подъёма."),
    LIGHT("СВЕТ", "Открой шторы и выполни текущий этап света."),
    DIET("ПИТАНИЕ", "Закрой день без сладкого и чипсов."),
    WATER("ВОДА", "Добавь следующие 0,25 л воды."),
    SLEEP("СОН", "Убери экраны и начни подготовку ко сну."),
}

enum class ViolationReason(val id: String, val label: String) {
    PHONE("phone", "Телефон"),
    LAPTOP("laptop", "Ноутбук"),
    TIRED("tired", "Устал"),
    SAD("sad", "Грустно");

    companion object {
        fun fromId(id: String?): ViolationReason? = entries.firstOrNull { it.id == id }
    }
}

enum class DietViolationReason(val id: String, val label: String) {
    HUNGER("hunger", "Сильный голод"),
    CRAVING("craving", "Купил по дороге"),
    STRESS("stress", "Стресс / грусть"),
    SOCIAL("social", "Компания / угощение"),
    AVAILABLE("available", "Сладкое было дома"),
    HABIT("habit", "Не было нормальной еды");

    companion object {
        fun fromId(id: String?): DietViolationReason? = entries.firstOrNull { it.id == id }
    }
}

data class DailyRecord(
    val date: LocalDate,
    val sleep: DecisionStatus? = null,
    val morning: DecisionStatus? = null,
    val light: DecisionStatus? = null,
    val diet: DecisionStatus? = null,
    val water: DecisionStatus? = null,
    val waterQuarterLiters: Int? = null,
    val sleepReason: ViolationReason? = null,
    val morningReason: ViolationReason? = null,
    val dietReason: DietViolationReason? = null,
    val sleepViolationPart: SleepViolationPart? = null,
    val morningRepetitions: Int? = null,
)

data class SystemSettings(
    val digitalCutoff: LocalTime = LocalTime.of(22, 45),
    val bedTime: LocalTime = LocalTime.of(23, 30),
    val morningTime: LocalTime = LocalTime.of(7, 30),
    val dietTime: LocalTime = LocalTime.of(21, 0),
    val notificationsEnabled: Boolean = false,
    val warningEnabled: Boolean = true,
    val cutoffEnabled: Boolean = true,
    val preparationEnabled: Boolean = true,
    val bedEnabled: Boolean = true,
    val morningEnabled: Boolean = true,
    val dietEnabled: Boolean = true,
    val reduceMotion: Boolean = false,
    val admissionStart: LocalDate = LocalDate.now(),
    val lightStart: LocalDate = LocalDate.now(),
    val hseModeEnabled: Boolean = false,
    val hseFirstClassTime: LocalTime = LocalTime.of(9, 30),
    val hseCommuteMinutes: Int = 45,
    val hseHomeAddress: String = "",
    val hseUniversityAddress: String = "Покровский бульвар, 11с4",
    val hseCalendarId: Long? = null,
    val hseCalendarName: String = "",
    val moneyTransferRubles: Long = 20_000L,
    val moneyReservePerTransferRubles: Long = 2_000L,
)

enum class AssignmentPriority(val label: String) {
    NORMAL("ОБЫЧНО"),
    IMPORTANT("ВАЖНО"),
}

data class StudyAssignment(
    val id: Long,
    val title: String,
    val subject: String,
    val dueDate: LocalDate,
    val priority: AssignmentPriority = AssignmentPriority.NORMAL,
    val completed: Boolean = false,
)

data class WeeklyReview(
    val date: LocalDate,
    val nextWeekDeadlines: String = "",
    val tests: String = "",
    val weakestSubject: String = "",
    val startEarly: String = "",
)

enum class MoneyCategory(val id: String, val label: String) {
    GROCERIES("groceries", "Продукты"),
    CAFE("cafe", "Кафе и доставка"),
    TRANSPORT("transport", "Транспорт"),
    STUDY("study", "Учёба"),
    HOME("home", "Дом"),
    SUBSCRIPTIONS("subscriptions", "Связь и подписки"),
    ENTERTAINMENT("entertainment", "Развлечения"),
    SHOPPING("shopping", "Покупки"),
    HEALTH("health", "Здоровье"),
    OTHER("other", "Другое");

    companion object {
        fun fromId(id: String?): MoneyCategory = entries.firstOrNull { it.id == id } ?: OTHER
    }
}

data class MoneyTransaction(
    val id: Long,
    val date: LocalDate,
    val amountRubles: Long,
    val category: MoneyCategory,
    val planned: Boolean,
)

data class MoneyCommitment(
    val id: Long,
    val title: String,
    val amountRubles: Long,
    val category: MoneyCategory,
)

data class MoneyPeriod(val start: LocalDate, val end: LocalDate)

enum class MoneyStatus { WAITING, CALM, WATCH, SAVE }

data class MoneySnapshot(
    val period: MoneyPeriod,
    val transferReceived: Boolean,
    val carryIn: Long,
    val spent: Long,
    val unplannedSpent: Long,
    val balance: Long,
    val reserveTarget: Long,
    val mandatoryRemaining: Long,
    val safeToSpend: Long,
    val safePerDay: Long,
    val averagePerDay: Long,
    val projectedSafeEnd: LocalDate?,
    val status: MoneyStatus,
    val topCategory: MoneyCategory?,
)

data class MoneyReport(
    val period: MoneyPeriod,
    val income: Long,
    val spent: Long,
    val endingBalance: Long,
    val unplannedSpent: Long,
    val topCategory: MoneyCategory?,
)

data class AdmissionState(
    val level: Int,
    val totalLevels: Int,
    val target: Int,
    val completed: Boolean,
)

data class LightPlanState(
    val day: Int,
    val totalDays: Int,
    val task: String,
    val completed: Boolean,
)

enum class ExperimentFeedback(val label: String) {
    HELPED("Помогло"),
    DID_NOT_HELP("Не помогло"),
    CONTINUE("Продолжить ещё неделю"),
}

enum class ExperimentFocus(val title: String, val action: String) {
    PHONE(
        "Телефон вне спальни",
        "До воскресенья оставляй телефон на зарядке вне зоны кровати в 22:40.",
    ),
    LAPTOP(
        "Последнее видео заканчивается вовремя",
        "Перед запуском вечернего видео проверь, что оно закончится до 22:45.",
    ),
    TIRED(
        "Подготовка начинается раньше",
        "В 22:15 закрой сложные дела и оставь только спокойную подготовку ко сну.",
    ),
    SAD(
        "Минимальный вечер",
        "Если грустно, не исправляй весь день: убери экран и выполни только подготовку ко сну.",
    ),
    MORNING(
        "Отжимания без переговоров",
        "С вечера освободи место, а утром начни первое повторение сразу после подъёма.",
    ),
    LIGHT(
        "Свет начинается с открытия штор",
        "Открывай шторы сразу после отжиманий, до телефона и других дел.",
    ),
    DIET_HUNGER(
        "Еда до сильного голода",
        "Заранее подготовь нормальную еду, чтобы не выбирать сладкое или чипсы на сильном голоде.",
    ),
    DIET_CRAVING(
        "Тяга без автоматического выбора",
        "При тяге сначала выпей воды и подожди 10 минут перед выбором еды.",
    ),
    DIET_STRESS(
        "Стресс не решает, что есть",
        "При стрессе сделай паузу на 10 минут и только потом решай, нужна ли еда.",
    ),
    DIET_SOCIAL(
        "Выбор до угощения",
        "До встречи заранее реши, что сладкое и чипсы сегодня не входят в план.",
    ),
    DIET_AVAILABLE(
        "Убрать соблазн из поля зрения",
        "Не держи сладкое и чипсы рядом с рабочим местом или кроватью всю неделю.",
    ),
    DIET_HABIT(
        "Заменить привычный перекус",
        "Подготовь один разрешённый перекус и используй его вместо сладкого или чипсов.",
    ),
    WATER(
        "Вода видна весь день",
        "С утра поставь наполненную бутылку на видное место и допей 2,5 литра до вечера.",
    ),
    SLEEP(
        "Цифровой отбой без продолжения",
        "В 22:45 закрой ноутбук и убери телефон, даже если контент ещё не закончился.",
    ),
    BASELINE(
        "Закрепить вечерний рубеж",
        "Всю неделю убирай телефон от кровати в 22:40 и начинай цифровой отбой вовремя.",
    ),
}

data class WeeklyExperiment(
    val weekStart: LocalDate,
    val focus: ExperimentFocus,
    val basis: String,
    val continued: Boolean = false,
)

data class CorrelationInsight(
    val title: String,
    val yesLabel: String,
    val noLabel: String,
    val yesRate: Int,
    val noRate: Int,
    val sampleSize: Int,
)

data class CorrelationAnalysis(
    val insights: List<CorrelationInsight>,
    val comparableDays: Int,
    val requiredDays: Int,
)

data class ComplianceStat(val value: Int?, val answered: Int, val eligible: Int)

object SystemLogic {
    const val ADMISSION_LEVELS = 14
    const val LIGHT_PLAN_DAYS = 30
    const val MIN_CORRELATION_DAYS = 14
    const val WATER_GOAL_QUARTERS = 10
    const val WATER_MAX_QUARTERS = 20
    const val MONEY_TRANSFER_RUBLES = 20_000L
    const val MONEY_RESERVE_PER_TRANSFER_RUBLES = 2_000L
    val MONEY_START_DATE: LocalDate = LocalDate.of(2026, 8, 30)
    private val MONEY_FIRST_PERIOD_END: LocalDate = LocalDate.of(2026, 9, 15)

    fun moneyPeriodFor(date: LocalDate): MoneyPeriod {
        if (!date.isAfter(MONEY_FIRST_PERIOD_END)) return MoneyPeriod(MONEY_START_DATE, MONEY_FIRST_PERIOD_END)
        val start = if (date.dayOfMonth <= 15) date.withDayOfMonth(1) else date.withDayOfMonth(16)
        val end = if (start.dayOfMonth == 1) start.withDayOfMonth(15) else start.withDayOfMonth(start.lengthOfMonth())
        return MoneyPeriod(start, end)
    }

    fun previousMoneyPeriod(date: LocalDate): MoneyPeriod {
        val current = moneyPeriodFor(date)
        return if (current.start == MONEY_START_DATE) {
            MoneyPeriod(MONEY_START_DATE.minusDays(14), MONEY_START_DATE.minusDays(1))
        } else {
            moneyPeriodFor(current.start.minusDays(1))
        }
    }

    fun moneySnapshot(
        date: LocalDate,
        transactions: Collection<MoneyTransaction>,
        receivedPeriods: Set<LocalDate>,
        commitments: Collection<MoneyCommitment> = emptyList(),
        transferRubles: Long = MONEY_TRANSFER_RUBLES,
        reservePerTransferRubles: Long = MONEY_RESERVE_PER_TRANSFER_RUBLES,
    ): MoneySnapshot {
        val period = moneyPeriodFor(date)
        val transfersBefore = receivedPeriods.count { !it.isBefore(MONEY_START_DATE) && it.isBefore(period.start) }
        val transferReceived = period.start in receivedPeriods
        val carryIn = transfersBefore * transferRubles - transactions
            .filter { it.date.isBefore(period.start) }
            .sumOf { it.amountRubles }
        val periodTransactions = transactions.filter { !it.date.isBefore(period.start) && !it.date.isAfter(period.end) }
        val spent = periodTransactions.sumOf { it.amountRubles }
        val balance = carryIn + (if (transferReceived) transferRubles else 0L) - spent
        val reserveTarget = (transfersBefore + if (transferReceived) 1 else 0) * reservePerTransferRubles
        val mandatoryRemaining = commitments.sumOf { it.amountRubles }
        val safeToSpend = balance - reserveTarget - mandatoryRemaining
        val effectiveDate = date.coerceIn(period.start, period.end)
        val daysRemaining = ChronoUnit.DAYS.between(effectiveDate, period.end) + 1L
        val elapsedDays = if (date.isBefore(period.start)) 0L else ChronoUnit.DAYS.between(period.start, effectiveDate) + 1L
        val safePerDay = safeToSpend.coerceAtLeast(0L) / daysRemaining
        val averagePerDay = if (elapsedDays == 0L) 0L else spent / elapsedDays
        val projectedSafeEnd = if (averagePerDay > 0L && safeToSpend > 0L) {
            effectiveDate.plusDays((safeToSpend + averagePerDay - 1L) / averagePerDay)
        } else {
            null
        }
        val status = when {
            !transferReceived -> MoneyStatus.WAITING
            balance < reserveTarget -> MoneyStatus.SAVE
            projectedSafeEnd != null && !projectedSafeEnd.isAfter(period.end) -> MoneyStatus.WATCH
            else -> MoneyStatus.CALM
        }
        val topCategory = periodTransactions
            .groupBy { it.category }
            .maxByOrNull { (_, values) -> values.sumOf { it.amountRubles } }
            ?.key
        return MoneySnapshot(
            period = period,
            transferReceived = transferReceived,
            carryIn = carryIn,
            spent = spent,
            unplannedSpent = periodTransactions.filterNot { it.planned }.sumOf { it.amountRubles },
            balance = balance,
            reserveTarget = reserveTarget,
            mandatoryRemaining = mandatoryRemaining,
            safeToSpend = safeToSpend,
            safePerDay = safePerDay,
            averagePerDay = averagePerDay,
            projectedSafeEnd = projectedSafeEnd,
            status = status,
            topCategory = topCategory,
        )
    }

    fun moneyReport(
        period: MoneyPeriod,
        transactions: Collection<MoneyTransaction>,
        receivedPeriods: Set<LocalDate>,
        transferRubles: Long = MONEY_TRANSFER_RUBLES,
    ): MoneyReport {
        val periodTransactions = transactions.filter { !it.date.isBefore(period.start) && !it.date.isAfter(period.end) }
        val income = if (period.start in receivedPeriods) transferRubles else 0L
        val endingBalance = receivedPeriods.count { !it.isBefore(MONEY_START_DATE) && !it.isAfter(period.end) } *
            transferRubles - transactions.filter { !it.date.isAfter(period.end) }.sumOf { it.amountRubles }
        return MoneyReport(
            period = period,
            income = income,
            spent = periodTransactions.sumOf { it.amountRubles },
            endingBalance = endingBalance,
            unplannedSpent = periodTransactions.filterNot { it.planned }.sumOf { it.amountRubles },
            topCategory = periodTransactions.groupBy { it.category }
                .maxByOrNull { (_, values) -> values.sumOf { it.amountRubles } }
                ?.key,
        )
    }

    fun hseLeaveTime(settings: SystemSettings): LocalTime =
        settings.hseFirstClassTime.minusMinutes(settings.hseCommuteMinutes.toLong())

    fun hseCountdown(now: LocalTime, leaveTime: LocalTime): String {
        val minutes = java.time.Duration.between(now, leaveTime).toMinutes()
        if (minutes <= 0) return "Время выходить"
        val hours = minutes / 60
        val rest = minutes % 60
        return when {
            hours > 0 && rest > 0 -> "До выхода $hours ч $rest мин"
            hours > 0 -> "До выхода $hours ч"
            else -> "До выхода $rest мин"
        }
    }
    val EXPERIMENT_START_DATE: LocalDate = LocalDate.of(2026, 9, 1)

    private data class PhraseTask(val name: String, val status: DecisionStatus?)

    fun toggledDecision(current: DecisionStatus?, tapped: DecisionStatus): DecisionStatus? =
        tapped.takeUnless { it == current }

    fun activeTasks(date: LocalDate, settings: SystemSettings): List<DailyTask> =
        DailyTask.entries.filterNot { it == DailyTask.LIGHT && lightPlanFor(date, settings.lightStart).completed }

    fun missingTasks(
        record: DailyRecord,
        activeTasks: Collection<DailyTask> = DailyTask.entries,
    ): List<DailyTask> = activeTasks.filter { taskStatus(record, it) == null }

    fun shouldShowPreviousDayReminder(
        today: LocalDate,
        admissionStart: LocalDate,
        previousRecord: DailyRecord,
        lastShownOn: LocalDate?,
        lightStart: LocalDate = admissionStart,
    ): Boolean = !today.minusDays(1).isBefore(admissionStart) &&
        lastShownOn != today &&
        missingTasks(
            previousRecord,
            DailyTask.entries.filterNot {
                it == DailyTask.LIGHT && lightPlanFor(previousRecord.date, lightStart).completed
            },
        ).isNotEmpty()

    fun currentTask(
        record: DailyRecord,
        time: LocalTime,
        digitalCutoff: LocalTime,
        activeTasks: Collection<DailyTask> = DailyTask.entries,
    ): DailyTask? {
        val order = when {
            !time.isBefore(digitalCutoff) -> listOf(DailyTask.SLEEP, DailyTask.WATER, DailyTask.DIET, DailyTask.LIGHT, DailyTask.MORNING)
            time.hour >= 21 -> listOf(DailyTask.DIET, DailyTask.WATER, DailyTask.SLEEP, DailyTask.LIGHT, DailyTask.MORNING)
            time.hour < 12 -> listOf(DailyTask.MORNING, DailyTask.LIGHT, DailyTask.WATER, DailyTask.DIET, DailyTask.SLEEP)
            else -> listOf(DailyTask.LIGHT, DailyTask.WATER, DailyTask.DIET, DailyTask.SLEEP, DailyTask.MORNING)
        }
        return order.firstOrNull { it in activeTasks && taskStatus(record, it) == null }
    }

    fun recoveryTask(
        record: DailyRecord,
        time: LocalTime,
        digitalCutoff: LocalTime,
        activeTasks: Collection<DailyTask> = DailyTask.entries,
    ): DailyTask? {
        val failures = activeTasks.count { taskStatus(record, it) == DecisionStatus.NO }
        return currentTask(record, time, digitalCutoff, activeTasks).takeIf { failures >= 2 }
    }

    private fun taskStatus(record: DailyRecord, task: DailyTask): DecisionStatus? = when (task) {
        DailyTask.MORNING -> record.morning
        DailyTask.LIGHT -> record.light
        DailyTask.DIET -> record.diet
        DailyTask.WATER -> record.water
        DailyTask.SLEEP -> record.sleep
    }

    fun statusFor(record: DailyRecord, task: DailyTask): DecisionStatus? = taskStatus(record, task)

    fun waterQuarters(record: DailyRecord): Int? = record.waterQuarterLiters ?: when (record.water) {
        DecisionStatus.YES -> WATER_GOAL_QUARTERS
        DecisionStatus.NO -> 0
        null -> null
    }

    fun adjustWaterQuarters(current: Int?, delta: Int): Int =
        ((current ?: 0) + delta).coerceIn(0, WATER_MAX_QUARTERS)

    fun waterStatus(quarters: Int?, dayFinished: Boolean): DecisionStatus? = when {
        quarters == null -> null
        quarters >= WATER_GOAL_QUARTERS -> DecisionStatus.YES
        dayFinished -> DecisionStatus.NO
        else -> null
    }

    fun formatWaterLiters(quarters: Int): String {
        val fraction = when (quarters % 4) {
            0 -> ""
            1 -> ",25"
            2 -> ",5"
            else -> ",75"
        }
        return "${quarters / 4}$fraction"
    }

    fun admissionFor(
        date: LocalDate,
        start: LocalDate,
        records: Collection<DailyRecord>,
    ): AdmissionState {
        val completedLevels = records
            .asSequence()
            .filter { !it.date.isBefore(start) && it.date.isBefore(date) }
            .filter { it.morning == DecisionStatus.YES }
            .map { it.date }
            .distinct()
            .count()
            .coerceAtMost(ADMISSION_LEVELS)
        val completed = completedLevels == ADMISSION_LEVELS
        val level = (completedLevels + 1).coerceAtMost(ADMISSION_LEVELS)
        val target = when {
            completed -> 20
            level <= 3 -> 10
            level <= 6 -> 12
            level <= 9 -> 14
            level <= 11 -> 16
            level <= 13 -> 18
            else -> 20
        }
        return AdmissionState(level, ADMISSION_LEVELS, target, completed)
    }

    fun compliance(records: Collection<DailyRecord>, selector: (DailyRecord) -> DecisionStatus?): Int? {
        val decisions = records.mapNotNull(selector)
        if (decisions.isEmpty()) return null
        return (decisions.count { it == DecisionStatus.YES } * 100f / decisions.size).toInt()
    }

    fun complianceStat(
        records: Collection<DailyRecord>,
        eligible: Int = records.size,
        selector: (DailyRecord) -> DecisionStatus?,
    ): ComplianceStat {
        val decisions = records.mapNotNull(selector)
        val value = decisions.takeIf { it.isNotEmpty() }
            ?.let { answered -> answered.count { it == DecisionStatus.YES } * 100 / answered.size }
        return ComplianceStat(value, decisions.size, eligible.coerceAtLeast(decisions.size))
    }

    fun weeklyExperiment(
        date: LocalDate,
        records: Collection<DailyRecord>,
        feedback: Map<LocalDate, ExperimentFeedback> = emptyMap(),
    ): WeeklyExperiment {
        val weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        var sourceWeek = weekStart
        var continuationDepth = 0
        while (continuationDepth < 52 && feedback[sourceWeek.minusWeeks(1)] == ExperimentFeedback.CONTINUE) {
            sourceWeek = sourceWeek.minusWeeks(1)
            continuationDepth++
        }
        val source = experimentForWeek(sourceWeek, records)
        return if (sourceWeek == weekStart) source else source.copy(
            weekStart = weekStart,
            basis = "Продолжение выбранного эксперимента.",
            continued = true,
        )
    }

    fun experimentAvailableOn(date: LocalDate): Boolean = !date.isBefore(EXPERIMENT_START_DATE)

    private fun experimentForWeek(weekStart: LocalDate, records: Collection<DailyRecord>): WeeklyExperiment {
        val windowStart = weekStart.minusDays(30)
        val window = records.filter { !it.date.isBefore(windowStart) && it.date.isBefore(weekStart) }
        val failures = listOf(
            "sleep" to window.count { it.sleep == DecisionStatus.NO },
            "morning" to window.count { it.morning == DecisionStatus.NO },
            "diet" to window.count { it.diet == DecisionStatus.NO },
            "water" to window.count { it.water == DecisionStatus.NO },
            "light" to window.count { it.light == DecisionStatus.NO },
        )
        val (task, count) = failures.maxByOrNull { it.second } ?: ("sleep" to 0)
        if (count == 0) {
            val basis = if (window.isEmpty()) {
                "Пока мало данных. Начинаем с главной точки вечернего режима."
            } else {
                "За предыдущие 30 дней нарушений не зафиксировано. Закрепляем результат."
            }
            return WeeklyExperiment(weekStart, ExperimentFocus.BASELINE, basis)
        }

        val focus = when (task) {
            "sleep" -> window
                .filter { it.sleep == DecisionStatus.NO }
                .mapNotNull { it.sleepReason }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key
                ?.let {
                    when (it) {
                        ViolationReason.PHONE -> ExperimentFocus.PHONE
                        ViolationReason.LAPTOP -> ExperimentFocus.LAPTOP
                        ViolationReason.TIRED -> ExperimentFocus.TIRED
                        ViolationReason.SAD -> ExperimentFocus.SAD
                    }
                } ?: ExperimentFocus.SLEEP
            "morning" -> ExperimentFocus.MORNING
            "light" -> ExperimentFocus.LIGHT
            "water" -> ExperimentFocus.WATER
            else -> window
                .filter { it.diet == DecisionStatus.NO }
                .mapNotNull { it.dietReason }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key
                ?.let {
                    when (it) {
                        DietViolationReason.HUNGER -> ExperimentFocus.DIET_HUNGER
                        DietViolationReason.CRAVING -> ExperimentFocus.DIET_CRAVING
                        DietViolationReason.STRESS -> ExperimentFocus.DIET_STRESS
                        DietViolationReason.SOCIAL -> ExperimentFocus.DIET_SOCIAL
                        DietViolationReason.AVAILABLE -> ExperimentFocus.DIET_AVAILABLE
                        DietViolationReason.HABIT -> ExperimentFocus.DIET_HABIT
                    }
                } ?: ExperimentFocus.DIET_AVAILABLE
        }
        val taskLabel = when (task) {
            "sleep" -> "Сон"
            "morning" -> "Утро"
            "diet" -> "Питание"
            "water" -> "Вода"
            else -> "Свет"
        }
        return WeeklyExperiment(
            weekStart = weekStart,
            focus = focus,
            basis = "$taskLabel: $count нарушений за предыдущие 30 дней.",
        )
    }

    fun correlationAnalysis(records: Collection<DailyRecord>): CorrelationAnalysis {
        val byDate = records.associateBy { it.date }
        val sleepToMorning = records.mapNotNull { record ->
            val sleep = byDate[record.date.minusDays(1)]?.sleep
            if (sleep != null && record.morning != null) sleep to record.morning else null
        }
        val lightToSleep = records.mapNotNull { record ->
            if (record.light != null && record.sleep != null) record.light to record.sleep else null
        }
        val waterToDiet = records.mapNotNull { record ->
            if (record.water != null && record.diet != null) record.water to record.diet else null
        }
        val sleepToDiet = records.mapNotNull { record ->
            val sleep = byDate[record.date.minusDays(1)]?.sleep
            if (sleep != null && record.diet != null) sleep to record.diet else null
        }
        val insights = listOfNotNull(
            buildCorrelation("СОН → УТРО", "После соблюдённого сна утро выполнено", "После нарушения сна утро выполнено", sleepToMorning),
            buildCorrelation("СВЕТ → СОН", "При выполненном плане света сон соблюдён", "При нарушении плана света сон соблюдён", lightToSleep),
            buildCorrelation("ВОДА → ПИТАНИЕ", "При выполненной воде питание соблюдено", "При нарушении воды питание соблюдено", waterToDiet),
            buildCorrelation("СОН → ПИТАНИЕ", "После соблюдённого сна питание выполнено", "После нарушения сна питание выполнено", sleepToDiet),
        )
        return CorrelationAnalysis(
            insights = insights,
            comparableDays = maxOf(sleepToMorning.size, lightToSleep.size, waterToDiet.size, sleepToDiet.size),
            requiredDays = MIN_CORRELATION_DAYS,
        )
    }

    private fun buildCorrelation(
        title: String,
        yesLabel: String,
        noLabel: String,
        pairs: List<Pair<DecisionStatus, DecisionStatus>>,
    ): CorrelationInsight? {
        val factorYes = pairs.filter { it.first == DecisionStatus.YES }
        val factorNo = pairs.filter { it.first == DecisionStatus.NO }
        if (pairs.size < MIN_CORRELATION_DAYS || factorYes.size < 2 || factorNo.size < 2) return null
        return CorrelationInsight(
            title = title,
            yesLabel = yesLabel,
            noLabel = noLabel,
            yesRate = factorYes.count { it.second == DecisionStatus.YES } * 100 / factorYes.size,
            noRate = factorNo.count { it.second == DecisionStatus.YES } * 100 / factorNo.size,
            sampleSize = pairs.size,
        )
    }

    fun contextualPhrases(record: DailyRecord): List<String> {
        val tasks = listOf(
            PhraseTask("отжимания", record.morning),
            PhraseTask("свет", record.light),
            PhraseTask("питание", record.diet),
            PhraseTask("вода", record.water),
            PhraseTask("сон", record.sleep),
        )
        val completed = tasks.filter { it.status == DecisionStatus.YES }.map { it.name }
        val missed = tasks.filter { it.status == DecisionStatus.NO }.map { it.name }
        val pending = tasks.filter { it.status == null }.map { it.name }
        val completedText = naturalList(completed)
        val missedText = naturalList(missed)
        val pendingText = naturalList(pending)

        return when {
            pending.size == tasks.size -> listOf(
                "Пять задач ждут честной отметки. Начни с первой.",
                "День пока не отмечен. Выбери «ДА» или «НЕТ» для каждой задачи.",
                "Ни одной отметки ещё нет. Отметь то, что уже можно оценить.",
            )

            completed.size == tasks.size -> listOf(
                "Все пять задач выполнены. Дело сделано. Супер!",
                "Отжимания, свет, питание, вода и сон — всё закрыто.",
                "Пять честных «ДА». Сегодня система выполнена полностью.",
            )

            missed.size == tasks.size -> listOf(
                "Все пять правил сегодня нарушены. Факты зафиксированы, завтра новый день.",
                "Пять «НЕТ» — не приговор, а точная карта этого дня.",
                "Сегодня задачи не выполнены. Следующий выбор начинается завтра.",
            )

            pending.isEmpty() -> listOf(
                "Выполнено: $completedText. Нарушено: $missedText.",
                "День отмечен полностью. «ДА»: $completedText; «НЕТ»: $missedText.",
                "Все задачи отмечены: выполнено ${completed.size} из 5. Картина дня готова.",
            )

            missed.isEmpty() -> listOf(
                "Уже выполнено: $completedText. Осталось отметить: $pendingText.",
                "Нарушений пока нет. Следующая задача — ${pending.first()}.",
                "Выполнено ${completed.size} из 5. Ещё не отмечены: $pendingText.",
            )

            completed.isEmpty() -> listOf(
                "Нарушено: $missedText. Без отметки: $pendingText.",
                "Выполненных задач пока нет, но ещё можно закрыть: $pendingText.",
                "Срывы уже отмечены. Следующая задача — ${pending.first()}.",
            )

            else -> listOf(
                "Выполнено: $completedText. Нарушено: $missedText. Осталось: $pendingText.",
                "Следующая задача — ${pending.first()}. Уже выполнено ${completed.size} из 5.",
                "Нарушения не отменяют оставшиеся задачи. Ещё ждут: $pendingText.",
            )
        }
    }

    fun contextualPhrase(record: DailyRecord): String {
        val stateCode = listOf(record.morning, record.light, record.diet, record.water, record.sleep)
            .fold(0) { code, status ->
                code * 3 + when (status) {
                    null -> 0
                    DecisionStatus.YES -> 1
                    DecisionStatus.NO -> 2
                }
            }
        val index = Math.floorMod(record.date.toEpochDay() + stateCode * 17L, 3L).toInt()
        return contextualPhrases(record)[index]
    }

    private fun naturalList(items: List<String>): String = when (items.size) {
        0 -> "ничего"
        1 -> items.first()
        2 -> items.joinToString(" и ")
        else -> "${items.dropLast(1).joinToString(", ")} и ${items.last()}"
    }

    fun lightPlanFor(date: LocalDate, start: LocalDate): LightPlanState {
        val rawDay = java.time.temporal.ChronoUnit.DAYS.between(start, date).toInt() + 1
        val day = rawDay.coerceIn(1, LIGHT_PLAN_DAYS)
        val task = when (day) {
            in 1..4 -> "Открой шторы на 25% на 15 минут. Если хочется закрыть — подожди 3 минуты."
            in 5..8 -> "Открой шторы на 40–50% на 20–30 минут. Добавь 10 минут дневного света."
            in 9..12 -> "Оставь шторы наполовину на 40–60 минут. Проведи 10–15 минут у окна или на улице."
            in 13..16 -> "Открой шторы на 70% на 1–2 часа. Желание уйти в темноту отложи на 5–10 минут."
            in 17..20 -> "Проведи первую половину дня при естественном свете. Добавь прогулку на 20–30 минут."
            in 21..24 -> "Проведи 30–60 минут за приятным делом при дневном освещении."
            else -> "Проведи обычный день без автоматического затемнения. Перед закрытием штор подожди 10–15 минут."
        }
        return LightPlanState(day, LIGHT_PLAN_DAYS, task, rawDay > LIGHT_PLAN_DAYS)
    }

    fun formatTime(time: LocalTime): String = "%02d:%02d".format(time.hour, time.minute)
}
