package com.sravanthi.kairo.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.userPreferences: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

internal class DataStoreUserPreferencesRepository @Inject constructor(
    @ApplicationContext context: Context,
) : UserPreferencesRepository {

    private val dataStore = context.userPreferences

    override val appLockEnabled: Flow<Boolean> =
        dataStore.data.map { it[APP_LOCK_ENABLED] ?: false }.distinctUntilChanged()

    override suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.edit { it[APP_LOCK_ENABLED] = enabled }
    }

    private companion object {
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    }
}
