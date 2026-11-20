package com.sravanthi.kairo.feature.medications.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sravanthi.kairo.feature.medications.MedicationsRoute
import com.sravanthi.kairo.feature.medications.editor.MedicationEditorRoute

const val MEDICATIONS_ROUTE = "medications"

internal const val MEDICATION_ID_ARG = "medicationId"
internal const val NEW_MEDICATION_ID = -1L
private const val MEDICATION_EDITOR_ROUTE = "medication_editor"

/** Opens the editor for [medicationId], or for a new medication when it is null. */
fun NavController.navigateToMedicationEditor(medicationId: Long? = null) =
    navigate("$MEDICATION_EDITOR_ROUTE?$MEDICATION_ID_ARG=${medicationId ?: NEW_MEDICATION_ID}")

fun NavGraphBuilder.medicationsScreen(
    onAddMedication: () -> Unit,
    onMedicationClick: (Long) -> Unit,
) {
    composable(route = MEDICATIONS_ROUTE) {
        MedicationsRoute(onAddMedication = onAddMedication, onMedicationClick = onMedicationClick)
    }
}

fun NavGraphBuilder.medicationEditorScreen(onDone: () -> Unit) {
    composable(
        route = "$MEDICATION_EDITOR_ROUTE?$MEDICATION_ID_ARG={$MEDICATION_ID_ARG}",
        arguments = listOf(
            navArgument(MEDICATION_ID_ARG) {
                type = NavType.LongType
                defaultValue = NEW_MEDICATION_ID
            },
        ),
    ) {
        MedicationEditorRoute(onDone = onDone)
    }
}
