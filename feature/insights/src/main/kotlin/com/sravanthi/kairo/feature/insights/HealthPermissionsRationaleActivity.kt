package com.sravanthi.kairo.feature.insights

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sravanthi.kairo.core.designsystem.theme.KairoTheme

/**
 * Health Connect opens this screen when the user asks why Kairo wants their data.
 * Required for every app that requests Health Connect permissions.
 */
class HealthPermissionsRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            KairoTheme {
                Surface(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.safeDrawingPadding().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(stringResource(R.string.feature_insights_rationale_title), style = MaterialTheme.typography.headlineSmall)
                        Text(stringResource(R.string.feature_insights_rationale_body), style = MaterialTheme.typography.bodyLarge)
                        Button(onClick = ::finish) { Text(stringResource(R.string.feature_insights_rationale_close)) }
                    }
                }
            }
        }
    }
}
