package com.sravanthi.kairo.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ReminderSlotTest {

    private val zone = ZoneId.of("America/Chicago")
    private fun local(text: String): Instant = LocalDateTime.parse(text).atZone(zone).toInstant()

    @Test
    fun `next occurrence is later today when the time has not passed`() {
        val slot = ReminderSlot(1, LocalTime.of(20, 0))
        assertEquals(local("2026-03-10T20:00"), slot.nextOccurrence(local("2026-03-10T09:15"), zone))
    }

    @Test
    fun `next occurrence is tomorrow when the time has passed or is now`() {
        val slot = ReminderSlot(1, LocalTime.of(8, 0))
        assertEquals(local("2026-03-11T08:00"), slot.nextOccurrence(local("2026-03-10T09:15"), zone))
        // Exactly at the reminder time counts as passed, so a fired alarm reschedules for tomorrow.
        assertEquals(local("2026-03-11T08:00"), slot.nextOccurrence(local("2026-03-10T08:00"), zone))
    }

    @Test
    fun `daylight saving change keeps the local wall-clock time`() {
        // US clocks spring forward on 2026-03-08.
        val slot = ReminderSlot(1, LocalTime.of(8, 0))
        val next = slot.nextOccurrence(local("2026-03-07T09:00"), zone)
        assertEquals(local("2026-03-08T08:00"), next)
        assertEquals(LocalTime.of(8, 0), next.atZone(zone).toLocalTime())
    }

    @Test
    fun `matches the instant the Today schedule uses for the same dose`() {
        val med = Medication(3, "Aspirin", "81 mg", null, listOf(LocalTime.of(9, 0)))
        val date = java.time.LocalDate.of(2026, 3, 10)
        val scheduleInstant = buildDaySchedule(listOf(med), emptyList(), date, zone).single().scheduledAt
        val reminderInstant = ReminderSlot(3, LocalTime.of(9, 0)).nextOccurrence(local("2026-03-10T00:00"), zone)
        assertEquals(scheduleInstant, reminderInstant)
    }

    @Test
    fun `keys round-trip and bad keys are rejected`() {
        val slot = ReminderSlot(12, LocalTime.of(8, 30))
        assertEquals("12@08:30", slot.key)
        assertEquals(slot, ReminderSlot.fromKey(slot.key))
        assertNull(ReminderSlot.fromKey("nonsense"))
        assertNull(ReminderSlot.fromKey("x@08:30"))
    }

    @Test
    fun `slots cover active medications only, one per distinct time`() {
        val meds = listOf(
            Medication(1, "A", "1", null, listOf(LocalTime.of(8, 0), LocalTime.of(20, 0))),
            Medication(2, "B", "1", null, listOf(LocalTime.of(9, 0)), isActive = false),
        )
        assertEquals(listOf("1@08:00", "1@20:00"), meds.reminderSlots().map { it.key })
    }
}
