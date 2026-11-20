package com.sravanthi.kairo.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class AdaptiveTimingTest {

    private val zone = ZoneId.of("America/Chicago")
    private val today = LocalDate.of(2026, 3, 20)
    private val now = today.atTime(12, 0).atZone(zone).toInstant()
    private val policy = AdaptiveTimingPolicy()

    private val metformin = Medication(1, "Metformin", "500 mg", null, listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)))

    /** A dose of [med] at [time], [daysAgo] days ago, taken [delay] minutes after the reminder. */
    private fun taken(daysAgo: Long, delay: Long, time: LocalTime = LocalTime.of(8, 0), med: Long = 1): DoseEvent {
        val scheduled = today.minusDays(daysAgo).atTime(time).atZone(zone).toInstant()
        return DoseEvent(medicationId = med, scheduledAt = scheduled, takenAt = scheduled.plusSeconds(delay * 60), status = DoseStatus.TAKEN)
    }

    private fun suggest(events: List<DoseEvent>, dismissals: List<SuggestionDismissal> = emptyList()) =
        buildTimingSuggestions(listOf(metformin), events, dismissals, now, zone, policy)

    @Test
    fun `a consistent late habit suggests moving the reminder, rounded to 5 minutes`() {
        val events = listOf(38L, 42, 40, 44, 37, 41, 39).mapIndexed { i, d -> taken(daysAgo = i + 1L, delay = d) }

        val suggestion = suggest(events).single()

        assertEquals(LocalTime.of(8, 0), suggestion.currentTime)
        assertEquals(LocalTime.of(8, 40), suggestion.suggestedTime)
        assertEquals(40, suggestion.typicalDelayMinutes)
        assertEquals(7, suggestion.sampleCount)
    }

    @Test
    fun `a consistent early habit suggests an earlier reminder`() {
        val events = (1L..6).map { taken(daysAgo = it, delay = -25) }
        assertEquals(LocalTime.of(7, 35), suggest(events).single().suggestedTime)
    }

    @Test
    fun `too few doses is not enough evidence`() {
        val events = (1L..4).map { taken(daysAgo = it, delay = 40) }
        assertTrue(suggest(events).isEmpty())
    }

    @Test
    fun `small differences are normal variation`() {
        val events = (1L..7).map { taken(daysAgo = it, delay = 10) }
        assertTrue(suggest(events).isEmpty())
    }

    @Test
    fun `an inconsistent routine gets no suggestion`() {
        val events = listOf(5L, 70, 15, 90, 30, 110, 45).mapIndexed { i, d -> taken(daysAgo = i + 1L, delay = d) }
        assertTrue(suggest(events).isEmpty())
    }

    @Test
    fun `one-off very late days are ignored rather than skewing the result`() {
        val events = listOf(30L, 32, 29, 31, 30, 600, 900).mapIndexed { i, d -> taken(daysAgo = i + 1L, delay = d) }
        val suggestion = suggest(events).single()
        assertEquals(LocalTime.of(8, 30), suggestion.suggestedTime)
        assertEquals(5, suggestion.sampleCount)
    }

    @Test
    fun `a single suggestion never moves a reminder more than an hour`() {
        val events = (1L..7).map { taken(daysAgo = it, delay = 150) }
        assertEquals(LocalTime.of(9, 0), suggest(events).single().suggestedTime)
    }

    @Test
    fun `only the recent window counts`() {
        val old = (20L..30).map { taken(daysAgo = it, delay = 45) }
        val recent = (1L..3).map { taken(daysAgo = it, delay = 45) }
        assertTrue(suggest(old + recent).isEmpty())
    }

    @Test
    fun `skipped doses and other reminder slots are not mixed in`() {
        val evening = (1L..7).map { taken(daysAgo = it, delay = 45, time = LocalTime.of(20, 0)) }
        val skipped = (1L..7).map { taken(daysAgo = it, delay = 45).copy(status = DoseStatus.SKIPPED) }
        val suggestions = suggest(evening + skipped)
        assertEquals(listOf(LocalTime.of(20, 0)), suggestions.map { it.currentTime })
    }

    @Test
    fun `dismissed suggestions stay quiet during the cooldown, then come back`() {
        val events = (1L..7).map { taken(daysAgo = it, delay = 40) }
        val recent = SuggestionDismissal(1, LocalTime.of(8, 0), now.minus(Duration.ofDays(3)))
        val expired = recent.copy(dismissedAt = now.minus(Duration.ofDays(15)))

        assertTrue(suggest(events, listOf(recent)).isEmpty())
        assertEquals(1, suggest(events, listOf(expired)).size)
    }

    @Test
    fun `never crosses midnight or lands on another reminder of the same medication`() {
        val lateNight = metformin.copy(reminderTimes = listOf(LocalTime.of(23, 30)))
        assertNull(suggestionFor(lateNight, LocalTime.of(23, 30), List(7) { 45L }, policy))

        val twoTimes = metformin.copy(reminderTimes = listOf(LocalTime.of(8, 0), LocalTime.of(8, 30)))
        assertNull(suggestionFor(twoTimes, LocalTime.of(8, 0), List(7) { 30L }, policy))
    }

    @Test
    fun `applying a suggestion swaps only that reminder time`() {
        val suggestion = TimingSuggestion(metformin, LocalTime.of(8, 0), LocalTime.of(8, 40), 40, 7)
        assertEquals(listOf(LocalTime.of(8, 40), LocalTime.of(20, 0)), metformin.applying(suggestion).reminderTimes)
    }

    @Test
    fun `percentile interpolates between values`() {
        val values = listOf(10L, 20, 30, 40)
        assertEquals(10.0, percentile(values, 0.0), 0.0)
        assertEquals(25.0, percentile(values, 0.5), 0.0)
        assertEquals(40.0, percentile(values, 1.0), 0.0)
    }
}
