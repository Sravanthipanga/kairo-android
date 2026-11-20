package com.sravanthi.kairo

import android.app.Application
import com.sravanthi.kairo.core.reminders.ReminderSync
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class KairoApplication : Application() {

    @Inject
    lateinit var reminderSync: ReminderSync

    override fun onCreate() {
        super.onCreate()
        // Keeps system alarms in step with the medication list for as long as the process lives.
        reminderSync.start()
    }
}
