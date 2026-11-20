package com.sravanthi.kairo.core.reminders

import android.content.Context
import androidx.core.content.edit
import com.sravanthi.kairo.core.model.ReminderSlot
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class SharedPreferencesSlotStore @Inject constructor(
    @ApplicationContext context: Context,
) : ScheduledSlotStore {

    private val prefs = context.getSharedPreferences("kairo_reminders", Context.MODE_PRIVATE)

    override fun load(): Set<ReminderSlot> =
        prefs.getStringSet(KEY_SLOTS, emptySet()).orEmpty().mapNotNull(ReminderSlot::fromKey).toSet()

    override fun save(slots: Set<ReminderSlot>) =
        prefs.edit { putStringSet(KEY_SLOTS, slots.map { it.key }.toSet()) }

    private companion object {
        const val KEY_SLOTS = "scheduled_slots"
    }
}
