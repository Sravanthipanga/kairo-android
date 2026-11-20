package com.sravanthi.kairo.lock

import java.time.Duration
import java.time.Instant

enum class LockState {
    /** The setting has not loaded yet; show nothing so content never flashes before the lock. */
    Checking,
    Locked,
    Unlocked,
}

/**
 * Decides when Kairo is locked. Pure logic with no Android types, so every rule is unit-tested.
 *
 * - Locked on launch when app lock is on.
 * - Re-locked when the app returns after being in the background for at least [gracePeriod].
 *   A short grace period avoids re-prompting after a quick glance at another app.
 * - Turning app lock off unlocks immediately. Turning it on keeps the current session open.
 */
class AppLockStateMachine(private val gracePeriod: Duration = Duration.ofSeconds(30)) {

    var state: LockState = LockState.Checking
        private set

    private var enabled: Boolean? = null
    private var backgroundedAt: Instant? = null

    fun onSettingChanged(enabled: Boolean) {
        val firstValue = this.enabled == null
        this.enabled = enabled
        state = when {
            !enabled -> LockState.Unlocked
            firstValue -> LockState.Locked
            else -> state.takeIf { it != LockState.Checking } ?: LockState.Locked
        }
    }

    fun onBackgrounded(now: Instant) {
        backgroundedAt = now
    }

    fun onForegrounded(now: Instant) {
        val since = backgroundedAt ?: return
        backgroundedAt = null
        if (enabled == true && Duration.between(since, now) >= gracePeriod) state = LockState.Locked
    }

    fun onAuthenticated() {
        if (state == LockState.Locked) state = LockState.Unlocked
    }

    /** The device no longer has any screen lock, so the app cannot be unlocked: open it rather than trap the user. */
    fun onAuthenticationUnavailable() {
        state = LockState.Unlocked
    }
}
