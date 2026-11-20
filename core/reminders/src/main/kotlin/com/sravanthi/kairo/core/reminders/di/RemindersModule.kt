package com.sravanthi.kairo.core.reminders.di

import com.sravanthi.kairo.core.reminders.AlarmScheduler
import com.sravanthi.kairo.core.reminders.AndroidAlarmScheduler
import com.sravanthi.kairo.core.reminders.AndroidReminderNotifier
import com.sravanthi.kairo.core.reminders.ReminderNotifier
import com.sravanthi.kairo.core.reminders.ScheduledSlotStore
import com.sravanthi.kairo.core.reminders.SharedPreferencesSlotStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RemindersModule {
    @Binds
    abstract fun bindAlarmScheduler(scheduler: AndroidAlarmScheduler): AlarmScheduler

    @Binds
    abstract fun bindNotifier(notifier: AndroidReminderNotifier): ReminderNotifier

    @Binds
    abstract fun bindSlotStore(store: SharedPreferencesSlotStore): ScheduledSlotStore
}
