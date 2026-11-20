package com.sravanthi.kairo.core.data.repository

import com.sravanthi.kairo.core.model.Medication
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for medications.
 *
 * The local database is authoritative so the app works fully offline.
 * A sync layer will push changes to the backend in a later milestone.
 */
interface MedicationRepository {
    fun observeActiveMedications(): Flow<List<Medication>>
    fun observeMedication(id: Long): Flow<Medication?>
    suspend fun save(medication: Medication): Long
    suspend fun delete(id: Long)
}
