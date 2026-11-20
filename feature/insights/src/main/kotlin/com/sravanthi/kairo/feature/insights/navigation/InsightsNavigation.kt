package com.sravanthi.kairo.feature.insights.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sravanthi.kairo.feature.insights.InsightsRoute

const val INSIGHTS_ROUTE = "insights"

fun NavGraphBuilder.insightsScreen() {
    composable(route = INSIGHTS_ROUTE) { InsightsRoute() }
}
