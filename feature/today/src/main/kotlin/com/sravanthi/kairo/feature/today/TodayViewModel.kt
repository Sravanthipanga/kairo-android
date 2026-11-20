package com.sravanthi.kairo.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sravanthi.kairo.core.data.repository.DoseRepository
import com.sravanthi.kairo.core.data.repository.MedicationRepository
import com.sravanthi.kairo.core.model.DayProgress
import com.sravanthi.kairo.core.model.DoseStatus
import com.sravanthi.kairo.core.model.ScheduledDose
import com.sravanthi.kairo.core.model.buildDaySchedule
import com.sravanthi.kairo.core.model.progress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    medicationRepository: MedicationRepository,
    private val doseRepository: DoseRepository,
    private val clock: Clock,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(clock)

    val uiState: StateFlow<TodayUiState> = combine(
        medicationRepository.observeActiveMedications(),
        doseRepository.observeDoses(
            from = today.atStartOfDay(clock.zone).toInstant(),
            to = today.plusDays(1).atStartOfDay(clock.zone).toInstant(),
        ),
    ) { medications, events ->
        if (medications.isEmpty()) {
            TodayUiState.NoMedications
        } else {
            val doses = buildDaySchedule(medications, events, today, clock.zone)
            TodayUiState.Success(
                date = today,
                doses = doses.map { TodayDose(it, isOverdue = it.isOverdueAt(clock.instant())) },
                progress = doses.progress(),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayUiState.Loading,
    )

    fun markTaken(dose: ScheduledDose) = record(dose, DoseStatus.TAKEN)

    fun markSkipped(dose: ScheduledDose) = record(dose, DoseStatus.SKIPPED)

    fun undo(dose: ScheduledDose) {
        viewModelScope.launch { doseRepository.clearDose(dose.medication.id, dose.scheduledAt) }
    }

    private fun record(dose: ScheduledDose, status: DoseStatus) {
        viewModelScope.launch {
            doseRepository.recordDose(dose.medication.id, dose.scheduledAt, status, at = clock.instant())
        }
    }
}

private fun ScheduledDose.isOverdueAt(now: Instant) = status == DoseStatus.PENDING && scheduledAt.isBefore(now)

sealed interface TodayUiState {
    data object Loading : TodayUiState
    data object NoMedications : TodayUiState
    data class Success(
        val date: LocalDate,
        val doses: List<TodayDose>,
        val progress: DayProgress,
    ) : TodayUiState
}

data class TodayDose(
    val dose: ScheduledDose,
    val isOverdue: Boolean,
)
