package com.sravanthi.kairo.core.model

import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Kairo's adaptive timing: when someone reliably takes a dose at a different time than its reminder,
 * suggest moving the reminder to match their real routine.
 *
 * It only ever *suggests*. For medication, a person must approve every schedule change.
 */
data class TimingSuggestion(
    val medication: Medication,
    val currentTime: LocalTime,
    val suggestedTime: LocalTime,
    /** Typical (median) minutes between the reminder and the dose being taken. Negative means early. */
    val typicalDelayMinutes: Long,
    /** How many taken doses the suggestion is based on. */
    val sampleCount: Int,
)

/** A user said "not now" to a suggestion for one reminder slot. */
data class SuggestionDismissal(
    val medicationId: Long,
    val reminderTime: LocalTime,
    val dismissedAt: Instant,
)

/** The guard rails. Conservative by design: a wrong suggestion for medication is worse than none. */
data class AdaptiveTimingPolicy(
    /** Only doses from this recent window count, so the suggestion follows the current routine. */
    val window: Duration = Duration.ofDays(14),
    /** Not enough evidence below this many taken doses. */
    val minSamples: Int = 5,
    /** Typical delays smaller than this are normal variation, not a habit. */
    val minShiftMinutes: Long = 15,
    /** One suggestion never moves a reminder further than this. */
    val maxShiftMinutes: Long = 60,
    /** The middle half of delays must fall within this many minutes, i.e. the habit is consistent. */
    val maxSpreadMinutes: Long = 30,
    /** Doses taken this far from the reminder are one-off days and are ignored. */
    val outlierMinutes: Long = 180,
    /** Suggested times are rounded to a friendly step, e.g. 8:40 rather than 8:37. */
    val roundToMinutes: Long = 5,
    /** After "not now", stay quiet about that reminder for this long. */
    val dismissCooldown: Duration = Duration.ofDays(14),
)

/** Suggestions for every active reminder slot whose recent history shows a consistent, sizeable shift. */
fun buildTimingSuggestions(
    medications: List<Medication>,
    events: List<DoseEvent>,
    dismissals: List<SuggestionDismissal>,
    now: Instant,
    zone: ZoneId,
    policy: AdaptiveTimingPolicy = AdaptiveTimingPolicy(),
): List<TimingSuggestion> {
    val windowStart = now.minus(policy.window)
    val recentTaken = events.filter {
        it.status == DoseStatus.TAKEN && it.takenAt != null &&
            !it.scheduledAt.isBefore(windowStart) && it.scheduledAt.isBefore(now)
    }

    return medications.filter { it.isActive }.flatMap { medication ->
        medication.reminderTimes.distinct().mapNotNull { time ->
            val recentlyDismissed = dismissals.any {
                it.medicationId == medication.id && it.reminderTime == time &&
                    it.dismissedAt.plus(policy.dismissCooldown).isAfter(now)
            }
            if (recentlyDismissed) return@mapNotNull null

            val delays = recentTaken
                .filter { it.medicationId == medication.id && it.scheduledAt.atZone(zone).toLocalTime() == time }
                .mapNotNull { it.delay?.toMinutes() }
            suggestionFor(medication, time, delays, policy)
        }
    }
}

/** Decides on one slot from its delays in minutes (positive = taken late). */
internal fun suggestionFor(
    medication: Medication,
    time: LocalTime,
    delayMinutes: List<Long>,
    policy: AdaptiveTimingPolicy,
): TimingSuggestion? {
    val delays = delayMinutes.filter { abs(it) <= policy.outlierMinutes }.sorted()
    if (delays.size < policy.minSamples) return null

    val spread = percentile(delays, 0.75) - percentile(delays, 0.25)
    if (spread > policy.maxSpreadMinutes) return null

    val typical = percentile(delays, 0.5)
    val step = policy.roundToMinutes.toDouble()
    val shift = ((typical / step).roundToLong() * policy.roundToMinutes)
        .coerceIn(-policy.maxShiftMinutes, policy.maxShiftMinutes)
    if (abs(shift) < policy.minShiftMinutes) return null

    val suggested = time.plusMinutes(shift)
    // Never move a reminder across midnight: that would silently change which day a dose belongs to.
    val crossesMidnight = (shift > 0 && suggested.isBefore(time)) || (shift < 0 && suggested.isAfter(time))
    if (crossesMidnight || suggested in medication.reminderTimes) return null

    return TimingSuggestion(
        medication = medication,
        currentTime = time,
        suggestedTime = suggested,
        typicalDelayMinutes = typical.roundToLong(),
        sampleCount = delays.size,
    )
}

/** Linear-interpolated percentile of an already sorted, non-empty list. */
internal fun percentile(sorted: List<Long>, p: Double): Double {
    val position = p * (sorted.size - 1)
    val lower = sorted[position.toInt()]
    val upper = sorted[minOf(position.toInt() + 1, sorted.size - 1)]
    return lower + (upper - lower) * (position - position.toInt())
}

/** The medication with [suggestion]'s reminder moved to the suggested time. */
fun Medication.applying(suggestion: TimingSuggestion): Medication =
    copy(reminderTimes = (reminderTimes - suggestion.currentTime + suggestion.suggestedTime).sorted())
