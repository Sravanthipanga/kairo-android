package com.sravanthi.kairo.core.model

import java.time.Duration
import java.time.Instant

/** One scheduled dose and what the user did with it. */
data class DoseEvent(
    val id: Long = 0,
    val medicationId: Long,
    val scheduledAt: Instant,
    val takenAt: Instant? = null,
    val status: DoseStatus = DoseStatus.PENDING,
) {
    /**
     * How late (positive) or early (negative) the dose was taken.
     * This is the signal Kairo's adaptive reminders learn from.
     */
    val delay: Duration?
        get() = takenAt?.let { Duration.between(scheduledAt, it) }
}

enum class DoseStatus { PENDING, TAKEN, SKIPPED, SNOOZED }
