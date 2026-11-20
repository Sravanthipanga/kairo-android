package com.sravanthi.kairo.core.reminders

import android.content.Context
import com.sravanthi.kairo.core.data.repository.MedicationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** Started once from the Application: reschedules alarms whenever medications change. */
@Singleton
class ReminderSync @Inject constructor(
    @ApplicationContext private val context: Context,
    private val medicationRepository: MedicationRepository,
    private val scheduler: ReminderScheduler,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var started = false

    fun start() {
        if (started) return
        started = true
        AndroidReminderNotifier.ensureChannel(context)
        scope.launch {
            medicationRepository.observeActiveMedications()
                .distinctUntilChanged()
                .collect(scheduler::sync)
        }
    }
}
