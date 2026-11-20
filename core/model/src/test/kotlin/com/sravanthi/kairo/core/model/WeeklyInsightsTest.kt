package com.sravanthi.kairo.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

class WeeklyInsightsTest {

    private val today = LocalDate.of(2026, 3, 10)
    private val days = lastDays(today, 3)
    private val med = Medication(1, "Metformin", "500 mg", null, listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)))

    private fun taken(date: LocalDate, hour: Int): DoseEvent {
        val at = date.atTime(hour, 0).toInstant(ZoneOffset.UTC)
        return DoseEvent(medicationId = 1, scheduledAt = at, takenAt = at, status = DoseStatus.TAKEN)
    }

    @Test
    fun `last days run oldest first and end today`() {
        assertEquals(listOf(LocalDate.of(2026, 3, 8), LocalDate.of(2026, 3, 9), today), days)
    }

    @Test
    fun `adherence is counted per day and overall`() {
        val events = listOf(taken(days[0], 8), taken(days[0], 20), taken(days[1], 8), taken(today, 8))
        val insights = buildWeeklyInsights(listOf(med), events, emptyMap(), days, ZoneOffset.UTC)

        assertEquals(listOf(2, 1, 1), insights.days.map { it.adherence.taken })
        assertEquals(listOf(2, 2, 2), insights.days.map { it.adherence.total })
        assertEquals(4f / 6, insights.adherenceRate!!, 0.0001f)
    }

    @Test
    fun `no doses due means no adherence rate rather than zero`() {
        assertNull(buildWeeklyInsights(emptyList(), emptyList(), emptyMap(), days, ZoneOffset.UTC).adherenceRate)
    }

    @Test
    fun `activity averages only use days that have data`() {
        val activity = mapOf(
            days[0] to DailyActivity(steps = 6_000, sleep = Duration.ofMinutes(420)),
            days[1] to DailyActivity(steps = 10_000),
        )
        val insights = buildWeeklyInsights(listOf(med), emptyList(), activity, days, ZoneOffset.UTC)

        assertEquals(8_000L, insights.averageSteps)
        assertEquals(Duration.ofMinutes(420), insights.averageSleep)
        assertEquals(DailyActivity(), insights.days[2].activity)
    }

    @Test
    fun `without health data the averages are empty`() {
        val insights = buildWeeklyInsights(listOf(med), emptyList(), emptyMap(), days, ZoneOffset.UTC)
        assertNull(insights.averageSteps)
        assertNull(insights.averageSleep)
    }
}
