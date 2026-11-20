package com.sravanthi.kairo.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sravanthi.kairo.core.database.dao.DoseEventDao
import com.sravanthi.kairo.core.database.dao.MedicationDao
import com.sravanthi.kairo.core.database.model.DoseEventEntity
import com.sravanthi.kairo.core.database.model.MedicationEntity

@Database(
    entities = [MedicationEntity::class, DoseEventEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(KairoConverters::class)
abstract class KairoDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun doseEventDao(): DoseEventDao
}
