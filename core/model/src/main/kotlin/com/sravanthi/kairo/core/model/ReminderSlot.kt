package com.sravanthi.kairo.core.model

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * One recurring daily reminder: a medication at a time of day.
 * Each slot is backed by exactly one system alarm, rescheduled for the next day after it fires.
 */
data class ReminderSlot(val medicationId: Long, val time: LocalTime) {

    /** Stable identifier, e.g. "12@08:30". */
    val key: String get() = "$medicationId@$time"

    /**
     * The first moment strictly after [after] when this slot is due, in [zone].
     * Matches how the Today schedule computes dose times, so a reminder and its dose share the same instant.
     */
    fun nextOccurrence(after: Instant, zone: ZoneId): Instant {
        val today = after.atZone(zone).toLocalDate()
        val candidate = today.atTime(time).atZone(zone).toInstant()
        return if (candidate.isAfter(after)) candidate else today.plusDays(1).atTime(time).atZone(zone).toInstant()
    }

    companion object {
        fun fromKey(key: String): ReminderSlot? {
            val (id, time) = key.split("@").takeIf { it.size == 2 } ?: return null
            return runCatching { ReminderSlot(id.toLong(), LocalTime.parse(time)) }.getOrNull()
        }
    }
}

/** Every reminder slot for the active medications. */
fun List<Medication>.reminderSlots(): List<ReminderSlot> =
    filter { it.isActive }.flatMap { med -> med.reminderTimes.distinct().map { ReminderSlot(med.id, it) } }
