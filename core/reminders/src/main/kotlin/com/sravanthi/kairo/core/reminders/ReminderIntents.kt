package com.sravanthi.kairo.core.reminders

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.sravanthi.kairo.core.model.ReminderSlot
import java.time.Instant

/**
 * Builds the intents behind alarms and notification buttons.
 *
 * Each intent gets a unique data URI, so Android treats every slot and every dose as a separate
 * PendingIntent. Rescheduling a slot replaces its alarm instead of adding a second one.
 */
internal object ReminderIntents {
    const val ACTION_SLOT_ALARM = "com.sravanthi.kairo.reminders.SLOT_ALARM"
    const val ACTION_SNOOZE_ALARM = "com.sravanthi.kairo.reminders.SNOOZE_ALARM"
    const val ACTION_TAKEN = "com.sravanthi.kairo.reminders.TAKEN"
    const val ACTION_SNOOZE = "com.sravanthi.kairo.reminders.SNOOZE"

    const val EXTRA_SLOT_KEY = "slot_key"
    const val EXTRA_MEDICATION_ID = "medication_id"
    const val EXTRA_SCHEDULED_AT = "scheduled_at"

    private const val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    fun slotAlarm(context: Context, slot: ReminderSlot, scheduledAt: Instant): PendingIntent =
        broadcast(context, slotIntent(context, slot).putExtra(EXTRA_SCHEDULED_AT, scheduledAt.toEpochMilli()))

    /** Matches an existing slot alarm for cancelling, or null if none is scheduled. */
    fun existingSlotAlarm(context: Context, slot: ReminderSlot): PendingIntent? =
        PendingIntent.getBroadcast(
            context,
            0,
            slotIntent(context, slot),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )

    fun snoozeAlarm(context: Context, medicationId: Long, scheduledAt: Instant): PendingIntent =
        broadcast(context, doseIntent(context, ACTION_SNOOZE_ALARM, "snooze-alarm", medicationId, scheduledAt))

    fun takenAction(context: Context, medicationId: Long, scheduledAt: Instant): PendingIntent =
        broadcast(context, doseIntent(context, ACTION_TAKEN, "taken", medicationId, scheduledAt))

    fun snoozeAction(context: Context, medicationId: Long, scheduledAt: Instant): PendingIntent =
        broadcast(context, doseIntent(context, ACTION_SNOOZE, "snooze", medicationId, scheduledAt))

    /** Opens the app on the Today screen when the notification itself is tapped. */
    fun openApp(context: Context): PendingIntent? =
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { launch ->
            PendingIntent.getActivity(context, 0, launch, FLAGS)
        }

    private fun slotIntent(context: Context, slot: ReminderSlot) =
        Intent(context, ReminderReceiver::class.java)
            .setAction(ACTION_SLOT_ALARM)
            .setData("kairo://reminders/slot/${slot.medicationId}/${slot.time}".toUri())
            .putExtra(EXTRA_SLOT_KEY, slot.key)

    private fun doseIntent(context: Context, action: String, kind: String, medicationId: Long, scheduledAt: Instant) =
        Intent(context, ReminderReceiver::class.java)
            .setAction(action)
            .setData("kairo://reminders/$kind/$medicationId/${scheduledAt.toEpochMilli()}".toUri())
            .putExtra(EXTRA_MEDICATION_ID, medicationId)
            .putExtra(EXTRA_SCHEDULED_AT, scheduledAt.toEpochMilli())

    private fun broadcast(context: Context, intent: Intent): PendingIntent =
        PendingIntent.getBroadcast(context, 0, intent, FLAGS)
}
