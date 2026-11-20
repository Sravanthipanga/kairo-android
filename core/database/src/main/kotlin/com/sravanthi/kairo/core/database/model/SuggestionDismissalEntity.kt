package com.sravanthi.kairo.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import com.sravanthi.kairo.core.model.SuggestionDismissal
import java.time.Instant
import java.time.LocalTime

/** One "not now" per reminder slot; dismissing again just refreshes the time. */
@Entity(
    tableName = "suggestion_dismissals",
    primaryKeys = ["medicationId", "reminderTime"],
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class SuggestionDismissalEntity(
    val medicationId: Long,
    val reminderTime: LocalTime,
    val dismissedAt: Instant,
)

fun SuggestionDismissalEntity.asExternalModel() = SuggestionDismissal(medicationId, reminderTime, dismissedAt)
