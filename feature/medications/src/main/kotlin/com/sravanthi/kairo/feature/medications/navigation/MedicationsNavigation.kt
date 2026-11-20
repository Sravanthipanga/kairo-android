package com.sravanthi.kairo.feature.medications.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sravanthi.kairo.feature.medications.MedicationsRoute

const val MEDICATIONS_ROUTE = "medications"

fun NavGraphBuilder.medicationsScreen() {
    composable(route = MEDICATIONS_ROUTE) { MedicationsRoute() }
}
