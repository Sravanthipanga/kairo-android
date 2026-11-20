package com.sravanthi.kairo.core.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import com.sravanthi.kairo.core.model.ReminderSlot
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject

internal class AndroidAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun scheduleSlot(slot: ReminderSlot, at: Instant) =
        set(at, ReminderIntents.slotAlarm(context, slot, scheduledAt = at))

    override fun cancelSlot(slot: ReminderSlot) {
        ReminderIntents.existingSlotAlarm(context, slot)?.let { pendingIntent ->
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    override fun scheduleSnooze(medicationId: Long, scheduledAt: Instant, at: Instant) =
        set(at, ReminderIntents.snoozeAlarm(context, medicationId, scheduledAt))

    /**
     * Uses an exact alarm when the user allows it, so the reminder arrives on time even in Doze.
     * Otherwise falls back to an inexact alarm, which Android may deliver a few minutes late.
     */
    private fun set(at: Instant, operation: PendingIntent) {
        if (ReminderPermissions.canScheduleExactAlarms(context)) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.toEpochMilli(), operation)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.toEpochMilli(), operation)
        }
    }
}
