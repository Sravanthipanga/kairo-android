package com.sravanthi.kairo.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sravanthi.kairo.core.database.model.DoseEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseEventDao {

    @Query(
        """
        SELECT * FROM dose_events
        WHERE scheduledAt >= :fromEpochMillis AND scheduledAt < :toEpochMillis
        ORDER BY scheduledAt
        """,
    )
    fun observeBetween(fromEpochMillis: Long, toEpochMillis: Long): Flow<List<DoseEventEntity>>

    @Upsert
    suspend fun upsert(event: DoseEventEntity): Long
}
