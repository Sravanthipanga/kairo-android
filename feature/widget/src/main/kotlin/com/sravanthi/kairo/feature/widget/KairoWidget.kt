package com.sravanthi.kairo.feature.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dagger.hilt.android.EntryPointAccessors
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Home-screen widget: the next dose to take today, with a one-tap Taken button. */
class KairoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataSource = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .dataSource()
        provideContent {
            val summary by dataSource.summary().collectAsState(initial = null)
            GlanceTheme { WidgetContent(summary) }
        }
    }
}

class KairoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KairoWidget()
}

private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

@Composable
private fun WidgetContent(summary: WidgetSummary?) {
    val context = LocalContext.current
    val openApp = context.packageManager.getLaunchIntentForPackage(context.packageName)
    var modifier = GlanceModifier
        .fillMaxSize()
        .cornerRadius(16.dp)
        .background(GlanceTheme.colors.widgetBackground)
        .padding(12.dp)
    if (openApp != null) modifier = modifier.clickable(actionStartActivity(openApp))

    Column(modifier = modifier, verticalAlignment = Alignment.Vertical.CenterVertically) {
        Text(
            context.getString(R.string.feature_widget_label),
            style = TextStyle(color = GlanceTheme.colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium),
        )
        Spacer(GlanceModifier.height(4.dp))
        when (summary) {
            null -> Unit
            WidgetSummary.NoMedications -> BodyText(context.getString(R.string.feature_widget_empty))
            is WidgetSummary.AllDone -> {
                TitleText(context.getString(R.string.feature_widget_all_done))
                BodyText(progressText(context, summary.progress.taken, summary.progress.total))
            }
            is WidgetSummary.Next -> NextDose(context, summary)
        }
    }
}

@Composable
private fun NextDose(context: Context, summary: WidgetSummary.Next) {
    val dose = summary.dose
    val time = dose.scheduledAt.atZone(ZoneId.systemDefault()).toLocalTime().format(timeFormatter)
    Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
        Text(
            time,
            style = TextStyle(
                color = if (summary.isOverdue) GlanceTheme.colors.error else GlanceTheme.colors.onSurface,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        if (summary.isOverdue) {
            Text(
                "  " + context.getString(R.string.feature_widget_overdue),
                style = TextStyle(color = GlanceTheme.colors.error, fontSize = 12.sp),
            )
        }
    }
    if (summary.hideDetails) {
        BodyText(context.getString(R.string.feature_widget_hidden))
    } else {
        TitleText(dose.medication.name)
        BodyText(dose.medication.dosage)
        Spacer(GlanceModifier.height(6.dp))
        Button(
            text = context.getString(R.string.feature_widget_taken),
            onClick = actionRunCallback<TakenAction>(
                actionParametersOf(
                    TakenAction.MEDICATION_ID to dose.medication.id,
                    TakenAction.SCHEDULED_AT to dose.scheduledAt.toEpochMilli(),
                ),
            ),
        )
    }
    Spacer(GlanceModifier.height(4.dp))
    BodyText(progressText(context, summary.progress.taken, summary.progress.total))
}

@Composable
private fun TitleText(text: String) = Text(
    text,
    maxLines = 1,
    style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium),
)

@Composable
private fun BodyText(text: String) = Text(
    text,
    maxLines = 2,
    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 13.sp),
)

private fun progressText(context: Context, taken: Int, total: Int) =
    context.resources.getQuantityString(R.plurals.feature_widget_progress, total, taken, total)
