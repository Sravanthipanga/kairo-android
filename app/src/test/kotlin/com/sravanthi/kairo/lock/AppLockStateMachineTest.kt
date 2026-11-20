package com.sravanthi.kairo.lock

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.Instant

class AppLockStateMachineTest {

    private val t0 = Instant.parse("2026-03-10T08:00:00Z")
    private fun machine() = AppLockStateMachine(gracePeriod = Duration.ofSeconds(30))

    @Test
    fun `nothing is shown until the setting loads`() {
        assertEquals(LockState.Checking, machine().state)
    }

    @Test
    fun `launch is locked when app lock is on and open when it is off`() {
        assertEquals(LockState.Locked, machine().apply { onSettingChanged(true) }.state)
        assertEquals(LockState.Unlocked, machine().apply { onSettingChanged(false) }.state)
    }

    @Test
    fun `authenticating unlocks`() {
        val machine = machine().apply { onSettingChanged(true) }
        machine.onAuthenticated()
        assertEquals(LockState.Unlocked, machine.state)
    }

    @Test
    fun `a quick switch to another app does not re-lock`() {
        val machine = machine().apply { onSettingChanged(true); onAuthenticated() }
        machine.onBackgrounded(t0)
        machine.onForegrounded(t0.plusSeconds(10))
        assertEquals(LockState.Unlocked, machine.state)
    }

    @Test
    fun `returning after the grace period re-locks`() {
        val machine = machine().apply { onSettingChanged(true); onAuthenticated() }
        machine.onBackgrounded(t0)
        machine.onForegrounded(t0.plusSeconds(30))
        assertEquals(LockState.Locked, machine.state)
    }

    @Test
    fun `turning app lock on keeps the current session open, turning it off unlocks`() {
        val machine = machine().apply { onSettingChanged(false) }
        machine.onSettingChanged(true)
        assertEquals(LockState.Unlocked, machine.state)

        machine.onBackgrounded(t0)
        machine.onForegrounded(t0.plusSeconds(60))
        assertEquals(LockState.Locked, machine.state)

        machine.onSettingChanged(false)
        assertEquals(LockState.Unlocked, machine.state)
    }

    @Test
    fun `with app lock off, time in the background never locks`() {
        val machine = machine().apply { onSettingChanged(false) }
        machine.onBackgrounded(t0)
        machine.onForegrounded(t0.plusSeconds(3600))
        assertEquals(LockState.Unlocked, machine.state)
    }

    @Test
    fun `if the device has no screen lock any more, the app opens instead of trapping the user`() {
        val machine = machine().apply { onSettingChanged(true) }
        machine.onAuthenticationUnavailable()
        assertEquals(LockState.Unlocked, machine.state)
    }
}
