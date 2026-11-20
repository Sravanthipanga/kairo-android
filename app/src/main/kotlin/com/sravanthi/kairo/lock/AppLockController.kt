package com.sravanthi.kairo.lock

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.sravanthi.kairo.core.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

/** Connects [AppLockStateMachine] to the saved setting and to the app moving to and from the background. */
@Singleton
class AppLockController @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val clock: Clock,
) : DefaultLifecycleObserver {

    private val machine = AppLockStateMachine()
    private val _state = MutableStateFlow(machine.state)
    val state: StateFlow<LockState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var started = false

    fun start() {
        if (started) return
        started = true
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        scope.launch {
            preferences.appLockEnabled.collect { enabled -> update { onSettingChanged(enabled) } }
        }
    }

    override fun onStop(owner: LifecycleOwner) = update { onBackgrounded(clock.instant()) }

    override fun onStart(owner: LifecycleOwner) = update { onForegrounded(clock.instant()) }

    fun onAuthenticated() = update { onAuthenticated() }

    fun onAuthenticationUnavailable() {
        update { onAuthenticationUnavailable() }
        // Keep the saved setting truthful: the lock cannot work without a device screen lock.
        scope.launch { preferences.setAppLockEnabled(false) }
    }

    private inline fun update(event: AppLockStateMachine.() -> Unit) {
        machine.event()
        _state.value = machine.state
    }
}
