package com.sravanthi.kairo.core.data.repository

import com.sravanthi.kairo.core.model.DoseEvent
import com.sravanthi.kairo.core.model.DoseStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/** Records what the user did with each scheduled dose. This history drives adaptive reminders. */
interface DoseRepository {
    /** Dose events scheduled in `[from, to)`. */
    fun observeDoses(from: Instant, to: Instant): Flow<List<DoseEvent>>

    /** Records [status] for one dose slot, replacing any earlier record for it. */
    suspend fun recordDose(medicationId: Long, scheduledAt: Instant, status: DoseStatus, at: Instant)

    /** Clears the record for one dose slot, returning it to pending (undo). */
    suspend fun clearDose(medicationId: Long, scheduledAt: Instant)
}
