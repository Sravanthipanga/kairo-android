package com.sravanthi.kairo.core.data.repository

import kotlinx.coroutines.flow.Flow

/** App-wide user settings. */
interface UserPreferencesRepository {
    /** Whether opening Kairo requires fingerprint, face or the device screen lock. */
    val appLockEnabled: Flow<Boolean>

    suspend fun setAppLockEnabled(enabled: Boolean)
}
