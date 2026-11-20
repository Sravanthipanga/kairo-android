package com.sravanthi.kairo.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sravanthi.kairo.core.model.DoseEvent
import com.sravanthi.kairo.core.model.DoseStatus
import java.time.Instant

@Entity(
    tableName = "dose_events",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("medicationId"), Index("scheduledAt")],
)
data class DoseEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val scheduledAt: Instant,
    val takenAt: Instant?,
    val status: DoseStatus,
)

fun DoseEventEntity.asExternalModel() = DoseEvent(
    id = id,
    medicationId = medicationId,
    scheduledAt = scheduledAt,
    takenAt = takenAt,
    status = status,
)

fun DoseEvent.asEntity() = DoseEventEntity(
    id = id,
    medicationId = medicationId,
    scheduledAt = scheduledAt,
    takenAt = takenAt,
    status = status,
)
