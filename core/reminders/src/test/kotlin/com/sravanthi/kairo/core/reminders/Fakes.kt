package com.sravanthi.kairo.core.reminders

import com.sravanthi.kairo.core.model.Medication
import com.sravanthi.kairo.core.model.ReminderSlot
import java.time.Instant

class FakeAlarmScheduler : AlarmScheduler {
    val slots = mutableMapOf<ReminderSlot, Instant>()
    val cancelled = mutableListOf<ReminderSlot>()
    val snoozes = mutableListOf<Triple<Long, Instant, Instant>>()

    override fun scheduleSlot(slot: ReminderSlot, at: Instant) {
        slots[slot] = at
    }

    override fun cancelSlot(slot: ReminderSlot) {
        slots.remove(slot)
        cancelled += slot
    }

    override fun scheduleSnooze(medicationId: Long, scheduledAt: Instant, at: Instant) {
        snoozes += Triple(medicationId, scheduledAt, at)
    }
}

class FakeNotifier : ReminderNotifier {
    val shown = mutableListOf<Pair<Long, Instant>>()
    val dismissed = mutableListOf<Pair<Long, Instant>>()

    override fun show(medication: Medication, scheduledAt: Instant) {
        shown += medication.id to scheduledAt
    }

    override fun dismiss(medicationId: Long, scheduledAt: Instant) {
        dismissed += medicationId to scheduledAt
    }
}

class FakeSlotStore : ScheduledSlotStore {
    var slots: Set<ReminderSlot> = emptySet()
    override fun load() = slots
    override fun save(slots: Set<ReminderSlot>) {
        this.slots = slots
    }
}
