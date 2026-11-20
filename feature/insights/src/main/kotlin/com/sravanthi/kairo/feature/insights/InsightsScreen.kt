package com.sravanthi.kairo.feature.insights

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sravanthi.kairo.core.designsystem.theme.KairoTheme
import com.sravanthi.kairo.core.model.DailyActivity
import com.sravanthi.kairo.core.model.DayInsight
import com.sravanthi.kairo.core.model.DayProgress
import com.sravanthi.kairo.core.model.WeeklyInsights
import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun InsightsRoute(viewModel: InsightsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(viewModel.permissionContract()) {
        viewModel.refreshHealth()
    }
    // The user may have changed permissions or installed Health Connect in another app.
    LifecycleResumeEffect(Unit) {
        viewModel.refreshHealth()
        onPauseOrDispose { }
    }
    InsightsScreen(
        uiState = uiState,
        onConnect = { permissionLauncher.launch(viewModel.healthPermissions) },
        onInstall = {
            try {
                context.startActivity(viewModel.installHealthConnectIntent())
            } catch (_: ActivityNotFoundException) {
                // No Play Store on this device; nothing else to offer.
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InsightsScreen(
    uiState: InsightsUiState,
    onConnect: () -> Unit,
    onInstall: () -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.feature_insights_title)) }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                InsightsUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                InsightsUiState.NoMedications -> Text(
                    stringResource(R.string.feature_insights_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
                is InsightsUiState.Success -> InsightsContent(uiState, onConnect, onInstall)
            }
        }
    }
}

@Composable
private fun InsightsContent(state: InsightsUiState.Success, onConnect: () -> Unit, onInstall: () -> Unit) {
    val connected = state.health == HealthStatus.Connected
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SummaryCard(state.insights, connected) }
        when (state.health) {
            HealthStatus.NeedsPermission -> item {
                HealthConnectCard(
                    stringResource(R.string.feature_insights_connect_body),
                    stringResource(R.string.feature_insights_connect_action),
                    onConnect,
                )
            }
            HealthStatus.NeedsInstall -> item {
                HealthConnectCard(
                    stringResource(R.string.feature_insights_install_body),
                    stringResource(R.string.feature_insights_install_action),
                    onInstall,
                )
            }
            else -> Unit
        }
        item {
            Text(
                stringResource(R.string.feature_insights_daily),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        items(state.insights.days.reversed(), key = { it.date.toString() }) { DayRow(it, connected) }
        item {
            Text(
                stringResource(R.string.feature_insights_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun SummaryCard(insights: WeeklyInsights, connected: Boolean) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.feature_insights_this_week), style = MaterialTheme.typography.titleMedium)
            }
            val rate = insights.adherenceRate
            Text(
                if (rate == null) {
                    stringResource(R.string.feature_insights_no_doses)
                } else {
                    stringResource(R.string.feature_insights_adherence, (rate * 100).roundToInt())
                },
                style = MaterialTheme.typography.headlineSmall,
            )
            if (connected) {
                val parts = listOfNotNull(
                    insights.averageSteps?.let { stringResource(R.string.feature_insights_avg_steps, formatNumber(it)) },
                    insights.averageSleep?.let { stringResource(R.string.feature_insights_avg_sleep, formatDuration(it)) },
                )
                Text(
                    parts.joinToString(" · ").ifEmpty { stringResource(R.string.feature_insights_no_activity) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HealthConnectCard(message: String, action: String, onAction: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.MonitorHeart, contentDescription = null)
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
            FilledTonalButton(onClick = onAction, modifier = Modifier.align(Alignment.End)) { Text(action) }
        }
    }
}

@Composable
private fun DayRow(day: DayInsight, connected: Boolean) {
    val dayName = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val doses = stringResource(R.string.feature_insights_doses, day.adherence.taken, day.adherence.total)
    Column(
        Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = "$dayName, $doses" },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(dayName, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
            LinearProgressIndicator(
                progress = { day.adherence.fraction },
                modifier = Modifier.weight(1f),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Text(
                "${day.adherence.taken}/${day.adherence.total}",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.End,
                modifier = Modifier.width(48.dp),
            )
        }
        if (connected) {
            val activity = listOfNotNull(
                day.activity.steps?.let { stringResource(R.string.feature_insights_steps, formatNumber(it)) },
                day.activity.sleep?.let { stringResource(R.string.feature_insights_sleep, formatDuration(it)) },
            )
            if (activity.isNotEmpty()) {
                Text(
                    activity.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 48.dp, top = 2.dp),
                )
            }
        }
    }
}

private fun formatNumber(value: Long): String = NumberFormat.getIntegerInstance().format(value)

private fun formatDuration(duration: Duration): String = "%dh %02dm".format(duration.toHours(), duration.toMinutes() % 60)

@Preview(showBackground = true)
@Composable
private fun InsightsPreview() {
    val today = LocalDate.of(2026, 3, 10)
    val days = (6 downTo 0).map { offset ->
        DayInsight(
            date = today.minusDays(offset.toLong()),
            adherence = DayProgress(taken = if (offset == 2) 1 else 2, total = 2),
            activity = DailyActivity(steps = 6_000L + offset * 700, sleep = Duration.ofMinutes(400L + offset * 5)),
        )
    }
    KairoTheme {
        InsightsScreen(
            uiState = InsightsUiState.Success(WeeklyInsights(days), HealthStatus.Connected),
            onConnect = {},
            onInstall = {},
        )
    }
}
