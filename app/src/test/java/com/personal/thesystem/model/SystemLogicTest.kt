package com.personal.thesystem.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.DayOfWeek
import com.personal.thesystem.notifications.ReminderType

class SystemLogicTest {
    private val start = LocalDate.of(2026, 8, 1)

    @Test
    fun everyWeekdayHasTheRequestedMorningArtist() {
        assertEquals("Big Baby Tape", SystemLogic.morningArtistFor(DayOfWeek.MONDAY))
        assertEquals("Bushido Zho", SystemLogic.morningArtistFor(DayOfWeek.TUESDAY))
        assertEquals("Icegergert", SystemLogic.morningArtistFor(DayOfWeek.WEDNESDAY))
        assertEquals("Friendly Thug 52 NGG", SystemLogic.morningArtistFor(DayOfWeek.THURSDAY))
        assertEquals("SQWOZ BAB", SystemLogic.morningArtistFor(DayOfWeek.FRIDAY))
        assertEquals("Aarne", SystemLogic.morningArtistFor(DayOfWeek.SATURDAY))
        assertEquals("kizaru", SystemLogic.morningArtistFor(DayOfWeek.SUNDAY))
    }

    @Test
    fun dietReminderIsSkippedAfterEitherDietAnswer() {
        assertTrue(ReminderType.DIET.shouldNotify(DailyRecord(start)))
        assertFalse(ReminderType.DIET.shouldNotify(DailyRecord(start, diet = DecisionStatus.YES)))
        assertFalse(ReminderType.DIET.shouldNotify(DailyRecord(start, diet = DecisionStatus.NO)))
        assertTrue(ReminderType.MORNING.shouldNotify(DailyRecord(start, diet = DecisionStatus.YES)))
    }

    @Test
    fun everyReminderStopsAfterItsTaskIsAnswered() {
        val record = DailyRecord(
            start,
            morning = DecisionStatus.NO,
            diet = DecisionStatus.YES,
            sleep = DecisionStatus.NO,
        )

        assertFalse(ReminderType.MORNING.shouldNotify(record))
        assertFalse(ReminderType.DIET.shouldNotify(record))
        assertFalse(ReminderType.WARNING.shouldNotify(record))
        assertFalse(ReminderType.CUTOFF.shouldNotify(record))
        assertFalse(ReminderType.PREPARATION.shouldNotify(record))
        assertFalse(ReminderType.BED.shouldNotify(record))
    }

    @Test
    fun repeatedDecisionTapClearsTheSelection() {
        assertEquals(DecisionStatus.YES, SystemLogic.toggledDecision(null, DecisionStatus.YES))
        assertEquals(DecisionStatus.NO, SystemLogic.toggledDecision(DecisionStatus.YES, DecisionStatus.NO))
        assertEquals(null, SystemLogic.toggledDecision(DecisionStatus.YES, DecisionStatus.YES))
        assertEquals(null, SystemLogic.toggledDecision(DecisionStatus.NO, DecisionStatus.NO))
    }

    @Test
    fun previousDayReminderAppearsOnceOnlyForAnIncompleteActiveDay() {
        val today = start.plusDays(1)
        val incomplete = DailyRecord(start, morning = DecisionStatus.YES)
        val complete = incomplete.copy(
            light = DecisionStatus.YES,
            diet = DecisionStatus.NO,
            water = DecisionStatus.YES,
            sleep = DecisionStatus.NO,
        )

        assertTrue(SystemLogic.shouldShowPreviousDayReminder(today, start, incomplete, null))
        assertFalse(SystemLogic.shouldShowPreviousDayReminder(today, start, incomplete, today))
        assertFalse(SystemLogic.shouldShowPreviousDayReminder(today, today, incomplete, null))
        assertFalse(SystemLogic.shouldShowPreviousDayReminder(today, start, complete, null))
    }

