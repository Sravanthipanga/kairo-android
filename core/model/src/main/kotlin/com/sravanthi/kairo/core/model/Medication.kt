package com.sravanthi.kairo.core.model

import java.time.LocalTime

/**
 * A medication the user takes on a schedule.
 *
 * @property reminderTimes the times of day a dose is due, kept sorted.
 */
data class Medication(
    val id: Long = 0,
    val name: String,
    val dosage: String,
    val instructions: String? = null,
    val reminderTimes: List<LocalTime>,
    val isActive: Boolean = true,
) {
    init {
        require(name.isNotBlank()) { "Medication name must not be blank" }
    }

    /** The next reminder time at or after [now], wrapping to tomorrow's first dose. */
    fun nextReminderAfter(now: LocalTime): LocalTime? =
        reminderTimes.sorted().firstOrNull { !it.isBefore(now) } ?: reminderTimes.minOrNull()
}
