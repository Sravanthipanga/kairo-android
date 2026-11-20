package com.sravanthi.kairo.core.database

import com.sravanthi.kairo.core.database.model.asEntity
import com.sravanthi.kairo.core.database.model.asExternalModel
import com.sravanthi.kairo.core.model.Medication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalTime

class KairoConvertersTest {

    private val converters = KairoConverters()

    @Test
    fun `reminder times round-trip through text`() {
        val times = listOf(LocalTime.of(8, 0), LocalTime.of(20, 30))
        val stored = converters.reminderTimesToString(times)
        assertEquals("08:00,20:30", stored)
        assertEquals(times, converters.stringToReminderTimes(stored))
    }

    @Test
    fun `empty text means no reminder times`() {
        assertEquals(emptyList<LocalTime>(), converters.stringToReminderTimes(""))
    }

    @Test
    fun `instants round-trip through epoch millis`() {
        val instant = Instant.parse("2026-03-01T07:45:00Z")
        assertEquals(instant, converters.epochMillisToInstant(converters.instantToEpochMillis(instant)))
        assertNull(converters.epochMillisToInstant(null))
    }

    @Test
    fun `saving a medication trims text, dedupes and sorts reminder times`() {
        val entity = Medication(
            name = "  Lisinopril ",
            dosage = "10 mg ",
            instructions = "   ",
            reminderTimes = listOf(LocalTime.of(21, 0), LocalTime.of(9, 0), LocalTime.of(21, 0)),
        ).asEntity()

        assertEquals("Lisinopril", entity.name)
        assertEquals("10 mg", entity.dosage)
        assertNull(entity.instructions)
        assertEquals(listOf(LocalTime.of(9, 0), LocalTime.of(21, 0)), entity.asExternalModel().reminderTimes)
    }
}
