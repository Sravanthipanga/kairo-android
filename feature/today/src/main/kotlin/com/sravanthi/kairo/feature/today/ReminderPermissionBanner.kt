package com.sravanthi.kairo.feature.today

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.sravanthi.kairo.core.reminders.ReminderPermissions

/**
 * Explains and requests what on-time reminders need. Shows nothing once both are granted.
 * Re-checks whenever the screen resumes, e.g. after returning from system settings.
 */
@Composable
internal fun ReminderPermissionBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var canNotify by remember { mutableStateOf(ReminderPermissions.canPostNotifications(context)) }
    var canAlarmExactly by remember { mutableStateOf(ReminderPermissions.canScheduleExactAlarms(context)) }

    LifecycleResumeEffect(Unit) {
        canNotify = ReminderPermissions.canPostNotifications(context)
        canAlarmExactly = ReminderPermissions.canScheduleExactAlarms(context)
        onPauseOrDispose { }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> canNotify = granted }

    when {
        !canNotify && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> BannerCard(
            message = stringResource(R.string.feature_today_permission_notifications),
            action = stringResource(R.string.feature_today_permission_notifications_action),
            onAction = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
            modifier = modifier,
        )
        !canAlarmExactly -> BannerCard(
            message = stringResource(R.string.feature_today_permission_exact_alarms),
            action = stringResource(R.string.feature_today_permission_exact_alarms_action),
            onAction = { ReminderPermissions.exactAlarmSettingsIntent(context)?.let(context::startActivity) },
            modifier = modifier,
        )
    }
}

@Composable
private fun BannerCard(message: String, action: String, onAction: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.NotificationsOff, contentDescription = null)
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
            FilledTonalButton(onClick = onAction, modifier = Modifier.align(Alignment.End)) { Text(action) }
        }
    }
}
