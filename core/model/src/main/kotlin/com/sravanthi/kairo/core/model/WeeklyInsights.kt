package com.sravanthi.kairo.core.model

import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

/** Daily activity read from Health Connect. Either value may be missing for a day. */
data class DailyActivity(
    val steps: Long? = null,
    val sleep: Duration? = null,
)

data class DayInsight(
    val date: LocalDate,
    val adherence: DayProgress,
    val activity: DailyActivity,
)

/**
 * A descriptive summary of the last days: doses taken, plus steps and sleep when available.
 * It reports what happened and never draws medical conclusions.
 */
data class WeeklyInsights(
    val days: List<DayInsight>,
) {
    /** Share of due doses that were taken, or null when nothing was due. */
    val adherenceRate: Float?
        get() {
            val total = days.sumOf { it.adherence.total }
            return if (total == 0) null else days.sumOf { it.adherence.taken }.toFloat() / total
        }

    /** Average over the days that have step data, or null if none do. */
    val averageSteps: Long?
        get() = days.mapNotNull { it.activity.steps }.takeIf { it.isNotEmpty() }?.average()?.toLong()

    /** Average over the days that have sleep data, or null if none do. */
    val averageSleep: Duration?
        get() = days.mapNotNull { it.activity.sleep }.takeIf { it.isNotEmpty() }
            ?.let { list -> Duration.ofMinutes(list.sumOf { it.toMinutes() } / list.size) }
}

/**
 * Builds insights for [days] (oldest first). Each day's adherence uses the current medication list,
 * so it reflects today's schedule rather than past edits.
 */
fun buildWeeklyInsights(
    medications: List<Medication>,
    events: List<DoseEvent>,
    activity: Map<LocalDate, DailyActivity>,
    days: List<LocalDate>,
    zone: ZoneId,
): WeeklyInsights = WeeklyInsights(
    days.map { date ->
        DayInsight(
            date = date,
            adherence = buildDaySchedule(medications, events, date, zone).progress(),
            activity = activity[date] ?: DailyActivity(),
        )
    },
)

/** The [count] days ending with [today], oldest first. */
fun lastDays(today: LocalDate, count: Int = 7): List<LocalDate> =
    (count - 1 downTo 0).map { today.minusDays(it.toLong()) }
