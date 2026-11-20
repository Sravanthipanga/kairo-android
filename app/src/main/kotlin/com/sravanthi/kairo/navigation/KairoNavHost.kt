package com.sravanthi.kairo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.sravanthi.kairo.feature.medications.navigation.MEDICATIONS_ROUTE
import com.sravanthi.kairo.feature.medications.navigation.medicationsScreen

@Composable
fun KairoNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = MEDICATIONS_ROUTE,
        modifier = modifier,
    ) {
        medicationsScreen()
    }
}
