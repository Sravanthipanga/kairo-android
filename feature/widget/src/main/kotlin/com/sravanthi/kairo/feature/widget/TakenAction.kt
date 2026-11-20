package com.sravanthi.kairo.feature.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.EntryPointAccessors
import java.time.Instant

/** The widget's Taken button: records the dose exactly like the notification's Taken button. */
class TakenAction : ActionCallback {

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val medicationId = parameters[MEDICATION_ID] ?: return
        val scheduledAt = parameters[SCHEDULED_AT]?.let(Instant::ofEpochMilli) ?: return
        EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .reminderActionHandler()
            .onTaken(medicationId, scheduledAt)
        KairoWidget().updateAll(context)
    }

    companion object {
        val MEDICATION_ID = ActionParameters.Key<Long>("medication_id")
        val SCHEDULED_AT = ActionParameters.Key<Long>("scheduled_at")
    }
}
