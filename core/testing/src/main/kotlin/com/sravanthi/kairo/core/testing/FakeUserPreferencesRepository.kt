package com.sravanthi.kairo.core.testing

import com.sravanthi.kairo.core.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferencesRepository(appLockEnabled: Boolean = false) : UserPreferencesRepository {
    override val appLockEnabled = MutableStateFlow(appLockEnabled)

    override suspend fun setAppLockEnabled(enabled: Boolean) {
        appLockEnabled.value = enabled
    }
}
