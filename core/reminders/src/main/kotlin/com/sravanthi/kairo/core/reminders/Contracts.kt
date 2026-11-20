package com.sravanthi.kairo.core.reminders

import com.sravanthi.kairo.core.model.Medication
import com.sravanthi.kairo.core.model.ReminderSlot
import java.time.Instant

/** Thin wrapper over the system alarm service, so reminder logic can be unit-tested. */
interface AlarmScheduler {
    /** Schedules (or replaces) the daily alarm for [slot] to fire at [at]. */
    fun scheduleSlot(slot: ReminderSlot, at: Instant)

    fun cancelSlot(slot: ReminderSlot)

    /** Schedules a one-off re-reminder for the dose of [medicationId] originally due at [scheduledAt]. */
    fun scheduleSnooze(medicationId: Long, scheduledAt: Instant, at: Instant)
}

/** Shows and dismisses dose notifications. */
interface ReminderNotifier {
    fun show(medication: Medication, scheduledAt: Instant)
    fun dismiss(medicationId: Long, scheduledAt: Instant)
}

/** Remembers which slots currently have an alarm, so removed slots can be cancelled. */
interface ScheduledSlotStore {
    fun load(): Set<ReminderSlot>
    fun save(slots: Set<ReminderSlot>)
}
