package com.personal.thesystem.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

enum class DecisionStatus { YES, NO }

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
    CRAVING("craving", "Сильная тяга"),
    STRESS("stress", "Стресс / грусть"),
    SOCIAL("social", "Компания / угощение"),
    AVAILABLE("available", "Было под рукой"),
    HABIT("habit", "По привычке");

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
    val studyTask: String = "",
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
    val admissionStart: LocalDate = LocalDate.now(),
    val lightStart: LocalDate = LocalDate.now(),
    val hseModeEnabled: Boolean = false,
    val hseFirstClassTime: LocalTime = LocalTime.of(9, 30),
    val hseCommuteMinutes: Int = 45,
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
        "Тяга без автоматического решения",
        "При тяге сначала выпей воды и подожди 10 минут перед выбором еды.",
    ),
    DIET_STRESS(
        "Стресс не решает, что есть",
        "При стрессе сделай паузу на 10 минут и только потом решай, нужна ли еда.",
    ),
    DIET_SOCIAL(
        "Решение до угощения",
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

data class WeeklyMetric(val label: String, val value: Int?)

data class WeeklyReport(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val overall: Int?,
    val metrics: List<WeeklyMetric>,
    val strongest: String?,
    val weakest: String?,
    val topReason: String?,
    val insight: String?,
    val experiment: WeeklyExperiment,
    val experimentFeedback: ExperimentFeedback?,
)

object SystemLogic {
    const val ADMISSION_LEVELS = 14
    const val LIGHT_PLAN_DAYS = 30
    const val MIN_CORRELATION_DAYS = 14
    const val WATER_GOAL_QUARTERS = 10
    const val WATER_MAX_QUARTERS = 20

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

    fun currentTask(record: DailyRecord, time: LocalTime, digitalCutoff: LocalTime): DailyTask? {
        val order = when {
            !time.isBefore(digitalCutoff) -> listOf(DailyTask.SLEEP, DailyTask.WATER, DailyTask.DIET, DailyTask.LIGHT, DailyTask.MORNING)
            time.hour >= 21 -> listOf(DailyTask.DIET, DailyTask.WATER, DailyTask.SLEEP, DailyTask.LIGHT, DailyTask.MORNING)
            time.hour < 12 -> listOf(DailyTask.MORNING, DailyTask.LIGHT, DailyTask.WATER, DailyTask.DIET, DailyTask.SLEEP)
            else -> listOf(DailyTask.LIGHT, DailyTask.WATER, DailyTask.DIET, DailyTask.SLEEP, DailyTask.MORNING)
        }
        return order.firstOrNull { taskStatus(record, it) == null }
    }

    fun recoveryTask(record: DailyRecord, time: LocalTime, digitalCutoff: LocalTime): DailyTask? {
        val failures = DailyTask.entries.count { taskStatus(record, it) == DecisionStatus.NO }
        return currentTask(record, time, digitalCutoff).takeIf { failures >= 2 }
    }

    private fun taskStatus(record: DailyRecord, task: DailyTask): DecisionStatus? = when (task) {
        DailyTask.MORNING -> record.morning
        DailyTask.LIGHT -> record.light
        DailyTask.DIET -> record.diet
        DailyTask.WATER -> record.water
        DailyTask.SLEEP -> record.sleep
    }

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

    fun weeklyReport(
        date: LocalDate,
        records: Collection<DailyRecord>,
        feedback: Map<LocalDate, ExperimentFeedback> = emptyMap(),
    ): WeeklyReport {
        val weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(6)
        val weekRecords = records.filter { !it.date.isBefore(weekStart) && !it.date.isAfter(weekEnd) }
        val metrics = listOf(
            WeeklyMetric("УТРО", compliance(weekRecords) { it.morning }),
            WeeklyMetric("СВЕТ", compliance(weekRecords) { it.light }),
            WeeklyMetric("ПИТАНИЕ", compliance(weekRecords) { it.diet }),
            WeeklyMetric("ВОДА", compliance(weekRecords) { it.water }),
            WeeklyMetric("СОН", compliance(weekRecords) { it.sleep }),
        )
        val allDecisions = weekRecords.flatMap { listOfNotNull(it.morning, it.light, it.diet, it.water, it.sleep) }
        val measured = metrics.filter { it.value != null }
        val reasons = buildList {
            weekRecords.filter { it.sleep == DecisionStatus.NO }.mapNotNullTo(this) { it.sleepReason?.label }
            weekRecords.filter { it.diet == DecisionStatus.NO }.mapNotNullTo(this) { it.dietReason?.label }
        }
        val topReason = reasons.groupingBy { it }.eachCount().maxByOrNull { it.value }?.let { (label, count) -> "$label · $count" }
        val strongestInsight = correlationAnalysis(records).insights.maxByOrNull { kotlin.math.abs(it.yesRate - it.noRate) }
        val experiment = weeklyExperiment(date, records, feedback)
        return WeeklyReport(
            weekStart = weekStart,
            weekEnd = weekEnd,
            overall = allDecisions.takeIf { it.isNotEmpty() }?.let { decisions -> decisions.count { it == DecisionStatus.YES } * 100 / decisions.size },
            metrics = metrics,
            strongest = measured.maxByOrNull { it.value ?: -1 }?.label,
            weakest = measured.minByOrNull { it.value ?: 101 }?.label,
            topReason = topReason,
            insight = strongestInsight?.let { "${it.title}: ${it.yesRate}% против ${it.noRate}%" },
            experiment = experiment,
            experimentFeedback = feedback[weekStart],
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
                "Ни одного решения ещё нет. Отметь то, что уже можно оценить.",
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
                "Все решения приняты: выполнено ${completed.size} из 5. Картина дня готова.",
            )

            missed.isEmpty() -> listOf(
                "Уже выполнено: $completedText. Осталось отметить: $pendingText.",
                "Нарушений пока нет. Следующая задача — ${pending.first()}.",
                "Выполнено ${completed.size} из 5. Ещё ждут решения: $pendingText.",
            )

            completed.isEmpty() -> listOf(
                "Нарушено: $missedText. Без отметки: $pendingText.",
                "Выполненных задач пока нет, но ещё можно закрыть: $pendingText.",
                "Срывы уже отмечены. Следующая задача — ${pending.first()}.",
            )

            else -> listOf(
                "Выполнено: $completedText. Нарушено: $missedText. Осталось: $pendingText.",
                "Следующая задача — ${pending.first()}. Уже выполнено ${completed.size} из 5.",
                "Нарушения не отменяют оставшиеся решения. Ещё ждут: $pendingText.",
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
