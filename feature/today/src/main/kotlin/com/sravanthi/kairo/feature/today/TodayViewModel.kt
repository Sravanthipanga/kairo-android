package com.sravanthi.kairo.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sravanthi.kairo.core.data.repository.DoseRepository
import com.sravanthi.kairo.core.data.repository.MedicationRepository
import com.sravanthi.kairo.core.data.repository.SuggestionDismissalRepository
import com.sravanthi.kairo.core.model.AdaptiveTimingPolicy
import com.sravanthi.kairo.core.model.DayProgress
import com.sravanthi.kairo.core.model.DoseStatus
import com.sravanthi.kairo.core.model.ScheduledDose
import com.sravanthi.kairo.core.model.TimingSuggestion
import com.sravanthi.kairo.core.model.applying
import com.sravanthi.kairo.core.model.buildDaySchedule
import com.sravanthi.kairo.core.model.buildTimingSuggestions
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
    private val medicationRepository: MedicationRepository,
    private val doseRepository: DoseRepository,
    private val dismissalRepository: SuggestionDismissalRepository,
    private val clock: Clock,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(clock)
    private val policy = AdaptiveTimingPolicy()

    val uiState: StateFlow<TodayUiState> = combine(
        medicationRepository.observeActiveMedications(),
        // Today's doses plus the recent history adaptive timing learns from, in one query.
        doseRepository.observeDoses(
            from = today.atStartOfDay(clock.zone).toInstant().minus(policy.window),
            to = today.plusDays(1).atStartOfDay(clock.zone).toInstant(),
        ),
        dismissalRepository.observeDismissals(),
    ) { medications, events, dismissals ->
        if (medications.isEmpty()) {
            TodayUiState.NoMedications
        } else {
            val now = clock.instant()
            val doses = buildDaySchedule(medications, events, today, clock.zone)
            TodayUiState.Success(
                date = today,
                doses = doses.map { TodayDose(it, isOverdue = it.isOverdueAt(now)) },
                progress = doses.progress(),
                suggestions = buildTimingSuggestions(medications, events, dismissals, now, clock.zone, policy),
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

    /** Moves the reminder. Alarms follow automatically because they sync with the medication list. */
    fun acceptSuggestion(suggestion: TimingSuggestion) {
        viewModelScope.launch { medicationRepository.save(suggestion.medication.applying(suggestion)) }
    }

    fun dismissSuggestion(suggestion: TimingSuggestion) {
        viewModelScope.launch {
            dismissalRepository.dismiss(suggestion.medication.id, suggestion.currentTime, at = clock.instant())
        }
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
        val suggestions: List<TimingSuggestion> = emptyList(),
    ) : TodayUiState
}

data class TodayDose(
    val dose: ScheduledDose,
    val isOverdue: Boolean,
)
