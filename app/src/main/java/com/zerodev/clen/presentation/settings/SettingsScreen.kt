package com.zerodev.clen.presentation.settings

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zerodev.clen.R
import com.zerodev.clen.domain.model.ThemeMode

@Composable
fun SettingsScreen(
    state: SettingsState,
    onSafTreePicked: (android.net.Uri) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onLargeFileThresholdChanged: (Int) -> Unit,
    onOldFileThresholdChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) {
            onSafTreePicked(uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.settings_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        SettingsSection(title = stringResource(R.string.appearance_title)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { themeMode ->
                    FilterChip(
                        selected = state.themeMode == themeMode,
                        onClick = { onThemeModeSelected(themeMode) },
                        label = { Text(text = themeMode.label()) },
                    )
                }
            }
            SettingSwitchRow(
                title = stringResource(R.string.dynamic_color_title),
                subtitle = stringResource(R.string.dynamic_color_subtitle),
                checked = state.dynamicColorEnabled,
                onCheckedChange = onDynamicColorChanged,
            )
        }
        SettingsSection(title = stringResource(R.string.scan_thresholds_title)) {
            val largeFileMb = (state.largeFileThresholdBytes / 1024L / 1024L).toInt()
            Text(
                text = stringResource(R.string.large_file_threshold_value, largeFileMb),
                style = MaterialTheme.typography.bodyLarge,
            )
            Slider(
                value = largeFileMb.toFloat().coerceIn(50f, 1024f),
                onValueChange = { onLargeFileThresholdChanged(it.toInt()) },
                valueRange = 50f..1024f,
                steps = 18,
            )
            Text(
                text = stringResource(R.string.old_download_threshold_value, state.oldFileThresholdDays),
                style = MaterialTheme.typography.bodyLarge,
            )
            Slider(
                value = state.oldFileThresholdDays.toFloat().coerceIn(7f, 180f),
                onValueChange = { onOldFileThresholdChanged(it.toInt()) },
                valueRange = 7f..180f,
                steps = 23,
            )
        }
        SettingsSection(title = stringResource(R.string.permissions_title)) {
            PermissionStatusRow(
                title = stringResource(R.string.media_access_title),
                value = when {
                    state.hasFullMediaAccess -> stringResource(R.string.permission_full)
                    state.hasAnyMediaAccess -> stringResource(R.string.permission_partial)
                    else -> stringResource(R.string.permission_denied)
                },
            )
            PermissionStatusRow(
                title = stringResource(R.string.saf_grants_title),
                value = stringResource(R.string.saf_grants_count, state.safGrantCount),
            )
            PermissionStatusRow(
                title = stringResource(R.string.whitelist_title),
                value = stringResource(R.string.whitelist_count, state.whitelistedCount),
            )
            Button(onClick = { folderPicker.launch(null) }) {
                Text(text = stringResource(R.string.add_folder_grant))
            }
            OutlinedButton(
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        },
                    )
                },
            ) {
                Text(text = stringResource(R.string.open_android_settings))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            content()
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun PermissionStatusRow(
    title: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        AssistChip(
            onClick = {},
            label = { Text(text = value) },
        )
    }
}

@Composable
private fun ThemeMode.label(): String =
    stringResource(
        when (this) {
            ThemeMode.SYSTEM -> R.string.theme_system
            ThemeMode.LIGHT -> R.string.theme_light
            ThemeMode.DARK -> R.string.theme_dark
        },
    )
