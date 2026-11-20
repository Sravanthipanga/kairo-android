package com.sravanthi.kairo.feature.widget

import com.sravanthi.kairo.core.model.DayProgress
import com.sravanthi.kairo.core.model.DoseStatus
import com.sravanthi.kairo.core.model.ScheduledDose
import com.sravanthi.kairo.core.model.progress
import java.time.Instant

/** Everything the widget shows, decided by plain Kotlin so it can be unit-tested. */
sealed interface WidgetSummary {
    data object NoMedications : WidgetSummary

    data class AllDone(val progress: DayProgress) : WidgetSummary

    data class Next(
        val dose: ScheduledDose,
        val isOverdue: Boolean,
        val progress: DayProgress,
        /** App lock is on: show only the time, never the medication, on the home screen. */
        val hideDetails: Boolean,
    ) : WidgetSummary
}

/**
 * The next dose still to take today: the earliest pending or snoozed one, so an overdue dose
 * stays at the top until it is handled rather than being skipped for a later one.
 */
fun buildWidgetSummary(
    schedule: List<ScheduledDose>,
    hasMedications: Boolean,
    now: Instant,
    appLockEnabled: Boolean,
): WidgetSummary {
    if (!hasMedications) return WidgetSummary.NoMedications
    val progress = schedule.progress()
    val next = schedule.firstOrNull { it.status == DoseStatus.PENDING || it.status == DoseStatus.SNOOZED }
        ?: return WidgetSummary.AllDone(progress)
    return WidgetSummary.Next(
        dose = next,
        isOverdue = next.scheduledAt.isBefore(now),
        progress = progress,
        hideDetails = appLockEnabled,
    )
}
