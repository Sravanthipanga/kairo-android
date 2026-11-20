package com.sravanthi.kairo.feature.today

import android.Manifest
import com.github.takahirom.roborazzi.captureRoboImage
import com.sravanthi.kairo.core.designsystem.theme.KairoTheme
import com.sravanthi.kairo.core.model.DayProgress
import com.sravanthi.kairo.core.model.DoseEvent
import com.sravanthi.kairo.core.model.DoseStatus
import com.sravanthi.kairo.core.model.Medication
import com.sravanthi.kairo.core.model.ScheduledDose
import com.sravanthi.kairo.core.model.TimingSuggestion
import com.sravanthi.kairo.core.testing.PHONE_QUALIFIERS
import com.sravanthi.kairo.core.testing.SCREENSHOT_DIR
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowAlarmManager
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE_QUALIFIERS)
class TodayScreenshotTest {

    @Before
    fun grantReminderPermissions() {
        // Hide the permission banner: screenshots show the app as a set-up user sees it.
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
    }

    private val date = LocalDate.of(2026, 3, 10)
    private fun at(hour: Int, minute: Int = 0) = date.atTime(hour, minute).atZone(ZoneId.systemDefault()).toInstant()

    private val metformin = Medication(1, "Metformin", "500 mg", "With food", listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)))
    private val vitaminD = Medication(2, "Vitamin D3", "1000 IU", null, listOf(LocalTime.of(9, 0)))
    private val lisinopril = Medication(3, "Lisinopril", "10 mg", null, listOf(LocalTime.of(13, 0)))

    private val state = TodayUiState.Success(
        date = date,
        doses = listOf(
            TodayDose(ScheduledDose(metformin, at(8), DoseEvent(1, 1, at(8), at(8, 12), DoseStatus.TAKEN)), isOverdue = false),
            TodayDose(ScheduledDose(vitaminD, at(9), null), isOverdue = true),
            TodayDose(ScheduledDose(lisinopril, at(13), null), isOverdue = false),
            TodayDose(ScheduledDose(metformin, at(20), null), isOverdue = false),
        ),
        progress = DayProgress(taken = 1, total = 4),
        suggestions = listOf(TimingSuggestion(metformin, LocalTime.of(8, 0), LocalTime.of(8, 40), 40, 9)),
    )

    @Test
    fun today() = captureRoboImage("$SCREENSHOT_DIR/today.png") {
        KairoTheme { TodayScreen(state, onAddMedication = {}, onTaken = {}, onSkipped = {}, onUndo = {}) }
    }

    @Test
    @Config(qualifiers = "+night")
    fun todayDark() = captureRoboImage("$SCREENSHOT_DIR/today_dark.png") {
        KairoTheme { TodayScreen(state, onAddMedication = {}, onTaken = {}, onSkipped = {}, onUndo = {}) }
    }
}
