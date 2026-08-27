package com.personal.thesystem.data

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class HseCalendarEvent(
    val title: String,
    val start: LocalTime,
    val end: LocalTime,
    val location: String,
    val calendarName: String,
)

internal fun selectLikelyHseEvents(events: List<HseCalendarEvent>): List<HseCalendarEvent> {
    val markers = listOf("hse", "вшэ", "вышк")
    val hseEvents = events.filter { event ->
        markers.any { marker ->
            event.calendarName.contains(marker, ignoreCase = true) ||
                event.title.contains(marker, ignoreCase = true) ||
                event.location.contains(marker, ignoreCase = true)
        }
    }
    return (hseEvents.ifEmpty { events })
        .distinctBy { listOf(it.start, it.end, it.title, it.location) }
        .sortedBy(HseCalendarEvent::start)
}

object HseCalendarReader {
    fun eventsFor(context: Context, date: LocalDate): List<HseCalendarEvent> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }

        val zone = ZoneId.systemDefault()
        val begin = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also {
            ContentUris.appendId(it, begin)
            ContentUris.appendId(it, end)
        }.build()
        val projection = arrayOf(
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.CALENDAR_DISPLAY_NAME,
            CalendarContract.Instances.ALL_DAY,
        )

        val events = runCatching {
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC",
            )?.use { cursor ->
                val title = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
                val start = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
                val finish = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
                val location = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION)
                val calendar = cursor.getColumnIndexOrThrow(CalendarContract.Instances.CALENDAR_DISPLAY_NAME)
                val allDay = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
                buildList {
                    while (cursor.moveToNext()) {
                        if (cursor.getInt(allDay) != 0) continue
                        add(
                            HseCalendarEvent(
                                title = cursor.getString(title).orEmpty().ifBlank { "Занятие" },
                                start = Instant.ofEpochMilli(cursor.getLong(start)).atZone(zone).toLocalTime(),
                                end = Instant.ofEpochMilli(cursor.getLong(finish)).atZone(zone).toLocalTime(),
                                location = cursor.getString(location).orEmpty(),
                                calendarName = cursor.getString(calendar).orEmpty(),
                            )
                        )
                    }
                }
            }.orEmpty()
        }.getOrDefault(emptyList())

        return selectLikelyHseEvents(events)
    }
}