    @Test
    fun admissionStaysAtLevelOneWithoutSuccess() {
        val records = listOf(
            DailyRecord(start, morning = DecisionStatus.NO),
            DailyRecord(start.plusDays(1)),
        )

        val admission = SystemLogic.admissionFor(start.plusDays(60), start, records)

        assertEquals(1, admission.level)
        assertEquals(10, admission.target)
        assertFalse(admission.completed)
    }

    @Test
    fun eachPreviousSuccessUnlocksExactlyOneLevel() {
        val records = (0L..12L).map { offset ->
            DailyRecord(start.plusDays(offset), morning = DecisionStatus.YES)
        }

        val admission = SystemLogic.admissionFor(start.plusDays(13), start, records)

        assertEquals(14, admission.level)
        assertEquals(20, admission.target)
        assertFalse(admission.completed)
    }

    @Test
    fun repetitionTargetFollowsTheUnlockedLevel() {
        val records = (0L..12L).map { offset ->
            DailyRecord(start.plusDays(offset), morning = DecisionStatus.YES)
        }

        assertEquals(10, SystemLogic.admissionFor(start.plusDays(1), start, records).target)
        assertEquals(12, SystemLogic.admissionFor(start.plusDays(3), start, records).target)
        assertEquals(14, SystemLogic.admissionFor(start.plusDays(6), start, records).target)
        assertEquals(16, SystemLogic.admissionFor(start.plusDays(9), start, records).target)
        assertEquals(18, SystemLogic.admissionFor(start.plusDays(11), start, records).target)
        assertEquals(20, SystemLogic.admissionFor(start.plusDays(13), start, records).target)
    }

    @Test
    fun todaysSuccessUnlocksNextLevelTomorrow() {
        val records = listOf(DailyRecord(start, morning = DecisionStatus.YES))

        assertEquals(1, SystemLogic.admissionFor(start, start, records).level)
        assertEquals(2, SystemLogic.admissionFor(start.plusDays(1), start, records).level)
    }

    @Test
    fun admissionBecomesPermanentAfterFourteenSuccessfulLevels() {
        val records = (0L..13L).map { offset ->
            DailyRecord(start.plusDays(offset), morning = DecisionStatus.YES)
        }

        val admission = SystemLogic.admissionFor(start.plusDays(14), start, records)

        assertTrue(admission.completed)
        assertEquals(14, admission.level)
        assertEquals(20, admission.target)
    }

    @Test
    fun complianceUsesOnlyAnsweredDecisions() {
        val records = listOf(
            DailyRecord(start, sleep = DecisionStatus.YES),
            DailyRecord(start.plusDays(1), sleep = DecisionStatus.NO),
            DailyRecord(start.plusDays(2)),
        )
        assertEquals(50, SystemLogic.compliance(records) { it.sleep })
        assertEquals(ComplianceStat(50, 2, 30), SystemLogic.complianceStat(records, 30) { it.sleep })
    }

    @Test
    fun lightLeavesTheDailySystemAfterTheThirtyDayPlan() {
        val settings = SystemSettings(lightStart = start)

        assertTrue(DailyTask.LIGHT in SystemLogic.activeTasks(start.plusDays(29), settings))
        assertFalse(DailyTask.LIGHT in SystemLogic.activeTasks(start.plusDays(30), settings))
        assertEquals(4, SystemLogic.activeTasks(start.plusDays(30), settings).size)
    }

    @Test
    fun dietDefaultsToNinePmAndHasIndependentCompliance() {
        val records = listOf(
            DailyRecord(start, diet = DecisionStatus.YES, water = DecisionStatus.NO, sleep = DecisionStatus.NO),
            DailyRecord(start.plusDays(1), diet = DecisionStatus.NO, water = DecisionStatus.YES, sleep = DecisionStatus.YES),
        )

        assertEquals(LocalTime.of(21, 0), SystemSettings().dietTime)
        assertTrue(SystemSettings().dietEnabled)
        assertEquals(50, SystemLogic.compliance(records) { it.diet })
        assertEquals(50, SystemLogic.compliance(records) { it.water })
    }

