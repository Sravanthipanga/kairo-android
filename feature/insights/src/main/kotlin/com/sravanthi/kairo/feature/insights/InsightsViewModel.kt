package com.sravanthi.kairo.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sravanthi.kairo.core.data.repository.DoseRepository
import com.sravanthi.kairo.core.data.repository.MedicationRepository
import com.sravanthi.kairo.core.health.HealthAvailability
import com.sravanthi.kairo.core.health.HealthDataRepository
import com.sravanthi.kairo.core.model.DailyActivity
import com.sravanthi.kairo.core.model.WeeklyInsights
import com.sravanthi.kairo.core.model.buildWeeklyInsights
import com.sravanthi.kairo.core.model.lastDays
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    medicationRepository: MedicationRepository,
    doseRepository: DoseRepository,
    private val health: HealthDataRepository,
    private val clock: Clock,
) : ViewModel() {

    private val days: List<LocalDate> = lastDays(LocalDate.now(clock), count = 7)
    private val healthStatus = MutableStateFlow(HealthStatus.Checking)
    private val activity = MutableStateFlow<Map<LocalDate, DailyActivity>>(emptyMap())

    val healthPermissions: Set<String> get() = health.permissions

    val uiState: StateFlow<InsightsUiState> = combine(
        medicationRepository.observeActiveMedications(),
        doseRepository.observeDoses(
            from = days.first().atStartOfDay(clock.zone).toInstant(),
            to = days.last().plusDays(1).atStartOfDay(clock.zone).toInstant(),
        ),
        activity,
        healthStatus,
    ) { medications, events, activity, status ->
        if (medications.isEmpty()) {
            InsightsUiState.NoMedications
        } else {
            InsightsUiState.Success(buildWeeklyInsights(medications, events, activity, days, clock.zone), status)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState.Loading)

    init {
        refreshHealth()
    }

    /** Re-checks Health Connect, e.g. on resume or after the permission screen closes. */
    fun refreshHealth() {
        viewModelScope.launch {
            val status = when (health.availability()) {
                HealthAvailability.NotSupported -> HealthStatus.NotSupported
                HealthAvailability.NeedsInstall -> HealthStatus.NeedsInstall
                HealthAvailability.Available ->
                    if (health.hasPermissions()) HealthStatus.Connected else HealthStatus.NeedsPermission
            }
            if (status == HealthStatus.Connected) activity.value = health.readDailyActivity(days, clock.zone)
            else activity.value = emptyMap()
            healthStatus.value = status
        }
    }

    fun installHealthConnectIntent() = health.installIntent()

    fun permissionContract() = health.permissionContract()
}

enum class HealthStatus { Checking, NotSupported, NeedsInstall, NeedsPermission, Connected }

sealed interface InsightsUiState {
    data object Loading : InsightsUiState
    data object NoMedications : InsightsUiState
    data class Success(val insights: WeeklyInsights, val health: HealthStatus) : InsightsUiState
}
