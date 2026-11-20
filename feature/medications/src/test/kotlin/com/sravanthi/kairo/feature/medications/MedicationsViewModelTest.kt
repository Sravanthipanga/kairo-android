package com.sravanthi.kairo.feature.medications

import app.cash.turbine.test
import com.sravanthi.kairo.core.model.Medication
import com.sravanthi.kairo.core.testing.FakeMedicationRepository
import com.sravanthi.kairo.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset

class MedicationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMedicationRepository()

    // 10:00 UTC
    private val clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC)

    private fun viewModel() = MedicationsViewModel(repository, clock)

    @Test
    fun `state starts as loading`() {
        assertEquals(MedicationsUiState.Loading, viewModel().uiState.value)
    }

    @Test
    fun `no medications shows the empty state`() = runTest {
        viewModel().uiState.test {
            assertEquals(MedicationsUiState.Loading, awaitItem())
            repository.setMedications(emptyList())
            assertEquals(MedicationsUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `medications are listed with their next dose time`() = runTest {
        val metformin = Medication(1, "Metformin", "500 mg", null, listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)))
        viewModel().uiState.test {
            assertEquals(MedicationsUiState.Loading, awaitItem())
            repository.setMedications(listOf(metformin))
            assertEquals(
                MedicationsUiState.Success(listOf(MedicationItem(metformin, LocalTime.of(20, 0)))),
                awaitItem(),
            )
        }
    }
}
