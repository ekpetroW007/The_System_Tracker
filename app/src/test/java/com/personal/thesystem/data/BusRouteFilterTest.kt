package com.personal.thesystem.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class BusRouteFilterTest {
    @Test
    fun acceptsOnlyBusVehicleType() {
        assertTrue(isBusLine(listOf("express_bus", "bus")))
        assertFalse(isBusLine(listOf("minibus")))
        assertFalse(isBusLine(listOf("trolleybus")))
        assertFalse(isBusLine(listOf("underground")))
        assertTrue(isDirectBusRoute(busSections = 1, transfers = 0))
        assertFalse(isDirectBusRoute(busSections = 2, transfers = 1))
    }

    @Test
    fun choosesFastestOnlyAmongThreeClosestBoardingStops() {
        val options = listOf(
            option("1", stopMeters = 100, totalMinutes = 40),
            option("2", stopMeters = 200, totalMinutes = 35),
            option("3", stopMeters = 300, totalMinutes = 30),
            option("4", stopMeters = 400, totalMinutes = 10),
        )

        val selected = prioritizeNearestBusOptions(options)

        assertEquals(listOf("3", "2", "1"), selected.map { it.lines })
        assertFalse(selected.any { it.lines == "4" })
    }

    @Test
    fun calculatesWalkingTimeAtFourKilometersPerHour() {
        assertEquals(15, walkingMinutesAtFourKmh(1_000))
        assertEquals(4, walkingMinutesAtFourKmh(250))
        assertEquals(
            65,
            adjustedTotalMinutes(
                routeSeconds = 3_600.0,
                defaultEdgeWalkingSeconds = 600.0,
                rideSeconds = 1_800.0,
                walkingMeters = 1_000,
            ),
        )
    }

    @Test
    fun keepsSavedMorningAndOtherwisePlansTheNextUsefulEightThirty() {
        val today = LocalDate.of(2026, 9, 1)

        assertEquals(today, plannedMorningDate(today, LocalTime.of(7, 0), null))
        assertEquals(today.plusDays(1), plannedMorningDate(today, LocalTime.of(20, 0), null))
        assertEquals(today, plannedMorningDate(today, LocalTime.of(20, 0), today))
        assertEquals(today.plusDays(1), plannedMorningDate(today, LocalTime.of(7, 0), today.plusDays(1)))
        assertEquals(
            Instant.parse("2026-09-01T05:30:00Z").toEpochMilli(),
            routeDepartureEpochMillis(today),
        )
    }

    private fun option(lines: String, stopMeters: Int, totalMinutes: Int) = TransitOption(
        lines = lines,
        totalMinutes = totalMinutes,
        boardingStop = "Остановка $lines",
        exitStop = "ВШЭ $lines",
        busArrivalTime = "08:40",
        walkToStopMeters = stopMeters,
        walkToUniversityMeters = 100,
    )
}
