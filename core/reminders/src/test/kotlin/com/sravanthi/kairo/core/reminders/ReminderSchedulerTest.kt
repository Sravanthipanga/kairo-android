package com.sravanthi.kairo.core.reminders

import com.sravanthi.kairo.core.model.Medication
import com.sravanthi.kairo.core.model.ReminderSlot
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset

class ReminderSchedulerTest {

    private val alarms = FakeAlarmScheduler()
    private val store = FakeSlotStore()
    private val clock = Clock.fixed(Instant.parse("2026-03-10T12:00:00Z"), ZoneOffset.UTC)
    private val scheduler = ReminderScheduler(alarms, store, clock)

    private val metformin = Medication(1, "Metformin", "500 mg", null, listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)))

    @Test
    fun `each slot is scheduled for its next occurrence`() {
        scheduler.sync(listOf(metformin))

        assertEquals(
            mapOf(
                ReminderSlot(1, LocalTime.of(8, 0)) to Instant.parse("2026-03-11T08:00:00Z"),
                ReminderSlot(1, LocalTime.of(20, 0)) to Instant.parse("2026-03-10T20:00:00Z"),
            ),
            alarms.slots,
        )
        assertEquals(alarms.slots.keys, store.slots)
    }

    @Test
    fun `removed times and deleted medications have their alarms cancelled`() {
        val aspirin = Medication(2, "Aspirin", "81 mg", null, listOf(LocalTime.of(9, 0)))
        scheduler.sync(listOf(metformin, aspirin))

        scheduler.sync(listOf(metformin.copy(reminderTimes = listOf(LocalTime.of(8, 0)))))

        assertEquals(
            setOf(ReminderSlot(1, LocalTime.of(20, 0)), ReminderSlot(2, LocalTime.of(9, 0))),
            alarms.cancelled.toSet(),
        )
        assertEquals(setOf(ReminderSlot(1, LocalTime.of(8, 0))), store.slots)
    }

    @Test
    fun `syncing twice is harmless`() {
        scheduler.sync(listOf(metformin))
        scheduler.sync(listOf(metformin))
        assertEquals(2, alarms.slots.size)
        assertEquals(emptyList<ReminderSlot>(), alarms.cancelled)
    }
}
