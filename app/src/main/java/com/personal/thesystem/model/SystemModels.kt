package com.personal.thesystem.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

enum class DecisionStatus { YES, NO }

enum class ViolationReason(val id: String, val label: String) {
    PHONE("phone", "Телефон"),
    LAPTOP("laptop", "Ноутбук"),
    TIRED("tired", "Устал"),
    SAD("sad", "Грустно"),
    OTHER("other", "Другое");

    companion object {
        fun fromId(id: String?): ViolationReason? = entries.firstOrNull { it.id == id }
    }
}

data class DailyRecord(
    val date: LocalDate,
    val sleep: DecisionStatus? = null,
    val morning: DecisionStatus? = null,
    val sleepReason: ViolationReason? = null,
    val morningReason: ViolationReason? = null,
)

data class SystemSettings(
    val digitalCutoff: LocalTime = LocalTime.of(22, 45),
    val bedTime: LocalTime = LocalTime.of(23, 30),
    val morningTime: LocalTime = LocalTime.of(7, 30),
    val notificationsEnabled: Boolean = false,
    val warningEnabled: Boolean = true,
    val cutoffEnabled: Boolean = true,
    val preparationEnabled: Boolean = true,
    val bedEnabled: Boolean = true,
    val morningEnabled: Boolean = true,
    val admissionStart: LocalDate = LocalDate.now(),
)

data class AdmissionState(
    val day: Int,
    val totalDays: Int,
    val target: Int,
    val completed: Boolean,
)

object SystemLogic {
    const val ADMISSION_DAYS = 14

    fun admissionFor(date: LocalDate, start: LocalDate): AdmissionState {
        val rawDay = ChronoUnit.DAYS.between(start, date).toInt() + 1
        val day = rawDay.coerceAtLeast(1)
        val completed = day > ADMISSION_DAYS
        val target = when {
            completed -> 20
            day <= 3 -> 10
            day <= 6 -> 12
            day <= 9 -> 14
            day <= 11 -> 16
            day <= 13 -> 18
            else -> 20
        }
        return AdmissionState(day.coerceAtMost(ADMISSION_DAYS), ADMISSION_DAYS, target, completed)
    }

    fun compliance(records: Collection<DailyRecord>, selector: (DailyRecord) -> DecisionStatus?): Int? {
        val decisions = records.mapNotNull(selector)
        if (decisions.isEmpty()) return null
        return (decisions.count { it == DecisionStatus.YES } * 100f / decisions.size).toInt()
    }

    fun formatTime(time: LocalTime): String = "%02d:%02d".format(time.hour, time.minute)
}
