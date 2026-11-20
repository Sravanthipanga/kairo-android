package com.sravanthi.kairo.core.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sravanthi.kairo.core.model.ReminderSlot
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant

/** Receives reminder alarms and notification button taps. */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val handler = EntryPointAccessors
            .fromApplication(context.applicationContext, ReminderEntryPoint::class.java)
            .actionHandler()
        val scheduledAt = intent.getLongExtra(ReminderIntents.EXTRA_SCHEDULED_AT, -1)
            .takeIf { it >= 0 }?.let(Instant::ofEpochMilli) ?: return
        val medicationId = intent.getLongExtra(ReminderIntents.EXTRA_MEDICATION_ID, -1)

        goAsyncLaunch {
            when (intent.action) {
                ReminderIntents.ACTION_SLOT_ALARM -> {
                    val slot = intent.getStringExtra(ReminderIntents.EXTRA_SLOT_KEY)?.let(ReminderSlot::fromKey)
                    if (slot != null) handler.onSlotAlarm(slot, scheduledAt)
                }
                ReminderIntents.ACTION_SNOOZE_ALARM -> handler.onSnoozeAlarm(medicationId, scheduledAt)
                ReminderIntents.ACTION_TAKEN -> handler.onTaken(medicationId, scheduledAt)
                ReminderIntents.ACTION_SNOOZE -> handler.onSnooze(medicationId, scheduledAt)
            }
        }
    }
}

/** Re-creates every alarm when the system clears or shifts them. */
class RescheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in RESCHEDULE_ACTIONS) return
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, ReminderEntryPoint::class.java)
        goAsyncLaunch {
            val medications = entryPoint.medicationRepository().observeActiveMedications().first()
            entryPoint.scheduler().sync(medications)
        }
    }

    private companion object {
        val RESCHEDULE_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED",
        )
    }
}

private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

/** Runs [block] off the main thread while keeping the receiver alive until it finishes. */
private fun BroadcastReceiver.goAsyncLaunch(block: suspend () -> Unit) {
    val pendingResult = goAsync()
    receiverScope.launch {
        try {
            block()
        } finally {
            pendingResult.finish()
        }
    }
}
