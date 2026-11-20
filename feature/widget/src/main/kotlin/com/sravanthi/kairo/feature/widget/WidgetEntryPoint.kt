package com.sravanthi.kairo.feature.widget

import com.sravanthi.kairo.core.reminders.ReminderActionHandler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Glance widgets and actions are created by the system, so they reach Hilt objects through this. */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface WidgetEntryPoint {
    fun dataSource(): WidgetDataSource
    fun reminderActionHandler(): ReminderActionHandler
}
