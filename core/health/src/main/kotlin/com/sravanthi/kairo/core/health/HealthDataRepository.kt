package com.sravanthi.kairo.core.health

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.sravanthi.kairo.core.model.DailyActivity
import java.time.LocalDate
import java.time.ZoneId

enum class HealthAvailability {
    /** This device cannot run Health Connect. */
    NotSupported,

    /** Health Connect needs to be installed or updated from the Play Store. */
    NeedsInstall,
    Available,
}

/** Read-only access to steps and sleep. Kairo never writes to Health Connect. */
interface HealthDataRepository {
    fun availability(): HealthAvailability

    /** The read permissions Kairo asks for: steps and sleep only. */
    val permissions: Set<String>

    suspend fun hasPermissions(): Boolean

    /** Steps and sleep per day. Days without data are left out; errors return an empty map. */
    suspend fun readDailyActivity(days: List<LocalDate>, zone: ZoneId): Map<LocalDate, DailyActivity>

    /** Launches Health Connect's own permission screen. */
    fun permissionContract(): ActivityResultContract<Set<String>, Set<String>>

    /** Opens the Play Store page to install or update Health Connect. */
    fun installIntent(): Intent
}
