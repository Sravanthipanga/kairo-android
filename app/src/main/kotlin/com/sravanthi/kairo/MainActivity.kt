package com.sravanthi.kairo

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sravanthi.kairo.core.data.repository.UserPreferencesRepository
import com.sravanthi.kairo.core.designsystem.theme.KairoTheme
import com.sravanthi.kairo.lock.AppLockController
import com.sravanthi.kairo.lock.BiometricUnlock
import com.sravanthi.kairo.lock.LockScreen
import com.sravanthi.kairo.lock.LockState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

// FragmentActivity (a ComponentActivity) is required by BiometricPrompt.
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var appLock: AppLockController

    @Inject
    lateinit var preferences: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        keepContentPrivateWhileLockIsOn()

        setContent {
            val lockState by appLock.state.collectAsStateWithLifecycle()
            LaunchedEffect(lockState) {
                if (lockState == LockState.Locked) unlock()
            }
            KairoTheme {
                when (lockState) {
                    LockState.Checking -> Surface(Modifier.fillMaxSize()) {}
                    LockState.Locked -> LockScreen(onUnlock = ::unlock)
                    LockState.Unlocked -> Box(Modifier.fillMaxSize()) { KairoApp() }
                }
            }
        }
    }

    private fun unlock() {
        if (BiometricUnlock.isAvailable(this)) {
            BiometricUnlock.prompt(this, onSuccess = appLock::onAuthenticated)
        } else {
            appLock.onAuthenticationUnavailable()
        }
    }

    /**
     * With app lock on, hides Kairo's screen from the recent-apps preview and from screenshots,
     * so medication names are not visible without unlocking.
     */
    private fun keepContentPrivateWhileLockIsOn() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                preferences.appLockEnabled.collect { enabled ->
                    if (enabled) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }
            }
        }
    }
}
