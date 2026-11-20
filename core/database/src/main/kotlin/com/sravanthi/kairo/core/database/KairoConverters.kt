package com.sravanthi.kairo.core.database

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalTime

/** Room type converters. Times are stored as "HH:mm" text so the database stays readable. */
internal class KairoConverters {

    @TypeConverter
    fun reminderTimesToString(times: List<LocalTime>): String =
        times.joinToString(SEPARATOR) { it.toString() }

    @TypeConverter
    fun stringToReminderTimes(value: String): List<LocalTime> =
        if (value.isBlank()) emptyList() else value.split(SEPARATOR).map(LocalTime::parse)

    @TypeConverter
    fun instantToEpochMillis(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun epochMillisToInstant(millis: Long?): Instant? = millis?.let(Instant::ofEpochMilli)

    private companion object {
        const val SEPARATOR = ","
    }
}
