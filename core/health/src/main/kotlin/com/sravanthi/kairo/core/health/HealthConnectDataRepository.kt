package com.sravanthi.kairo.core.health

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.net.toUri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.sravanthi.kairo.core.model.DailyActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import javax.inject.Inject

internal class HealthConnectDataRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : HealthDataRepository {

    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    override val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
    )

    override fun availability(): HealthAvailability = when (HealthConnectClient.getSdkStatus(context)) {
        HealthConnectClient.SDK_AVAILABLE -> HealthAvailability.Available
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthAvailability.NeedsInstall
        else -> HealthAvailability.NotSupported
    }

    override suspend fun hasPermissions(): Boolean =
        availability() == HealthAvailability.Available &&
            runCatching { client.permissionController.getGrantedPermissions().containsAll(permissions) }
                .getOrDefault(false)

    override suspend fun readDailyActivity(days: List<LocalDate>, zone: ZoneId): Map<LocalDate, DailyActivity> {
        if (days.isEmpty() || !hasPermissions()) return emptyMap()
        val first = days.min()
        val last = days.max()
        val steps = runCatching { readSteps(first, last) }.getOrDefault(emptyMap())
        val sleep = runCatching { readSleep(first, last) }.getOrDefault(emptyMap())
        return (steps.keys + sleep.keys).associateWith { DailyActivity(steps = steps[it], sleep = sleep[it]) }
    }

    /** Steps per calendar day, in local time. */
    private suspend fun readSteps(first: LocalDate, last: LocalDate): Map<LocalDate, Long> =
        client.aggregateGroupByPeriod(
            AggregateGroupByPeriodRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(first.atStartOfDay(), last.plusDays(1).atStartOfDay()),
                timeRangeSlicer = Period.ofDays(1),
            ),
        ).mapNotNull { bucket ->
            bucket.result[StepsRecord.COUNT_TOTAL]?.let { bucket.startTime.toLocalDate() to it }
        }.toMap()

    /**
     * Sleep per night, credited to the morning it ends: each day runs from 6 pm the evening before
     * to 6 pm, so a night that crosses midnight counts once, for the day you woke up.
     */
    private suspend fun readSleep(first: LocalDate, last: LocalDate): Map<LocalDate, Duration> =
        client.aggregateGroupByPeriod(
            AggregateGroupByPeriodRequest(
                metrics = setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(
                    first.minusDays(1).atTime(SLEEP_DAY_START_HOUR, 0),
                    last.atTime(SLEEP_DAY_START_HOUR, 0),
                ),
                timeRangeSlicer = Period.ofDays(1),
            ),
        ).mapNotNull { bucket ->
            bucket.result[SleepSessionRecord.SLEEP_DURATION_TOTAL]
                ?.takeIf { !it.isZero }
                ?.let { bucket.startTime.toLocalDate().plusDays(1) to it }
        }.toMap()

    override fun permissionContract(): ActivityResultContract<Set<String>, Set<String>> =
        PermissionController.createRequestPermissionResultContract()

    override fun installIntent(): Intent =
        Intent(Intent.ACTION_VIEW, "market://details?id=$PROVIDER_PACKAGE&url=healthconnect%3A%2F%2Fonboarding".toUri())
            .setPackage("com.android.vending")
            .putExtra("overlay", true)
            .putExtra("callerId", context.packageName)

    private companion object {
        const val PROVIDER_PACKAGE = "com.google.android.apps.healthdata"
        const val SLEEP_DAY_START_HOUR = 18
    }
}
