package com.sravanthi.kairo.feature.insights

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import app.cash.turbine.test
import com.sravanthi.kairo.core.health.HealthAvailability
import com.sravanthi.kairo.core.health.HealthDataRepository
import com.sravanthi.kairo.core.model.DailyActivity
import com.sravanthi.kairo.core.model.DoseStatus
import com.sravanthi.kairo.core.model.Medication
import com.sravanthi.kairo.core.testing.FakeDoseRepository
import com.sravanthi.kairo.core.testing.FakeMedicationRepository
import com.sravanthi.kairo.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

class InsightsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val medications = FakeMedicationRepository()
    private val doses = FakeDoseRepository()
    private val health = FakeHealthDataRepository()
    private val clock = Clock.fixed(Instant.parse("2026-03-10T21:00:00Z"), ZoneOffset.UTC)
    private val today = LocalDate.of(2026, 3, 10)
    private val aspirin = Medication(1, "Aspirin", "81 mg", null, listOf(LocalTime.of(8, 0)))

    private fun viewModel() = InsightsViewModel(medications, doses, health, clock)

    @Test
    fun `no medications shows the empty state`() = runTest {
        medications.setMedications(emptyList())
        viewModel().uiState.test {
            assertEquals(InsightsUiState.NoMedications, awaitItem())
        }
    }

    @Test
    fun `adherence covers the last 7 days without Health Connect`() = runTest {
        health.availability = HealthAvailability.NotSupported
        medications.setMedications(listOf(aspirin))
        val morning = today.atTime(8, 0).toInstant(ZoneOffset.UTC)
        doses.recordDose(1, morning, DoseStatus.TAKEN, at = morning)

        viewModel().uiState.test {
            val state = awaitItem() as InsightsUiState.Success
            assertEquals(HealthStatus.NotSupported, state.health)
            assertEquals(7, state.insights.days.size)
            assertEquals(today, state.insights.days.last().date)
            assertEquals(1f / 7, state.insights.adherenceRate!!, 0.0001f)
        }
    }

    @Test
    fun `asks for permission when Health Connect is available but not granted`() = runTest {
        medications.setMedications(listOf(aspirin))
        viewModel().uiState.test {
            assertEquals(HealthStatus.NeedsPermission, (awaitItem() as InsightsUiState.Success).health)
        }
    }

    @Test
    fun `once permission is granted, steps and sleep appear`() = runTest {
        medications.setMedications(listOf(aspirin))
        health.activity = mapOf(today to DailyActivity(steps = 9_000))
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(HealthStatus.NeedsPermission, (awaitItem() as InsightsUiState.Success).health)

            health.granted = true
            viewModel.refreshHealth()

            val state = expectMostRecentItem() as InsightsUiState.Success
            assertEquals(HealthStatus.Connected, state.health)
            assertEquals(9_000L, state.insights.averageSteps)
        }
    }
}

private class FakeHealthDataRepository : HealthDataRepository {
    var availability = HealthAvailability.Available
    var granted = false
    var activity: Map<LocalDate, DailyActivity> = emptyMap()

    override fun availability() = availability
    override val permissions = setOf("steps", "sleep")
    override suspend fun hasPermissions() = availability == HealthAvailability.Available && granted
    override suspend fun readDailyActivity(days: List<LocalDate>, zone: ZoneId) = if (granted) activity else emptyMap()
    override fun permissionContract(): ActivityResultContract<Set<String>, Set<String>> = error("Not used in unit tests")
    override fun installIntent(): Intent = error("Not used in unit tests")
}
