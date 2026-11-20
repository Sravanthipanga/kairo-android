package com.sravanthi.kairo.feature.medications.editor

import androidx.lifecycle.SavedStateHandle
import com.sravanthi.kairo.core.model.Medication
import com.sravanthi.kairo.core.testing.FakeMedicationRepository
import com.sravanthi.kairo.core.testing.MainDispatcherRule
import com.sravanthi.kairo.feature.medications.navigation.MEDICATION_ID_ARG
import com.sravanthi.kairo.feature.medications.navigation.NEW_MEDICATION_ID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalTime

class MedicationEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMedicationRepository()

    private val metformin = Medication(7, "Metformin", "500 mg", "With food", listOf(LocalTime.of(20, 0), LocalTime.of(8, 0)))

    private fun viewModel(id: Long = NEW_MEDICATION_ID) =
        MedicationEditorViewModel(SavedStateHandle(mapOf(MEDICATION_ID_ARG to id)), repository)

    @Test
    fun `new medication starts empty`() {
        val state = viewModel().uiState.value
        assertTrue(state.isNew)
        assertFalse(state.isLoading)
        assertEquals("", state.name)
    }

    @Test
    fun `editing loads the existing medication with sorted times`() {
        repository.setMedications(listOf(metformin))
        val state = viewModel(id = 7).uiState.value

        assertFalse(state.isNew)
        assertFalse(state.isLoading)
        assertEquals("Metformin", state.name)
        assertEquals("With food", state.instructions)
        assertEquals(listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)), state.reminderTimes)
    }

    @Test
    fun `editing a medication that no longer exists closes the editor`() {
        repository.setMedications(emptyList())
        assertTrue(viewModel(id = 99).uiState.value.isFinished)
    }

    @Test
    fun `saving with missing fields shows every error and saves nothing`() {
        val viewModel = viewModel()
        viewModel.save()

        val state = viewModel.uiState.value
        assertTrue(state.nameError && state.dosageError && state.timesError)
        assertFalse(state.isFinished)
        assertTrue(repository.current.isEmpty())
    }

    @Test
    fun `typing clears that field's error`() {
        val viewModel = viewModel()
        viewModel.save()
        viewModel.onNameChange("Aspirin")
        viewModel.addReminderTime(LocalTime.of(9, 0))

        val state = viewModel.uiState.value
        assertFalse(state.nameError)
        assertFalse(state.timesError)
        assertTrue(state.dosageError)
    }

    @Test
    fun `reminder times are deduplicated, sorted and truncated to the minute`() {
        val viewModel = viewModel()
        viewModel.addReminderTime(LocalTime.of(20, 0, 42))
        viewModel.addReminderTime(LocalTime.of(8, 0))
        viewModel.addReminderTime(LocalTime.of(20, 0))
        viewModel.removeReminderTime(LocalTime.of(8, 0))
        viewModel.addReminderTime(LocalTime.of(7, 30))

        assertEquals(listOf(LocalTime.of(7, 30), LocalTime.of(20, 0)), viewModel.uiState.value.reminderTimes)
    }

    @Test
    fun `a valid new medication is saved and the editor closes`() {
        val viewModel = viewModel()
        viewModel.onNameChange("Aspirin")
        viewModel.onDosageChange("81 mg")
        viewModel.addReminderTime(LocalTime.of(9, 0))
        viewModel.save()

        assertTrue(viewModel.uiState.value.isFinished)
        val saved = repository.current.single()
        assertEquals("Aspirin", saved.name)
        assertEquals(listOf(LocalTime.of(9, 0)), saved.reminderTimes)
    }

    @Test
    fun `saving an existing medication updates it in place`() {
        repository.setMedications(listOf(metformin))
        val viewModel = viewModel(id = 7)
        viewModel.onDosageChange("1000 mg")
        viewModel.save()

        val saved = repository.current.single()
        assertEquals(7, saved.id)
        assertEquals("1000 mg", saved.dosage)
    }

    @Test
    fun `deleting removes the medication and closes the editor`() {
        repository.setMedications(listOf(metformin))
        val viewModel = viewModel(id = 7)
        viewModel.delete()

        assertTrue(repository.current.isEmpty())
        assertTrue(viewModel.uiState.value.isFinished)
    }
}
