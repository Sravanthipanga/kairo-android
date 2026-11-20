package com.sravanthi.kairo.core.reminders

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sravanthi.kairo.core.model.Medication
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.util.Objects
import javax.inject.Inject

internal class AndroidReminderNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReminderNotifier {

    private val notificationManager = NotificationManagerCompat.from(context)

    // The permission is checked just above notify(); lint cannot see it through the helper.
    @SuppressLint("MissingPermission")
    override fun show(medication: Medication, scheduledAt: Instant) {
        if (!ReminderPermissions.canPostNotifications(context)) return
        ensureChannel(context)

        val body = listOfNotNull(medication.dosage, medication.instructions).joinToString(" · ")
        // On a locked phone, show only a generic reminder: medication names are health data.
        val lockScreenVersion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.core_reminders_ic_notification)
            .setContentTitle(context.getString(R.string.core_reminders_public_title))
            .build()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.core_reminders_ic_notification)
            .setContentTitle(context.getString(R.string.core_reminders_title, medication.name))
            .setContentText(body)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setWhen(scheduledAt.toEpochMilli())
            .setShowWhen(true)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(lockScreenVersion)
            .setContentIntent(ReminderIntents.openApp(context))
            .addAction(
                0,
                context.getString(R.string.core_reminders_action_taken),
                ReminderIntents.takenAction(context, medication.id, scheduledAt),
            )
            .addAction(
                0,
                context.getString(R.string.core_reminders_action_snooze),
                ReminderIntents.snoozeAction(context, medication.id, scheduledAt),
            )
            .build()

        notificationManager.notify(notificationId(medication.id, scheduledAt), notification)
    }

    override fun dismiss(medicationId: Long, scheduledAt: Instant) =
        notificationManager.cancel(notificationId(medicationId, scheduledAt))

    /** One notification per dose: a snoozed reminder replaces the original instead of stacking. */
    private fun notificationId(medicationId: Long, scheduledAt: Instant) =
        Objects.hash(medicationId, scheduledAt.toEpochMilli())

    companion object {
        private const val CHANNEL_ID = "dose_reminders"

        fun ensureChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.core_reminders_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = context.getString(R.string.core_reminders_channel_description) }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
