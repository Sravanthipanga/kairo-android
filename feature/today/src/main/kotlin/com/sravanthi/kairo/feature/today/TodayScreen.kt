package com.sravanthi.kairo.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sravanthi.kairo.core.designsystem.theme.KairoTheme
import com.sravanthi.kairo.core.model.DayProgress
import com.sravanthi.kairo.core.model.DoseEvent
import com.sravanthi.kairo.core.model.DoseStatus
import com.sravanthi.kairo.core.model.Medication
import com.sravanthi.kairo.core.model.ScheduledDose
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun TodayRoute(
    onAddMedication: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TodayScreen(
        uiState = uiState,
        onAddMedication = onAddMedication,
        onTaken = viewModel::markTaken,
        onSkipped = viewModel::markSkipped,
        onUndo = viewModel::undo,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TodayScreen(
    uiState: TodayUiState,
    onAddMedication: () -> Unit,
    onTaken: (ScheduledDose) -> Unit,
    onSkipped: (ScheduledDose) -> Unit,
    onUndo: (ScheduledDose) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.feature_today_title)) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                TodayUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                TodayUiState.NoMedications -> NoMedications(onAddMedication, Modifier.align(Alignment.Center))
                is TodayUiState.Success -> DoseList(uiState, onTaken, onSkipped, onUndo)
            }
        }
    }
}

@Composable
private fun NoMedications(onAddMedication: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Outlined.EventAvailable,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            stringResource(R.string.feature_today_empty_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            stringResource(R.string.feature_today_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onAddMedication) { Text(stringResource(R.string.feature_today_add_medication)) }
    }
}

private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

@Composable
private fun DoseList(
    state: TodayUiState.Success,
    onTaken: (ScheduledDose) -> Unit,
    onSkipped: (ScheduledDose) -> Unit,
    onUndo: (ScheduledDose) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ReminderPermissionBanner() }
        item { ProgressHeader(state.date, state.progress) }
        items(state.doses, key = { "${it.dose.medication.id}@${it.dose.scheduledAt}" }) { item ->
            DoseCard(item, onTaken, onSkipped, onUndo)
        }
    }
}

@Composable
private fun ProgressHeader(date: LocalDate, progress: DayProgress) {
    val summary = pluralStringResource(R.plurals.feature_today_progress, progress.total, progress.taken, progress.total)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .semantics(mergeDescendants = true) { contentDescription = summary },
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress.fraction },
                modifier = Modifier.size(56.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Text("${progress.taken}/${progress.total}", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(date.format(dateFormatter), style = MaterialTheme.typography.titleMedium)
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DoseCard(
    item: TodayDose,
    onTaken: (ScheduledDose) -> Unit,
    onSkipped: (ScheduledDose) -> Unit,
    onUndo: (ScheduledDose) -> Unit,
) {
    val dose = item.dose
    val zone = ZoneId.systemDefault()
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        dose.scheduledAt.atZone(zone).toLocalTime().format(timeFormatter),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (item.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                    )
                    Text(dose.medication.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        dose.medication.dosage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusLabel(item)
            }
            when (dose.status) {
                DoseStatus.PENDING, DoseStatus.SNOOZED -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = { onTaken(dose) }) {
                        Icon(Icons.Filled.Check, null, Modifier.size(18.dp))
                        Text(stringResource(R.string.feature_today_take), Modifier.padding(start = 8.dp))
                    }
                    TextButton(onClick = { onSkipped(dose) }) { Text(stringResource(R.string.feature_today_skip)) }
                }
                DoseStatus.TAKEN, DoseStatus.SKIPPED ->
                    TextButton(onClick = { onUndo(dose) }) { Text(stringResource(R.string.feature_today_undo)) }
            }
        }
    }
}

@Composable
private fun StatusLabel(item: TodayDose) {
    val zone = ZoneId.systemDefault()
    val (text, color) = when (item.dose.status) {
        DoseStatus.TAKEN -> {
            val at = item.dose.event?.takenAt?.atZone(zone)?.toLocalTime()?.format(timeFormatter).orEmpty()
            stringResource(R.string.feature_today_taken_at, at) to MaterialTheme.colorScheme.primary
        }
        DoseStatus.SKIPPED -> stringResource(R.string.feature_today_skipped) to MaterialTheme.colorScheme.onSurfaceVariant
        DoseStatus.PENDING, DoseStatus.SNOOZED -> if (item.isOverdue) {
            stringResource(R.string.feature_today_overdue) to MaterialTheme.colorScheme.error
        } else {
            return
        }
    }
    Text(text, style = MaterialTheme.typography.labelLarge, color = color)
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenPreview() {
    val zone = ZoneId.systemDefault()
    val date = LocalDate.of(2026, 3, 10)
    val metformin = Medication(1, "Metformin", "500 mg", null, listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)))
    val vitaminD = Medication(2, "Vitamin D3", "1000 IU", null, listOf(LocalTime.of(9, 0)))
    fun at(h: Int) = date.atTime(h, 0).atZone(zone).toInstant()
    KairoTheme {
        TodayScreen(
            uiState = TodayUiState.Success(
                date = date,
                doses = listOf(
                    TodayDose(
                        ScheduledDose(metformin, at(8), DoseEvent(1, 1, at(8), at(8).plusSeconds(600), DoseStatus.TAKEN)),
                        isOverdue = false,
                    ),
                    TodayDose(ScheduledDose(vitaminD, at(9), null), isOverdue = true),
                    TodayDose(ScheduledDose(metformin, at(20), null), isOverdue = false),
                ),
                progress = DayProgress(taken = 1, total = 3),
            ),
            onAddMedication = {}, onTaken = {}, onSkipped = {}, onUndo = {},
        )
    }
}
