package com.sravanthi.kairo.core.testing

import com.sravanthi.kairo.core.data.repository.DoseRepository
import com.sravanthi.kairo.core.model.DoseEvent
import com.sravanthi.kairo.core.model.DoseStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant

/** In-memory [DoseRepository] keeping one event per (medication, scheduled time) slot. */
class FakeDoseRepository : DoseRepository {

    private val events = MutableStateFlow<List<DoseEvent>>(emptyList())
    private var nextId = 1L

    val current: List<DoseEvent> get() = events.value

    override fun observeDoses(from: Instant, to: Instant): Flow<List<DoseEvent>> =
        events.map { list -> list.filter { !it.scheduledAt.isBefore(from) && it.scheduledAt.isBefore(to) } }

    override suspend fun recordDose(medicationId: Long, scheduledAt: Instant, status: DoseStatus, at: Instant) {
        events.update { list ->
            val existing = list.firstOrNull { it.medicationId == medicationId && it.scheduledAt == scheduledAt }
            list.filterNot { it === existing } + DoseEvent(
                id = existing?.id ?: nextId++,
                medicationId = medicationId,
                scheduledAt = scheduledAt,
                takenAt = at.takeIf { status == DoseStatus.TAKEN },
                status = status,
            )
        }
    }

    override suspend fun clearDose(medicationId: Long, scheduledAt: Instant) {
        events.update { list -> list.filterNot { it.medicationId == medicationId && it.scheduledAt == scheduledAt } }
    }
}
