package com.sravanthi.kairo.core.data.repository

import com.sravanthi.kairo.core.database.dao.MedicationDao
import com.sravanthi.kairo.core.database.model.asEntity
import com.sravanthi.kairo.core.database.model.asExternalModel
import com.sravanthi.kairo.core.model.Medication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class OfflineFirstMedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao,
) : MedicationRepository {

    override fun observeActiveMedications(): Flow<List<Medication>> =
        medicationDao.observeActive().map { entities -> entities.map { it.asExternalModel() } }

    override fun observeMedication(id: Long): Flow<Medication?> =
        medicationDao.observeById(id).map { it?.asExternalModel() }

    override suspend fun save(medication: Medication): Long {
        val rowId = medicationDao.upsert(medication.asEntity())
        // @Upsert returns -1 when it updated an existing row.
        return if (rowId == -1L) medication.id else rowId
    }

    override suspend fun delete(id: Long) = medicationDao.deleteById(id)
}
