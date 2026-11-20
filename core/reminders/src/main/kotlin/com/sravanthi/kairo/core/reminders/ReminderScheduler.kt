package com.sravanthi.kairo.core.reminders

import com.sravanthi.kairo.core.model.Medication
import com.sravanthi.kairo.core.model.reminderSlots
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps system alarms in step with the medication list.
 *
 * [sync] is idempotent: it (re)schedules every current slot for its next occurrence and cancels
 * slots that no longer exist, so it is safe to call on every change, after a reboot or a time zone change.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
    private val store: ScheduledSlotStore,
    private val clock: Clock,
) {
    @Synchronized
    fun sync(medications: List<Medication>) {
        val now = clock.instant()
        val current = medications.reminderSlots().toSet()

        (store.load() - current).forEach(alarmScheduler::cancelSlot)
        current.forEach { slot -> alarmScheduler.scheduleSlot(slot, slot.nextOccurrence(now, clock.zone)) }
        store.save(current)
    }
}
