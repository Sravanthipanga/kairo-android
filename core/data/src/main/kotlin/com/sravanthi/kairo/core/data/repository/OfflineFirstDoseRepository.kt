package com.sravanthi.kairo.core.data.repository

import com.sravanthi.kairo.core.database.dao.DoseEventDao
import com.sravanthi.kairo.core.database.model.DoseEventEntity
import com.sravanthi.kairo.core.database.model.asExternalModel
import com.sravanthi.kairo.core.model.DoseEvent
import com.sravanthi.kairo.core.model.DoseStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

internal class OfflineFirstDoseRepository @Inject constructor(
    private val doseEventDao: DoseEventDao,
) : DoseRepository {

    override fun observeDoses(from: Instant, to: Instant): Flow<List<DoseEvent>> =
        doseEventDao.observeBetween(from, to).map { events -> events.map { it.asExternalModel() } }

    override suspend fun recordDose(medicationId: Long, scheduledAt: Instant, status: DoseStatus, at: Instant) {
        doseEventDao.record(
            DoseEventEntity(
                medicationId = medicationId,
                scheduledAt = scheduledAt,
                // Only a taken dose has a taken time; it is what adaptive timing learns from.
                takenAt = at.takeIf { status == DoseStatus.TAKEN },
                status = status,
            ),
        )
    }

    override suspend fun clearDose(medicationId: Long, scheduledAt: Instant) =
        doseEventDao.deleteBySlot(medicationId, scheduledAt)
}
