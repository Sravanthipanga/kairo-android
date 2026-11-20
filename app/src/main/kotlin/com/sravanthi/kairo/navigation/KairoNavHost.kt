package com.sravanthi.kairo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.sravanthi.kairo.feature.medications.navigation.medicationEditorScreen
import com.sravanthi.kairo.feature.medications.navigation.medicationsScreen
import com.sravanthi.kairo.feature.medications.navigation.navigateToMedicationEditor
import com.sravanthi.kairo.feature.today.navigation.TODAY_ROUTE
import com.sravanthi.kairo.feature.today.navigation.todayScreen

@Composable
fun KairoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TODAY_ROUTE,
        modifier = modifier,
    ) {
        todayScreen(onAddMedication = { navController.navigateToMedicationEditor() })
        medicationsScreen(
            onAddMedication = { navController.navigateToMedicationEditor() },
            onMedicationClick = { id -> navController.navigateToMedicationEditor(id) },
        )
        medicationEditorScreen(onDone = { navController.popBackStack() })
    }
}
