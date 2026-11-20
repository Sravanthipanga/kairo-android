package com.sravanthi.kairo.core.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class DayScheduleTest {

    private val zone = ZoneId.of("America/Chicago")
    private val date = LocalDate.of(2026, 3, 10)

    private val metformin = Medication(1, "Metformin", "500 mg", null, listOf(LocalTime.of(20, 0), LocalTime.of(8, 0)))
    private val aspirin = Medication(2, "aspirin", "81 mg", null, listOf(LocalTime.of(8, 0)))

    private fun at(time: String): Instant = date.atTime(LocalTime.parse(time)).atZone(zone).toInstant()

    @Test
    fun `one dose per active medication per reminder time, ordered by time then name`() {
        val schedule = buildDaySchedule(listOf(metformin, aspirin), emptyList(), date, zone)

        assertEquals(
            listOf("aspirin" to at("08:00"), "Metformin" to at("08:00"), "Metformin" to at("20:00")),
            schedule.map { it.medication.name to it.scheduledAt },
        )
        assertEquals(List(3) { DoseStatus.PENDING }, schedule.map { it.status })
    }

    @Test
    fun `inactive medications are left out`() {
        val schedule = buildDaySchedule(listOf(metformin.copy(isActive = false), aspirin), emptyList(), date, zone)
        assertEquals(listOf("aspirin"), schedule.map { it.medication.name })
    }

    @Test
    fun `recorded events are matched to their exact slot`() {
        val taken = DoseEvent(10, medicationId = 1, scheduledAt = at("08:00"), takenAt = at("08:12"), status = DoseStatus.TAKEN)
        val otherDay = DoseEvent(11, medicationId = 1, scheduledAt = at("20:00").minusSeconds(86_400), status = DoseStatus.SKIPPED)

        val schedule = buildDaySchedule(listOf(metformin), listOf(taken, otherDay), date, zone)

        assertEquals(listOf(DoseStatus.TAKEN, DoseStatus.PENDING), schedule.map { it.status })
        assertEquals(taken, schedule.first().event)
    }

    @Test
    fun `progress counts taken doses only`() {
        val schedule = buildDaySchedule(
            listOf(metformin, aspirin),
            listOf(
                DoseEvent(medicationId = 1, scheduledAt = at("08:00"), status = DoseStatus.TAKEN),
                DoseEvent(medicationId = 2, scheduledAt = at("08:00"), status = DoseStatus.SKIPPED),
            ),
            date,
            zone,
        )
        assertEquals(DayProgress(taken = 1, total = 3), schedule.progress())
        assertEquals(0f, emptyList<ScheduledDose>().progress().fraction)
    }

    @Test
    fun `daylight saving day still schedules at local wall-clock time`() {
        // 2026-03-08 is the US spring-forward day.
        val dstDay = LocalDate.of(2026, 3, 8)
        val schedule = buildDaySchedule(listOf(aspirin), emptyList(), dstDay, zone)
        assertEquals(LocalTime.of(8, 0), schedule.single().scheduledAt.atZone(zone).toLocalTime())
    }
}
