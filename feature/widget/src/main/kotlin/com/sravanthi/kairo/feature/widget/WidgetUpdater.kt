package com.sravanthi.kairo.feature.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Started from the Application: refreshes the widget whenever doses, medications or the
 * app lock setting change, e.g. after "Taken" in the app or on a notification.
 */
@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataSource: WidgetDataSource,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch {
            // Skip the first value: it describes what the widget already shows.
            dataSource.summary().drop(1).collect { KairoWidget().updateAll(context) }
        }
    }
}