    @Test
    fun lightPlanRunsForThirtyDaysAndThenKeepsTheFinalStage() {
        val first = SystemLogic.lightPlanFor(start, start)
        val dayTwentyFour = SystemLogic.lightPlanFor(start.plusDays(23), start)
        val dayTwentyFive = SystemLogic.lightPlanFor(start.plusDays(24), start)
        val dayThirty = SystemLogic.lightPlanFor(start.plusDays(29), start)
        val afterPlan = SystemLogic.lightPlanFor(start.plusDays(30), start)

        assertEquals(1, first.day)
        assertEquals(30, dayThirty.day)
        assertFalse(dayThirty.completed)
        assertFalse(dayTwentyFour.task == dayTwentyFive.task)
        assertEquals(dayTwentyFive.task, dayThirty.task)
        assertEquals(dayThirty.task, afterPlan.task)
        assertTrue(afterPlan.completed)
    }

    @Test
    fun everyDailyCombinationHasThreeStableUniqueContextualPhrases() {
        val states = listOf<DecisionStatus?>(null, DecisionStatus.YES, DecisionStatus.NO)
        var combinations = 0
        var phrases = 0

        for (morning in states) {
            for (light in states) {
                for (diet in states) {
                    for (water in states) {
                        for (sleep in states) {
                            val record = DailyRecord(
                                date = start,
                                morning = morning,
                                light = light,
                                diet = diet,
                                water = water,
                                sleep = sleep,
                            )
                            val options = SystemLogic.contextualPhrases(record)
                            val selected = SystemLogic.contextualPhrase(record)

                            assertEquals(3, options.size)
                            assertEquals(3, options.distinct().size)
                            assertTrue(options.all { it.isNotBlank() })
                            assertTrue(selected in options)
                            assertEquals(selected, SystemLogic.contextualPhrase(record))
                            combinations++
                            phrases += options.size
                        }
                    }
                }
            }
        }

        assertEquals(243, combinations)
        assertEquals(729, phrases)
    }

    @Test
    fun weeklyExperimentUsesTheMostFrequentViolationAndCanContinue() {
        val weekStart = LocalDate.of(2026, 8, 17)
        val records = listOf(
            DailyRecord(weekStart.minusDays(3), sleep = DecisionStatus.NO, sleepReason = ViolationReason.PHONE),
            DailyRecord(weekStart.minusDays(4), sleep = DecisionStatus.NO, sleepReason = ViolationReason.PHONE),
            DailyRecord(weekStart.minusDays(5), water = DecisionStatus.NO),
        )

        val first = SystemLogic.weeklyExperiment(weekStart, records)
        val continued = SystemLogic.weeklyExperiment(
            weekStart.plusWeeks(1),
            records,
            mapOf(weekStart to ExperimentFeedback.CONTINUE),
        )

        assertEquals(ExperimentFocus.PHONE, first.focus)
        assertEquals(ExperimentFocus.PHONE, continued.focus)
        assertTrue(continued.continued)
    }

    @Test
    fun weeklyExperimentAppearsOnSeptemberFirst() {
        assertFalse(SystemLogic.experimentAvailableOn(LocalDate.of(2026, 8, 31)))
        assertTrue(SystemLogic.experimentAvailableOn(LocalDate.of(2026, 9, 1)))
    }

    @Test
    fun waterUsesQuarterLiterStepsAndCompletesExactlyAtTwoAndAHalfLiters() {
        var quarters: Int? = null
        repeat(9) { quarters = SystemLogic.adjustWaterQuarters(quarters, 1) }

        assertEquals(9, quarters)
        assertEquals("2,25", SystemLogic.formatWaterLiters(quarters!!))
        assertEquals(null, SystemLogic.waterStatus(quarters, dayFinished = false))
        assertEquals(DecisionStatus.NO, SystemLogic.waterStatus(quarters, dayFinished = true))

        quarters = SystemLogic.adjustWaterQuarters(quarters, 1)

        assertEquals(SystemLogic.WATER_GOAL_QUARTERS, quarters)
        assertEquals("2,5", SystemLogic.formatWaterLiters(quarters))
        assertEquals(DecisionStatus.YES, SystemLogic.waterStatus(quarters, dayFinished = false))
        assertEquals(8, SystemLogic.adjustWaterQuarters(quarters, -2))
    }

