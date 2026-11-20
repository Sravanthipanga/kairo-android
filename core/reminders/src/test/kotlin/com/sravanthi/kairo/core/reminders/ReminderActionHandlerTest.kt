package com.sravanthi.kairo.core.reminders

import com.sravanthi.kairo.core.model.DoseStatus
import com.sravanthi.kairo.core.model.Medication
import com.sravanthi.kairo.core.model.ReminderSlot
import com.sravanthi.kairo.core.testing.FakeDoseRepository
import com.sravanthi.kairo.core.testing.FakeMedicationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset

class ReminderActionHandlerTest {

    private val now = Instant.parse("2026-03-10T08:02:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val medications = FakeMedicationRepository()
    private val doses = FakeDoseRepository()
    private val alarms = FakeAlarmScheduler()
    private val notifier = FakeNotifier()
    private val handler = ReminderActionHandler(medications, doses, alarms, notifier, clock)

    private val slot = ReminderSlot(1, LocalTime.of(8, 0))
    private val dueAt = Instant.parse("2026-03-10T08:00:00Z")
    private val metformin = Medication(1, "Metformin", "500 mg", null, listOf(LocalTime.of(8, 0)))

    @Test
    fun `a slot alarm shows the reminder and re-arms for tomorrow`() = runTest {
        medications.setMedications(listOf(metformin))
        handler.onSlotAlarm(slot, dueAt)

        assertEquals(listOf(1L to dueAt), notifier.shown)
        assertEquals(Instant.parse("2026-03-11T08:00:00Z"), alarms.slots[slot])
    }

    @Test
    fun `no reminder when the dose was already taken in the app, but tomorrow is still armed`() = runTest {
        medications.setMedications(listOf(metformin))
        doses.recordDose(1, dueAt, DoseStatus.TAKEN, at = dueAt.minusSeconds(300))

        handler.onSlotAlarm(slot, dueAt)

        assertTrue(notifier.shown.isEmpty())
        assertEquals(Instant.parse("2026-03-11T08:00:00Z"), alarms.slots[slot])
    }

    @Test
    fun `a slot for a deleted medication or removed time does nothing`() = runTest {
        medications.setMedications(listOf(metformin.copy(reminderTimes = listOf(LocalTime.of(9, 0)))))
        handler.onSlotAlarm(slot, dueAt)
        medications.setMedications(emptyList())
        handler.onSlotAlarm(slot, dueAt)

        assertTrue(notifier.shown.isEmpty())
        assertTrue(alarms.slots.isEmpty())
    }

    @Test
    fun `tapping Taken records the dose at the current time and dismisses the notification`() = runTest {
        handler.onTaken(1, dueAt)

        val event = doses.current.single()
        assertEquals(DoseStatus.TAKEN, event.status)
        assertEquals(now, event.takenAt)
        assertEquals(listOf(1L to dueAt), notifier.dismissed)
    }

    @Test
    fun `tapping Snooze records it and re-reminds in 10 minutes for the same dose`() = runTest {
        handler.onSnooze(1, dueAt)

        assertEquals(DoseStatus.SNOOZED, doses.current.single().status)
        assertEquals(listOf(Triple(1L, dueAt, now.plusSeconds(600))), alarms.snoozes)
        assertEquals(listOf(1L to dueAt), notifier.dismissed)
    }

    @Test
    fun `a snoozed reminder shows again unless the dose was taken meanwhile`() = runTest {
        medications.setMedications(listOf(metformin))
        handler.onSnoozeAlarm(1, dueAt)
        assertEquals(listOf(1L to dueAt), notifier.shown)

        doses.recordDose(1, dueAt, DoseStatus.TAKEN, at = now)
        handler.onSnoozeAlarm(1, dueAt)
        assertEquals(1, notifier.shown.size)
    }
}
