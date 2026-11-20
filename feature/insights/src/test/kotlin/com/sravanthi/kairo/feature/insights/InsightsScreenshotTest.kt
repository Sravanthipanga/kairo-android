package com.sravanthi.kairo.feature.insights

import com.github.takahirom.roborazzi.captureRoboImage
import com.sravanthi.kairo.core.designsystem.theme.KairoTheme
import com.sravanthi.kairo.core.model.DailyActivity
import com.sravanthi.kairo.core.model.DayInsight
import com.sravanthi.kairo.core.model.DayProgress
import com.sravanthi.kairo.core.model.WeeklyInsights
import com.sravanthi.kairo.core.testing.PHONE_QUALIFIERS
import com.sravanthi.kairo.core.testing.SCREENSHOT_DIR
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Duration
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE_QUALIFIERS)
class InsightsScreenshotTest {

    @Test
    fun insights() {
        val today = LocalDate.of(2026, 3, 10)
        val taken = listOf(4, 4, 3, 4, 4, 2, 1)
        val steps = listOf(6_240L, 8_910L, 4_120L, 10_480L, 7_655L, 9_030L, 3_870L)
        val sleepMinutes = listOf(412L, 455L, 371L, 438L, 465L, 420L, 401L)
        val days = (0 until 7).map { i ->
            DayInsight(
                date = today.minusDays(6L - i),
                adherence = DayProgress(taken = taken[i], total = 4),
                activity = DailyActivity(steps = steps[i], sleep = Duration.ofMinutes(sleepMinutes[i])),
            )
        }
        captureRoboImage("$SCREENSHOT_DIR/insights.png") {
            KairoTheme {
                InsightsScreen(
                    uiState = InsightsUiState.Success(WeeklyInsights(days), HealthStatus.Connected),
                    onConnect = {},
                    onInstall = {},
                )
            }
        }
    }
}
