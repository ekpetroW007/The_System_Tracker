package com.personal.thesystem.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SystemLogicTest {
    private val start = LocalDate.of(2026, 8, 1)

    @Test
    fun admissionProgressesToTwenty() {
        assertEquals(10, SystemLogic.admissionFor(start, start).target)
        assertEquals(12, SystemLogic.admissionFor(start.plusDays(3), start).target)
        assertEquals(14, SystemLogic.admissionFor(start.plusDays(6), start).target)
        assertEquals(16, SystemLogic.admissionFor(start.plusDays(9), start).target)
        assertEquals(18, SystemLogic.admissionFor(start.plusDays(11), start).target)
        assertEquals(20, SystemLogic.admissionFor(start.plusDays(13), start).target)
    }

    @Test
    fun admissionBecomesPermanentModeAfterDayFourteen() {
        assertFalse(SystemLogic.admissionFor(start.plusDays(13), start).completed)
        assertTrue(SystemLogic.admissionFor(start.plusDays(14), start).completed)
        assertEquals(20, SystemLogic.admissionFor(start.plusDays(60), start).target)
    }

    @Test
    fun complianceUsesOnlyAnsweredDecisions() {
        val records = listOf(
            DailyRecord(start, sleep = DecisionStatus.YES),
            DailyRecord(start.plusDays(1), sleep = DecisionStatus.NO),
            DailyRecord(start.plusDays(2)),
        )
        assertEquals(50, SystemLogic.compliance(records) { it.sleep })
    }
}
