package com.sravanthi.kairo.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Today
import androidx.compose.ui.graphics.vector.ImageVector
import com.sravanthi.kairo.R
import com.sravanthi.kairo.feature.medications.navigation.MEDICATIONS_ROUTE
import com.sravanthi.kairo.feature.today.navigation.TODAY_ROUTE

/** Destinations shown in the bottom navigation bar. */
enum class TopLevelDestination(
    val route: String,
    @StringRes val label: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    TODAY(TODAY_ROUTE, R.string.nav_today, Icons.Filled.Today, Icons.Outlined.Today),
    MEDICATIONS(MEDICATIONS_ROUTE, R.string.nav_medications, Icons.Filled.Medication, Icons.Outlined.Medication),
}
