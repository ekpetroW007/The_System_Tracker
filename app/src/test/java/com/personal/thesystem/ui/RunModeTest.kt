package com.personal.thesystem.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunModeTest {
    @Test
    fun distanceAndCaloriesStartImmediately() {
        val firstSecond = calculateRunStats(1L)
        val tenMinutes = calculateRunStats(10L * 60L)

        assertEquals(1L, firstSecond.countedSeconds)
        assertEquals(0.00138, firstSecond.distanceKm, 0.00001)
        assertEquals(600L, tenMinutes.countedSeconds)
        assertEquals(0.8333, tenMinutes.distanceKm, 0.0001)
        assertEquals(58.9, tenMinutes.calories, 0.01)
        assertEquals("10:00", formatRunTime(10L * 60L))
    }

    @Test
    fun twoAccelerationPeaksTriggerRunPrompt() {
        val trigger = ShakeTrigger()

        assertFalse(trigger.register(1.0f, 0L))
        assertFalse(trigger.register(3.0f, 100L))
        assertTrue(trigger.register(3.0f, 500L))
        assertFalse(trigger.register(3.0f, 1_000L))
        assertFalse(trigger.register(3.0f, 1_200L))
        assertFalse(trigger.register(3.0f, 3_000L))
        assertTrue(trigger.register(3.0f, 3_300L))
    }
}
