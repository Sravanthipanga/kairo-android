package com.sravanthi.kairo.feature.medications.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sravanthi.kairo.core.data.repository.MedicationRepository
import com.sravanthi.kairo.core.model.Medication
import com.sravanthi.kairo.feature.medications.navigation.MEDICATION_ID_ARG
import com.sravanthi.kairo.feature.medications.navigation.NEW_MEDICATION_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class MedicationEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MedicationRepository,
) : ViewModel() {

    private val medicationId: Long =
        savedStateHandle.get<Long>(MEDICATION_ID_ARG)?.takeIf { it != NEW_MEDICATION_ID } ?: 0L

    private val _uiState = MutableStateFlow(
        MedicationEditorUiState(isNew = medicationId == 0L, isLoading = medicationId != 0L),
    )
    val uiState: StateFlow<MedicationEditorUiState> = _uiState.asStateFlow()

    init {
        if (medicationId != 0L) load()
    }

    private fun load() = viewModelScope.launch {
        val medication = repository.observeMedication(medicationId).first()
        _uiState.update {
            if (medication == null) {
                // It was deleted elsewhere; close the editor.
                it.copy(isLoading = false, isFinished = true)
            } else {
                it.copy(
                    isLoading = false,
                    name = medication.name,
                    dosage = medication.dosage,
                    instructions = medication.instructions.orEmpty(),
                    reminderTimes = medication.reminderTimes.sorted(),
                )
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = false) }

    fun onDosageChange(value: String) = _uiState.update { it.copy(dosage = value, dosageError = false) }

    fun onInstructionsChange(value: String) = _uiState.update { it.copy(instructions = value) }

    fun addReminderTime(time: LocalTime) = _uiState.update {
        it.copy(reminderTimes = (it.reminderTimes + time.withSecond(0).withNano(0)).distinct().sorted(), timesError = false)
    }

    fun removeReminderTime(time: LocalTime) = _uiState.update { it.copy(reminderTimes = it.reminderTimes - time) }

    fun save() {
        val state = _uiState.value
        val validated = state.copy(
            nameError = state.name.isBlank(),
            dosageError = state.dosage.isBlank(),
            timesError = state.reminderTimes.isEmpty(),
        )
        if (validated.hasErrors) {
            _uiState.value = validated
            return
        }
        _uiState.value = validated.copy(isSaving = true)
        viewModelScope.launch {
            repository.save(
                Medication(
                    id = medicationId,
                    name = state.name,
                    dosage = state.dosage,
                    instructions = state.instructions,
                    reminderTimes = state.reminderTimes,
                ),
            )
            _uiState.update { it.copy(isSaving = false, isFinished = true) }
        }
    }

    fun delete() {
        if (medicationId == 0L) return
        viewModelScope.launch {
            repository.delete(medicationId)
            _uiState.update { it.copy(isFinished = true) }
        }
    }
}

data class MedicationEditorUiState(
    val isNew: Boolean,
    val isLoading: Boolean = false,
    val name: String = "",
    val dosage: String = "",
    val instructions: String = "",
    val reminderTimes: List<LocalTime> = emptyList(),
    val nameError: Boolean = false,
    val dosageError: Boolean = false,
    val timesError: Boolean = false,
    val isSaving: Boolean = false,
    /** True once the medication was saved or deleted and the editor should close. */
    val isFinished: Boolean = false,
) {
    val hasErrors: Boolean get() = nameError || dosageError || timesError
}
