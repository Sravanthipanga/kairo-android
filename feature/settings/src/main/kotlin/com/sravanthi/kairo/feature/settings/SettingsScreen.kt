package com.sravanthi.kairo.feature.settings

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.PhonelinkLock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sravanthi.kairo.core.designsystem.theme.KairoTheme

@Composable
internal fun SettingsRoute(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var lockAvailable by remember { mutableStateOf(true) }
    // Re-check on resume: the user may have just set up a screen lock in system settings.
    LifecycleResumeEffect(Unit) {
        lockAvailable = BiometricManager.from(context).canAuthenticate(BIOMETRIC_WEAK or DEVICE_CREDENTIAL) ==
            BiometricManager.BIOMETRIC_SUCCESS
        onPauseOrDispose { }
    }
    SettingsScreen(uiState, lockAvailable, onAppLockChange = viewModel::setAppLockEnabled)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    lockAvailable: Boolean,
    onAppLockChange: (Boolean) -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.feature_settings_title)) }) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                stringResource(R.string.feature_settings_privacy),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
            )
            val enabled = (uiState as? SettingsUiState.Ready)?.appLockEnabled ?: false
            val canToggle = uiState is SettingsUiState.Ready && (lockAvailable || enabled)
            ListItem(
                headlineContent = { Text(stringResource(R.string.feature_settings_app_lock)) },
                supportingContent = {
                    Text(
                        stringResource(
                            if (lockAvailable) {
                                R.string.feature_settings_app_lock_description
                            } else {
                                R.string.feature_settings_app_lock_unavailable
                            },
                        ),
                    )
                },
                leadingContent = { Icon(Icons.Outlined.Fingerprint, contentDescription = null) },
                trailingContent = { Switch(checked = enabled, onCheckedChange = null, enabled = canToggle) },
                modifier = Modifier.toggleable(
                    value = enabled,
                    enabled = canToggle,
                    role = Role.Switch,
                    onValueChange = onAppLockChange,
                ),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.feature_settings_on_device)) },
                supportingContent = { Text(stringResource(R.string.feature_settings_on_device_description)) },
                leadingContent = { Icon(Icons.Outlined.PhonelinkLock, contentDescription = null) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    KairoTheme { SettingsScreen(SettingsUiState.Ready(appLockEnabled = true), lockAvailable = true, onAppLockChange = {}) }
}
