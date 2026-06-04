package com.zerodev.clen.presentation.scan

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zerodev.clen.R
import com.zerodev.clen.domain.model.JunkCategory

@Composable
fun ScanScreen(
    state: ScanState,
    autoStart: Boolean,
    onAutoStartConsumed: () -> Unit,
    onStartScanClick: () -> Unit,
    onCancelScanClick: () -> Unit,
    onViewResultsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bytesFound = Formatter.formatFileSize(context, state.totalBytesFound)
    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        onStartScanClick()
    }

    LaunchedEffect(autoStart) {
        if (autoStart && !state.isScanning) {
            onAutoStartConsumed()
            if (state.mediaPermissions.isEmpty()) {
                onStartScanClick()
            } else {
                mediaPermissionLauncher.launch(state.mediaPermissions.toTypedArray())
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(R.string.scan_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.scan_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        if (!state.hasFullMediaAccess) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.reduced_scan_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = if (state.hasAnyMediaAccess) {
                            stringResource(R.string.reduced_scan_partial)
                        } else {
                            stringResource(R.string.reduced_scan_denied)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (state.isScanning) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(
                            R.string.scan_running_value,
                            bytesFound,
                            state.foundItemCount,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(
                            R.string.scan_current_category_value,
                            state.currentCategory?.label() ?: stringResource(R.string.scan_category_preparing),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            OutlinedButton(
                onClick = onCancelScanClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.cancel_scan))
            }
        } else {
            state.status?.let { status ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(
                            when (status) {
                                com.zerodev.clen.domain.model.ScanStatus.COMPLETED ->
                                    R.string.scan_status_completed
                                com.zerodev.clen.domain.model.ScanStatus.CANCELLED ->
                                    R.string.scan_status_cancelled
                                com.zerodev.clen.domain.model.ScanStatus.FAILED ->
                                    R.string.scan_status_failed
                                else -> R.string.scan_status_idle
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
            state.errorMessageResId?.let { errorMessageResId ->
                Text(
                    text = stringResource(errorMessageResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Button(
                onClick = {
                    if (state.mediaPermissions.isEmpty()) {
                        onStartScanClick()
                    } else {
                        mediaPermissionLauncher.launch(state.mediaPermissions.toTypedArray())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.start_scan))
            }
            OutlinedButton(
                onClick = onViewResultsClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.review_results))
            }
        }
    }
}

@Composable
private fun JunkCategory.label(): String =
    stringResource(
        when (this) {
            JunkCategory.OWN_CACHE -> R.string.category_own_cache
            JunkCategory.RESIDUAL_APK -> R.string.category_residual_apk
            JunkCategory.LARGE_FILE -> R.string.category_large_file
            JunkCategory.EMPTY_FOLDER -> R.string.category_empty_folder
            JunkCategory.OLD_DOWNLOAD -> R.string.category_old_download
            JunkCategory.DUPLICATE_FILE -> R.string.category_duplicate_file
            JunkCategory.DUPLICATE_PHOTO -> R.string.category_duplicate_photo
            JunkCategory.OLD_SCREENSHOT -> R.string.category_old_screenshot
            JunkCategory.WHATSAPP_MEDIA -> R.string.category_whatsapp_media
        },
    )
