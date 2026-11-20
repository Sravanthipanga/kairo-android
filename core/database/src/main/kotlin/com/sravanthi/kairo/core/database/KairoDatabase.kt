package com.sravanthi.kairo.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sravanthi.kairo.core.database.dao.DoseEventDao
import com.sravanthi.kairo.core.database.dao.MedicationDao
import com.sravanthi.kairo.core.database.model.DoseEventEntity
import com.sravanthi.kairo.core.database.model.MedicationEntity

@Database(
    entities = [MedicationEntity::class, DoseEventEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        // v2: unique (medicationId, scheduledAt) index on dose_events.
        AutoMigration(from = 1, to = 2),
    ],
)
@TypeConverters(KairoConverters::class)
abstract class KairoDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun doseEventDao(): DoseEventDao
}
