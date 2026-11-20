package com.sravanthi.kairo.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.sravanthi.kairo.core.database.model.DoseEventEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface DoseEventDao {

    @Query(
        """
        SELECT * FROM dose_events
        WHERE scheduledAt >= :from AND scheduledAt < :to
        ORDER BY scheduledAt
        """,
    )
    fun observeBetween(from: Instant, to: Instant): Flow<List<DoseEventEntity>>

    @Query("SELECT * FROM dose_events WHERE medicationId = :medicationId AND scheduledAt = :scheduledAt")
    suspend fun getBySlot(medicationId: Long, scheduledAt: Instant): DoseEventEntity?

    @Upsert
    suspend fun upsert(event: DoseEventEntity): Long

    @Query("DELETE FROM dose_events WHERE medicationId = :medicationId AND scheduledAt = :scheduledAt")
    suspend fun deleteBySlot(medicationId: Long, scheduledAt: Instant)

    /** Inserts or updates the single record for this dose slot. */
    @Transaction
    suspend fun record(event: DoseEventEntity) {
        val existing = getBySlot(event.medicationId, event.scheduledAt)
        upsert(event.copy(id = existing?.id ?: 0))
    }
}