    @Test
    fun currentTaskFollowsTimeAndRecoveryStartsAfterTwoFailures() {
        val morningRecord = DailyRecord(start)
        assertEquals(DailyTask.MORNING, SystemLogic.currentTask(morningRecord, LocalTime.of(8, 0), LocalTime.of(22, 45)))

        val recoveryRecord = DailyRecord(
            start,
            morning = DecisionStatus.NO,
            light = DecisionStatus.NO,
        )
        assertEquals(DailyTask.WATER, SystemLogic.recoveryTask(recoveryRecord, LocalTime.of(15, 0), LocalTime.of(22, 45)))
        assertEquals(DailyTask.SLEEP, SystemLogic.currentTask(recoveryRecord, LocalTime.of(23, 0), LocalTime.of(22, 45)))
    }

    @Test
    fun weeklyReportSummarizesCurrentWeek() {
        val monday = LocalDate.of(2026, 9, 7)
        val records = listOf(
            DailyRecord(monday, morning = DecisionStatus.YES, sleep = DecisionStatus.NO, sleepReason = ViolationReason.PHONE),
            DailyRecord(monday.plusDays(1), morning = DecisionStatus.YES, sleep = DecisionStatus.YES),
        )

        val report = SystemLogic.weeklyReport(monday.plusDays(3), records)

        assertEquals(monday, report.weekStart)
        assertEquals(75, report.overall)
        assertEquals("УТРО", report.strongest)
        assertEquals("СОН", report.weakest)
        assertEquals("Телефон · 1", report.topReason)
    }

    @Test
    fun hseModeCalculatesLeaveTimeAndReadableCountdown() {
        val settings = SystemSettings(
            hseFirstClassTime = LocalTime.of(10, 0),
            hseCommuteMinutes = 45,
        )

        val leave = SystemLogic.hseLeaveTime(settings)

        assertEquals(LocalTime.of(9, 15), leave)
        assertEquals("До выхода 1 ч 15 мин", SystemLogic.hseCountdown(LocalTime.of(8, 0), leave))
        assertEquals("Время выходить", SystemLogic.hseCountdown(LocalTime.of(9, 20), leave))
    }

