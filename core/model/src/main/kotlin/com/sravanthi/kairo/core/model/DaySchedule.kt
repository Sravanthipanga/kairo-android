package com.sravanthi.kairo.core.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** A dose due at a specific moment, combined with what (if anything) the user recorded for it. */
data class ScheduledDose(
    val medication: Medication,
    val scheduledAt: Instant,
    val event: DoseEvent?,
) {
    val status: DoseStatus get() = event?.status ?: DoseStatus.PENDING
}

/**
 * Builds the list of doses due on [date] in [zone]: one entry per active medication per reminder time,
 * matched with any [events] already recorded for that exact slot, ordered by time then name.
 */
fun buildDaySchedule(
    medications: List<Medication>,
    events: List<DoseEvent>,
    date: LocalDate,
    zone: ZoneId,
): List<ScheduledDose> {
    val eventsBySlot = events.associateBy { it.medicationId to it.scheduledAt }
    return medications
        .filter { it.isActive }
        .flatMap { medication ->
            medication.reminderTimes.distinct().map { time ->
                val scheduledAt = date.atTime(time).atZone(zone).toInstant()
                ScheduledDose(medication, scheduledAt, eventsBySlot[medication.id to scheduledAt])
            }
        }
        .sortedWith(compareBy({ it.scheduledAt }, { it.medication.name.lowercase() }))
}

/** Progress for a day: how many doses were taken out of all doses due. */
data class DayProgress(val taken: Int, val total: Int) {
    val fraction: Float get() = if (total == 0) 0f else taken.toFloat() / total
}

fun List<ScheduledDose>.progress() = DayProgress(
    taken = count { it.status == DoseStatus.TAKEN },
    total = size,
)
