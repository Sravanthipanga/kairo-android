package com.sravanthi.kairo.feature.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sravanthi.kairo.core.data.repository.MedicationRepository
import com.sravanthi.kairo.core.model.Medication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class MedicationsViewModel @Inject constructor(
    repository: MedicationRepository,
    private val clock: Clock,
) : ViewModel() {

    val uiState: StateFlow<MedicationsUiState> = repository.observeActiveMedications()
        .map { medications ->
            if (medications.isEmpty()) {
                MedicationsUiState.Empty
            } else {
                val now = LocalTime.now(clock)
                MedicationsUiState.Success(
                    medications.map { MedicationItem(it, it.nextReminderAfter(now)) },
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MedicationsUiState.Loading,
        )
}

sealed interface MedicationsUiState {
    data object Loading : MedicationsUiState
    data object Empty : MedicationsUiState
    data class Success(val items: List<MedicationItem>) : MedicationsUiState
}

data class MedicationItem(
    val medication: Medication,
    val nextDose: LocalTime?,
)
