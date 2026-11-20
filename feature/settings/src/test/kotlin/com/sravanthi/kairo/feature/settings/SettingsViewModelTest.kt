package com.sravanthi.kairo.feature.settings

import app.cash.turbine.test
import com.sravanthi.kairo.core.testing.FakeUserPreferencesRepository
import com.sravanthi.kairo.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferences = FakeUserPreferencesRepository(appLockEnabled = false)

    @Test
    fun `shows the saved app lock setting and saves changes`() = runTest {
        val viewModel = SettingsViewModel(preferences)
        viewModel.uiState.test {
            assertEquals(SettingsUiState.Ready(appLockEnabled = false), awaitItem())

            viewModel.setAppLockEnabled(true)

            assertEquals(SettingsUiState.Ready(appLockEnabled = true), awaitItem())
            assertEquals(true, preferences.appLockEnabled.value)
        }
    }
}
