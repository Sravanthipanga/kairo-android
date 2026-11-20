package com.sravanthi.kairo.feature.today.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sravanthi.kairo.feature.today.TodayRoute

const val TODAY_ROUTE = "today"

fun NavGraphBuilder.todayScreen(onAddMedication: () -> Unit) {
    composable(route = TODAY_ROUTE) { TodayRoute(onAddMedication = onAddMedication) }
}
