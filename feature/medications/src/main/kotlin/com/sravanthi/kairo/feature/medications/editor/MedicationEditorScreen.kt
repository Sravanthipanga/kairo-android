package com.sravanthi.kairo.feature.medications.editor

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sravanthi.kairo.core.designsystem.theme.KairoTheme
import com.sravanthi.kairo.feature.medications.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun MedicationEditorRoute(
    onDone: () -> Unit,
    viewModel: MedicationEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) onDone()
    }

    MedicationEditorScreen(
        uiState = uiState,
        onBack = onDone,
        onNameChange = viewModel::onNameChange,
        onDosageChange = viewModel::onDosageChange,
        onInstructionsChange = viewModel::onInstructionsChange,
        onAddTime = viewModel::addReminderTime,
        onRemoveTime = viewModel::removeReminderTime,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
    )
}

private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun MedicationEditorScreen(
    uiState: MedicationEditorUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onDosageChange: (String) -> Unit,
    onInstructionsChange: (String) -> Unit,
    onAddTime: (LocalTime) -> Unit,
    onRemoveTime: (LocalTime) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
) {
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (uiState.isNew) R.string.feature_medications_add_title else R.string.feature_medications_edit_title,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.feature_medications_back))
                    }
                },
                actions = {
                    if (!uiState.isNew) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Outlined.Delete, stringResource(R.string.feature_medications_delete))
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.feature_medications_name_label)) },
                isError = uiState.nameError,
                supportingText = if (uiState.nameError) {
                    { Text(stringResource(R.string.feature_medications_name_required)) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.dosage,
                onValueChange = onDosageChange,
                label = { Text(stringResource(R.string.feature_medications_dosage_label)) },
                placeholder = { Text(stringResource(R.string.feature_medications_dosage_placeholder)) },
                isError = uiState.dosageError,
                supportingText = if (uiState.dosageError) {
                    { Text(stringResource(R.string.feature_medications_dosage_required)) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.instructions,
                onValueChange = onInstructionsChange,
                label = { Text(stringResource(R.string.feature_medications_instructions_label)) },
                placeholder = { Text(stringResource(R.string.feature_medications_instructions_placeholder)) },
                minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )

            Text(stringResource(R.string.feature_medications_reminders_heading), style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.reminderTimes.forEach { time ->
                    val label = time.format(timeFormatter)
                    InputChip(
                        selected = false,
                        onClick = { onRemoveTime(time) },
                        label = { Text(label) },
                        leadingIcon = { Icon(Icons.Outlined.Alarm, null, Modifier.size(18.dp)) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                stringResource(R.string.feature_medications_remove_time, label),
                                Modifier.size(18.dp),
                            )
                        },
                    )
                }
            }
            if (uiState.timesError) {
                Text(
                    stringResource(R.string.feature_medications_times_required),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            OutlinedButton(onClick = { showTimePicker = true }) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Text(stringResource(R.string.feature_medications_add_time), Modifier.padding(start = 8.dp))
            }

            Button(
                onClick = onSave,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Text(stringResource(R.string.feature_medications_save))
            }
        }
    }

    if (showTimePicker) {
        ReminderTimePickerDialog(
            onConfirm = {
                onAddTime(it)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.feature_medications_delete_title, uiState.name)) },
            text = { Text(stringResource(R.string.feature_medications_delete_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                ) { Text(stringResource(R.string.feature_medications_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.feature_medications_cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = DateFormat.is24HourFormat(LocalContext.current),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.feature_medications_add_time)) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                Text(stringResource(R.string.feature_medications_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.feature_medications_cancel)) }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun MedicationEditorPreview() {
    KairoTheme {
        MedicationEditorScreen(
            uiState = MedicationEditorUiState(
                isNew = false,
                name = "Metformin",
                dosage = "500 mg",
                instructions = "With food",
                reminderTimes = listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)),
            ),
            onBack = {}, onNameChange = {}, onDosageChange = {}, onInstructionsChange = {},
            onAddTime = {}, onRemoveTime = {}, onSave = {}, onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MedicationEditorErrorsPreview() {
    KairoTheme {
        MedicationEditorScreen(
            uiState = MedicationEditorUiState(isNew = true, nameError = true, dosageError = true, timesError = true),
            onBack = {}, onNameChange = {}, onDosageChange = {}, onInstructionsChange = {},
            onAddTime = {}, onRemoveTime = {}, onSave = {}, onDelete = {},
        )
    }
}
