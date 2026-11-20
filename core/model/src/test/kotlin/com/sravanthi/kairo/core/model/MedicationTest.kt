package com.sravanthi.kairo.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalTime

class MedicationTest {

    private fun medication(vararg times: String) = Medication(
        name = "Metformin",
        dosage = "500 mg",
        reminderTimes = times.map(LocalTime::parse),
    )

    @Test
    fun `next reminder is the first time at or after now`() {
        val med = medication("20:00", "08:00", "14:00")
        assertEquals(LocalTime.parse("14:00"), med.nextReminderAfter(LocalTime.parse("09:30")))
        assertEquals(LocalTime.parse("08:00"), med.nextReminderAfter(LocalTime.parse("08:00")))
    }

    @Test
    fun `next reminder wraps to tomorrow's first dose`() {
        val med = medication("08:00", "20:00")
        assertEquals(LocalTime.parse("08:00"), med.nextReminderAfter(LocalTime.parse("21:00")))
    }

    @Test
    fun `no reminder times means no next reminder`() {
        assertNull(medication().nextReminderAfter(LocalTime.NOON))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank name is rejected`() {
        Medication(name = " ", dosage = "1 tablet", reminderTimes = emptyList())
    }

    @Test
    fun `dose delay is measured from the scheduled time`() {
        val scheduled = Instant.parse("2026-01-01T08:00:00Z")
        val dose = DoseEvent(
            medicationId = 1,
            scheduledAt = scheduled,
            takenAt = scheduled.plus(Duration.ofMinutes(25)),
            status = DoseStatus.TAKEN,
        )
        assertEquals(Duration.ofMinutes(25), dose.delay)
        assertNull(dose.copy(takenAt = null).delay)
    }
}
