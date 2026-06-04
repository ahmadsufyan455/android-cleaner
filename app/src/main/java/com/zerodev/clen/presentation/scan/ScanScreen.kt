package com.zerodev.clen.presentation.scan

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zerodev.clen.R

@Composable
fun ScanScreen(
    state: ScanState,
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
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
        if (state.isScanning) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
            Text(
                text = stringResource(R.string.scan_running_value, bytesFound, state.foundItemCount),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = stringResource(R.string.scan_current_category),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            OutlinedButton(
                onClick = onCancelScanClick,
                modifier = Modifier.padding(top = 24.dp),
            ) {
                Text(text = stringResource(R.string.cancel_scan))
            }
        } else {
            state.status?.let { status ->
                Text(
                    text = stringResource(
                        when (status.name) {
                            "COMPLETED" -> R.string.scan_status_completed
                            "CANCELLED" -> R.string.scan_status_cancelled
                            "FAILED" -> R.string.scan_status_failed
                            else -> R.string.scan_status_idle
                        },
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
            state.errorMessage?.let { errorMessage ->
                Text(
                    text = errorMessage,
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
                modifier = Modifier.padding(top = 24.dp),
            ) {
                Text(text = stringResource(R.string.start_scan))
            }
            OutlinedButton(
                onClick = onViewResultsClick,
                modifier = Modifier.padding(top = 12.dp),
            ) {
                Text(text = stringResource(R.string.view_results))
            }
        }
    }
}
