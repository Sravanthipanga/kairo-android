package com.sravanthi.kairo.core.reminders

import com.sravanthi.kairo.core.data.repository.DoseRepository
import com.sravanthi.kairo.core.data.repository.MedicationRepository
import com.sravanthi.kairo.core.model.DoseStatus
import com.sravanthi.kairo.core.model.ReminderSlot
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/** What happens when a reminder alarm fires or the user taps a notification button. */
class ReminderActionHandler @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val doseRepository: DoseRepository,
    private val alarmScheduler: AlarmScheduler,
    private val notifier: ReminderNotifier,
    private val clock: Clock,
) {

    /** A daily slot alarm fired for the dose due at [scheduledAt]. */
    suspend fun onSlotAlarm(slot: ReminderSlot, scheduledAt: Instant) {
        val medication = medicationRepository.observeMedication(slot.medicationId).first()
        // The medication was deleted, paused or the time removed; its alarm was (or will be) cancelled by sync.
        if (medication == null || !medication.isActive || slot.time !in medication.reminderTimes) return

        // Re-arm for tomorrow first, so a failure below never silences future reminders.
        alarmScheduler.scheduleSlot(slot, slot.nextOccurrence(scheduledAt, clock.zone))

        if (!isAlreadyHandled(slot.medicationId, scheduledAt)) notifier.show(medication, scheduledAt)
    }

    /** A snoozed reminder came due again. */
    suspend fun onSnoozeAlarm(medicationId: Long, scheduledAt: Instant) {
        val medication = medicationRepository.observeMedication(medicationId).first() ?: return
        if (!isAlreadyHandled(medicationId, scheduledAt)) notifier.show(medication, scheduledAt)
    }

    /** "Taken" was tapped on the notification. */
    suspend fun onTaken(medicationId: Long, scheduledAt: Instant) {
        doseRepository.recordDose(medicationId, scheduledAt, DoseStatus.TAKEN, at = clock.instant())
        notifier.dismiss(medicationId, scheduledAt)
    }

    /** "Snooze" was tapped on the notification. */
    suspend fun onSnooze(medicationId: Long, scheduledAt: Instant) {
        doseRepository.recordDose(medicationId, scheduledAt, DoseStatus.SNOOZED, at = clock.instant())
        alarmScheduler.scheduleSnooze(medicationId, scheduledAt, at = clock.instant().plus(SNOOZE_DURATION))
        notifier.dismiss(medicationId, scheduledAt)
    }

    /** True when the dose was already taken or skipped, e.g. from the Today screen before the alarm fired. */
    private suspend fun isAlreadyHandled(medicationId: Long, scheduledAt: Instant): Boolean {
        val event = doseRepository.observeDoses(scheduledAt, scheduledAt.plusMillis(1)).first()
            .firstOrNull { it.medicationId == medicationId }
        return event?.status == DoseStatus.TAKEN || event?.status == DoseStatus.SKIPPED
    }

    companion object {
        val SNOOZE_DURATION: Duration = Duration.ofMinutes(10)
    }
}
