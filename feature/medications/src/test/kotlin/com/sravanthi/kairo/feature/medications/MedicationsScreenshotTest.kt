package com.sravanthi.kairo.feature.medications

import com.github.takahirom.roborazzi.captureRoboImage
import com.sravanthi.kairo.core.designsystem.theme.KairoTheme
import com.sravanthi.kairo.core.model.Medication
import com.sravanthi.kairo.core.testing.PHONE_QUALIFIERS
import com.sravanthi.kairo.core.testing.SCREENSHOT_DIR
import com.sravanthi.kairo.feature.medications.editor.MedicationEditorScreen
import com.sravanthi.kairo.feature.medications.editor.MedicationEditorUiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE_QUALIFIERS)
class MedicationsScreenshotTest {

    @Test
    fun medications() = captureRoboImage("$SCREENSHOT_DIR/medications.png") {
        KairoTheme {
            MedicationsScreen(
                MedicationsUiState.Success(
                    listOf(
                        MedicationItem(Medication(3, "Lisinopril", "10 mg", null, listOf(LocalTime.of(13, 0))), LocalTime.of(13, 0)),
                        MedicationItem(
                            Medication(1, "Metformin", "500 mg", "With food", listOf(LocalTime.of(8, 40), LocalTime.of(20, 0))),
                            LocalTime.of(20, 0),
                        ),
                        MedicationItem(Medication(2, "Vitamin D3", "1000 IU", null, listOf(LocalTime.of(9, 0))), LocalTime.of(9, 0)),
                    ),
                ),
            )
        }
    }

    @Test
    fun medicationEditor() = captureRoboImage("$SCREENSHOT_DIR/medication_editor.png") {
        KairoTheme {
            MedicationEditorScreen(
                uiState = MedicationEditorUiState(
                    isNew = false,
                    name = "Metformin",
                    dosage = "500 mg",
                    instructions = "With food",
                    reminderTimes = listOf(LocalTime.of(8, 40), LocalTime.of(20, 0)),
                ),
                onBack = {}, onNameChange = {}, onDosageChange = {}, onInstructionsChange = {},
                onAddTime = {}, onRemoveTime = {}, onSave = {}, onDelete = {},
            )
        }
    }
}
