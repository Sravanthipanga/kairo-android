package com.sravanthi.kairo.feature.settings

import com.github.takahirom.roborazzi.captureRoboImage
import com.sravanthi.kairo.core.designsystem.theme.KairoTheme
import com.sravanthi.kairo.core.testing.PHONE_QUALIFIERS
import com.sravanthi.kairo.core.testing.SCREENSHOT_DIR
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE_QUALIFIERS)
class SettingsScreenshotTest {

    @Test
    fun settings() = captureRoboImage("$SCREENSHOT_DIR/settings.png") {
        KairoTheme {
            SettingsScreen(SettingsUiState.Ready(appLockEnabled = true), lockAvailable = true, onAppLockChange = {})
        }
    }
}
