package com.sravanthi.kairo.feature.widget

import com.sravanthi.kairo.core.data.repository.DoseRepository
import com.sravanthi.kairo.core.data.repository.MedicationRepository
import com.sravanthi.kairo.core.data.repository.UserPreferencesRepository
import com.sravanthi.kairo.core.model.buildDaySchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** A live [WidgetSummary] for today, built from the same repositories as the app. */
class WidgetDataSource @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val doseRepository: DoseRepository,
    private val preferences: UserPreferencesRepository,
    private val clock: Clock,
) {
    fun summary(): Flow<WidgetSummary> {
        val today = LocalDate.now(clock)
        return combine(
            medicationRepository.observeActiveMedications(),
            doseRepository.observeDoses(
                from = today.atStartOfDay(clock.zone).toInstant(),
                to = today.plusDays(1).atStartOfDay(clock.zone).toInstant(),
            ),
            preferences.appLockEnabled,
        ) { medications, events, appLockEnabled ->
            buildWidgetSummary(
                schedule = buildDaySchedule(medications, events, today, clock.zone),
                hasMedications = medications.isNotEmpty(),
                now = clock.instant(),
                appLockEnabled = appLockEnabled,
            )
        }.distinctUntilChanged()
    }
}
