package com.sravanthi.kairo.feature.today

import app.cash.turbine.test
import com.sravanthi.kairo.core.model.DayProgress
import com.sravanthi.kairo.core.model.DoseStatus
import com.sravanthi.kairo.core.model.Medication
import com.sravanthi.kairo.core.testing.FakeDoseRepository
import com.sravanthi.kairo.core.testing.FakeMedicationRepository
import com.sravanthi.kairo.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset

class TodayViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val medications = FakeMedicationRepository()
    private val doses = FakeDoseRepository()

    // 2026-03-10 at 12:00 UTC
    private val now = Instant.parse("2026-03-10T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    private val metformin = Medication(1, "Metformin", "500 mg", null, listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)))

    private fun viewModel() = TodayViewModel(medications, doses, clock)

    @Test
    fun `no medications shows the add prompt`() = runTest {
        viewModel().uiState.test {
            assertEquals(TodayUiState.Loading, awaitItem())
            medications.setMedications(emptyList())
            assertEquals(TodayUiState.NoMedications, awaitItem())
        }
    }

    @Test
    fun `today's doses are listed and past pending doses are overdue`() = runTest {
        medications.setMedications(listOf(metformin))
        viewModel().uiState.test {
            val state = awaitItem() as TodayUiState.Success
            assertEquals(listOf(true, false), state.doses.map { it.isOverdue })
            assertEquals(DayProgress(taken = 0, total = 2), state.progress)
        }
    }

    @Test
    fun `taking a dose records it with the current time and updates progress`() = runTest {
        medications.setMedications(listOf(metformin))
        val viewModel = viewModel()
        viewModel.uiState.test {
            val morning = (awaitItem() as TodayUiState.Success).doses.first().dose
            viewModel.markTaken(morning)

            val updated = awaitItem() as TodayUiState.Success
            assertEquals(DoseStatus.TAKEN, updated.doses.first().dose.status)
            assertEquals(false, updated.doses.first().isOverdue)
            assertEquals(DayProgress(taken = 1, total = 2), updated.progress)
            assertEquals(now, doses.current.single().takenAt)
        }
    }

    @Test
    fun `skipping then undoing returns the dose to pending`() = runTest {
        medications.setMedications(listOf(metformin))
        val viewModel = viewModel()
        viewModel.uiState.test {
            val evening = (awaitItem() as TodayUiState.Success).doses.last().dose
            viewModel.markSkipped(evening)
            assertEquals(DoseStatus.SKIPPED, (awaitItem() as TodayUiState.Success).doses.last().dose.status)
            assertNull(doses.current.single().takenAt)

            viewModel.undo(evening)
            assertEquals(DoseStatus.PENDING, (awaitItem() as TodayUiState.Success).doses.last().dose.status)
        }
    }
}
