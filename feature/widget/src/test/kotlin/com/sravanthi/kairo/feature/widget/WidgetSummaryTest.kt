package com.sravanthi.kairo.feature.widget

import com.sravanthi.kairo.core.model.DayProgress
import com.sravanthi.kairo.core.model.DoseEvent
import com.sravanthi.kairo.core.model.DoseStatus
import com.sravanthi.kairo.core.model.Medication
import com.sravanthi.kairo.core.model.buildDaySchedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

class WidgetSummaryTest {

    private val date = LocalDate.of(2026, 3, 10)
    private val metformin = Medication(1, "Metformin", "500 mg", null, listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)))
    private fun at(h: Int): Instant = date.atTime(h, 0).toInstant(ZoneOffset.UTC)

    private fun summary(events: List<DoseEvent> = emptyList(), now: Instant = at(12), appLock: Boolean = false) =
        buildWidgetSummary(
            schedule = buildDaySchedule(listOf(metformin), events, date, ZoneOffset.UTC),
            hasMedications = true,
            now = now,
            appLockEnabled = appLock,
        )

    @Test
    fun `no medications prompts to add one`() {
        assertEquals(WidgetSummary.NoMedications, buildWidgetSummary(emptyList(), hasMedications = false, at(12), false))
    }

    @Test
    fun `an overdue dose stays first until it is handled`() {
        val next = summary() as WidgetSummary.Next
        assertEquals(at(8), next.dose.scheduledAt)
        assertTrue(next.isOverdue)
        assertEquals(DayProgress(taken = 0, total = 2), next.progress)
    }

    @Test
    fun `after taking the morning dose, the evening dose is next and not overdue`() {
        val taken = DoseEvent(medicationId = 1, scheduledAt = at(8), takenAt = at(8), status = DoseStatus.TAKEN)
        val next = summary(listOf(taken)) as WidgetSummary.Next
        assertEquals(at(20), next.dose.scheduledAt)
        assertEquals(false, next.isOverdue)
        assertEquals(DayProgress(taken = 1, total = 2), next.progress)
    }

    @Test
    fun `a snoozed dose still counts as next`() {
        val snoozed = DoseEvent(medicationId = 1, scheduledAt = at(8), status = DoseStatus.SNOOZED)
        assertEquals(at(8), (summary(listOf(snoozed)) as WidgetSummary.Next).dose.scheduledAt)
    }

    @Test
    fun `when every dose is taken or skipped the day is done`() {
        val events = listOf(
            DoseEvent(medicationId = 1, scheduledAt = at(8), takenAt = at(8), status = DoseStatus.TAKEN),
            DoseEvent(medicationId = 1, scheduledAt = at(20), status = DoseStatus.SKIPPED),
        )
        assertEquals(WidgetSummary.AllDone(DayProgress(taken = 1, total = 2)), summary(events, now = at(21)))
    }

    @Test
    fun `app lock hides the medication on the home screen`() {
        assertEquals(true, (summary(appLock = true) as WidgetSummary.Next).hideDetails)
        assertEquals(false, (summary(appLock = false) as WidgetSummary.Next).hideDetails)
    }
}
