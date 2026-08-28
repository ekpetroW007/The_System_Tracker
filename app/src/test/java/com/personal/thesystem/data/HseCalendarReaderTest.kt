package com.personal.thesystem.data

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class HseCalendarReaderTest {
    @Test
    fun prefersHseCalendarAndSortsLessons() {
        val personal = HseCalendarEvent("Врач", LocalTime.of(11, 0), LocalTime.NOON, "", "Личный")
        val late = HseCalendarEvent("Программирование", LocalTime.of(12, 10), LocalTime.of(13, 30), "Покровка", "HSE App X")
        val early = HseCalendarEvent("Математика", LocalTime.of(9, 30), LocalTime.of(10, 50), "Покровка", "HSE App X")

        assertEquals(listOf(early, late), selectLikelyHseEvents(listOf(personal, late, early)))
    }

    @Test
    fun doesNotLeakPersonalEventsWhenCalendarHasNoHseLabel() {
        val event = HseCalendarEvent("Математика", LocalTime.of(9, 30), LocalTime.of(10, 50), "", "Календарь")

        assertEquals(emptyList<HseCalendarEvent>(), selectLikelyHseEvents(listOf(event)))
    }
}
