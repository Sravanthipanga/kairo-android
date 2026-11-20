package com.sravanthi.kairo.core.reminders

import com.sravanthi.kairo.core.data.repository.MedicationRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Lets broadcast receivers reach Hilt-provided objects without field injection. */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface ReminderEntryPoint {
    fun actionHandler(): ReminderActionHandler
    fun scheduler(): ReminderScheduler
    fun medicationRepository(): MedicationRepository
}
