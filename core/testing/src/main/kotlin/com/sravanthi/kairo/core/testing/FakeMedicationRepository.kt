package com.sravanthi.kairo.core.testing

import com.sravanthi.kairo.core.data.repository.MedicationRepository
import com.sravanthi.kairo.core.model.Medication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [MedicationRepository]. Emits nothing until [setMedications] or [save] is called. */
class FakeMedicationRepository : MedicationRepository {

    private val medications = MutableStateFlow<List<Medication>?>(null)
    private var nextId = 1L

    val current: List<Medication> get() = medications.value.orEmpty()

    fun setMedications(list: List<Medication>) {
        medications.value = list
        nextId = (list.maxOfOrNull { it.id } ?: 0) + 1
    }

    override fun observeActiveMedications(): Flow<List<Medication>> =
        medications.filterNotNull().map { list -> list.filter { it.isActive }.sortedBy { it.name.lowercase() } }

    override fun observeMedication(id: Long): Flow<Medication?> =
        medications.filterNotNull().map { list -> list.firstOrNull { it.id == id } }

    override suspend fun save(medication: Medication): Long {
        val id = if (medication.id == 0L) nextId++ else medication.id
        medications.update { list ->
            list.orEmpty().filterNot { it.id == id } + medication.copy(id = id)
        }
        return id
    }

    override suspend fun delete(id: Long) {
        medications.update { list -> list.orEmpty().filterNot { it.id == id } }
    }
}