    @Test
    fun moneyUsesTwoFixedPeriodsPerMonth() {
        assertEquals(
            MoneyPeriod(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 15)),
            SystemLogic.moneyPeriodFor(LocalDate.of(2026, 8, 24)),
        )
        assertEquals(
            MoneyPeriod(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 15)),
            SystemLogic.moneyPeriodFor(LocalDate.of(2026, 9, 15)),
        )
        assertEquals(
            MoneyPeriod(LocalDate.of(2026, 9, 16), LocalDate.of(2026, 9, 30)),
            SystemLogic.moneyPeriodFor(LocalDate.of(2026, 9, 16)),
        )
    }

    @Test
    fun moneySnapshotCarriesBalanceAndProtectsEachReserve() {
        val firstPeriod = LocalDate.of(2026, 9, 1)
        val secondPeriod = LocalDate.of(2026, 9, 16)
        val transactions = listOf(
            MoneyTransaction(1, firstPeriod, 15_000, MoneyCategory.GROCERIES, true),
            MoneyTransaction(2, secondPeriod, 1_000, MoneyCategory.CAFE, false),
        )

        val snapshot = SystemLogic.moneySnapshot(
            date = secondPeriod,
            transactions = transactions,
            receivedPeriods = setOf(firstPeriod, secondPeriod),
        )

        assertEquals(5_000, snapshot.carryIn)
        assertEquals(24_000, snapshot.balance)
        assertEquals(4_000, snapshot.reserveTarget)
        assertEquals(20_000, snapshot.safeToSpend)
        assertEquals(1_000, snapshot.unplannedSpent)
        assertEquals(MoneyCategory.CAFE, snapshot.topCategory)
        assertEquals(MoneyStatus.CALM, snapshot.status)
    }

    @Test
    fun mandatoryMoneyCommitmentsReduceTheSafeDailyBudget() {
        val periodStart = LocalDate.of(2026, 9, 1)
        val snapshot = SystemLogic.moneySnapshot(
            date = periodStart,
            transactions = emptyList(),
            receivedPeriods = setOf(periodStart),
            commitments = listOf(MoneyCommitment(1L, "Транспорт", 3_000L, MoneyCategory.TRANSPORT)),
            transferRubles = 22_000L,
            reservePerTransferRubles = 2_000L,
        )

        assertEquals(3_000L, snapshot.mandatoryRemaining)
        assertEquals(17_000L, snapshot.safeToSpend)
        assertEquals(22_000L, snapshot.balance)
    }

    @Test
    fun moneyWaitsForTheRealTransferAndBuildsAReadableReport() {
        val periodStart = LocalDate.of(2026, 9, 1)
        val transactions = listOf(
            MoneyTransaction(1, periodStart, 1_000, MoneyCategory.GROCERIES, true),
            MoneyTransaction(2, periodStart.plusDays(1), 500, MoneyCategory.CAFE, false),
        )

        val waiting = SystemLogic.moneySnapshot(periodStart, transactions, emptySet())
        val active = SystemLogic.moneySnapshot(periodStart.plusDays(2), transactions, setOf(periodStart))
        val watch = SystemLogic.moneySnapshot(
            periodStart,
            listOf(MoneyTransaction(3, periodStart, 2_000, MoneyCategory.ENTERTAINMENT, false)),
            setOf(periodStart),
        )
        val save = SystemLogic.moneySnapshot(
            periodStart,
            listOf(MoneyTransaction(4, periodStart, 19_000, MoneyCategory.SHOPPING, false)),
            setOf(periodStart),
        )
        val report = SystemLogic.moneyReport(active.period, transactions, setOf(periodStart))

        assertEquals(MoneyStatus.WAITING, waiting.status)
        assertEquals(MoneyStatus.WATCH, watch.status)
        assertEquals(MoneyStatus.SAVE, save.status)
        assertEquals(18_500, active.balance)
        assertEquals(16_500, active.safeToSpend)
        assertEquals(500, active.averagePerDay)
        assertEquals(20_000, report.income)
        assertEquals(1_500, report.spent)
        assertEquals(18_500, report.endingBalance)
        assertEquals(500, report.unplannedSpent)
    }

    @Test
    fun correlationsNeedFourteenComparableDaysAndCompareYesWithNo() {
        val records = (0L..14L).map { offset ->
            val sleep = if (offset < 10) DecisionStatus.YES else DecisionStatus.NO
            DailyRecord(
                date = start.plusDays(offset),
                sleep = sleep,
                morning = if (offset == 0L || offset >= 10) DecisionStatus.NO else DecisionStatus.YES,
                light = sleep,
                diet = sleep,
                water = sleep,
            )
        }

        val tooEarly = SystemLogic.correlationAnalysis(records.take(10))
        val analysis = SystemLogic.correlationAnalysis(records)
        val lightToSleep = analysis.insights.first { it.title == "СВЕТ → СОН" }

        assertTrue(tooEarly.insights.isEmpty())
        assertTrue(analysis.insights.isNotEmpty())
        assertEquals(100, lightToSleep.yesRate)
        assertEquals(0, lightToSleep.noRate)
        assertEquals(15, lightToSleep.sampleSize)
    }
}
