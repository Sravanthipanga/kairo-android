package com.sravanthi.kairo.feature.medications

import app.cash.turbine.test
import com.sravanthi.kairo.core.data.repository.MedicationRepository
import com.sravanthi.kairo.core.model.Medication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationsViewModelTest {

    private val repository = FakeMedicationRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = MedicationsViewModel(repository).apply {
        // 10:00 UTC
        clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC)
    }

    @Test
    fun `state starts as loading`() {
        assertEquals(MedicationsUiState.Loading, viewModel().uiState.value)
    }

    @Test
    fun `no medications shows the empty state`() = runTest {
        viewModel().uiState.test {
            assertEquals(MedicationsUiState.Loading, awaitItem())
            repository.emit(emptyList())
            assertEquals(MedicationsUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `medications are listed with their next dose time`() = runTest {
        val metformin = Medication(1, "Metformin", "500 mg", null, listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)))
        viewModel().uiState.test {
            assertEquals(MedicationsUiState.Loading, awaitItem())
            repository.emit(listOf(metformin))
            assertEquals(
                MedicationsUiState.Success(listOf(MedicationItem(metformin, LocalTime.of(20, 0)))),
                awaitItem(),
            )
        }
    }
}

private class FakeMedicationRepository : MedicationRepository {
    private val medications = MutableStateFlow<List<Medication>?>(null)

    fun emit(list: List<Medication>) {
        medications.value = list
    }

    override fun observeActiveMedications(): Flow<List<Medication>> =
        medications.filterNotNull()

    override fun observeMedication(id: Long): Flow<Medication?> =
        medications.map { list -> list?.firstOrNull { it.id == id } }

    override suspend fun save(medication: Medication): Long = medication.id

    override suspend fun delete(id: Long) = Unit
}
