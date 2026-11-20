package com.sravanthi.kairo.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sravanthi.kairo.core.model.Medication
import java.time.LocalTime

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String,
    val instructions: String?,
    val reminderTimes: List<LocalTime>,
    val isActive: Boolean,
)

fun MedicationEntity.asExternalModel() = Medication(
    id = id,
    name = name,
    dosage = dosage,
    instructions = instructions,
    reminderTimes = reminderTimes.sorted(),
    isActive = isActive,
)

fun Medication.asEntity() = MedicationEntity(
    id = id,
    name = name.trim(),
    dosage = dosage.trim(),
    instructions = instructions?.trim()?.ifEmpty { null },
    reminderTimes = reminderTimes.distinct().sorted(),
    isActive = isActive,
)
