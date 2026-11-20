package com.sravanthi.kairo.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sravanthi.kairo.core.designsystem.theme.KairoTheme
import com.sravanthi.kairo.core.model.Medication
import com.sravanthi.kairo.core.model.TimingSuggestion
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.abs

private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

/** "Kairo noticed…": proposes moving a reminder to match the user's real routine. */
@Composable
internal fun TimingSuggestionCard(
    suggestion: TimingSuggestion,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val current = suggestion.currentTime.format(timeFormatter)
    val suggested = suggestion.suggestedTime.format(timeFormatter)
    val minutes = abs(suggestion.typicalDelayMinutes).toInt()
    val explanation = pluralStringResource(
        if (suggestion.typicalDelayMinutes > 0) R.plurals.feature_today_suggestion_late else R.plurals.feature_today_suggestion_early,
        suggestion.sampleCount,
        minutes,
        current,
        suggestion.sampleCount,
    )

    OutlinedCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    stringResource(R.string.feature_today_suggestion_title, suggestion.medication.name, suggested),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            Text(explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.feature_today_suggestion_dismiss)) }
                FilledTonalButton(onClick = onAccept) {
                    Text(stringResource(R.string.feature_today_suggestion_accept, suggested))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimingSuggestionCardPreview() {
    val metformin = Medication(1, "Metformin", "500 mg", null, listOf(LocalTime.of(8, 0)))
    KairoTheme {
        TimingSuggestionCard(
            suggestion = TimingSuggestion(metformin, LocalTime.of(8, 0), LocalTime.of(8, 40), 40, 9),
            onAccept = {},
            onDismiss = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
