package com.zerodev.clen.presentation.home

import android.text.format.Formatter
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zerodev.clen.R
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(
    state: HomeState,
    onQuickCleanClick: () -> Unit,
    onRefreshCacheClick: () -> Unit,
    onClearCacheClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cacheSize = Formatter.formatFileSize(context, state.cacheSizeBytes)
    val storageUsed = Formatter.formatFileSize(context, state.storageUsedBytes)
    val storageTotal = Formatter.formatFileSize(context, state.storageTotalBytes)
    val storageFree = Formatter.formatFileSize(context, state.storageFreeBytes)
    val latestScanBytes = Formatter.formatFileSize(context, state.latestScanBytes)
    val lastCleared = state.lastClearedBytes?.let { Formatter.formatFileSize(context, it) }
    val lastScan = state.lastScanAtMillis?.toDisplayTime()
    val lastClean = state.lastCleanAtMillis?.toDisplayTime()
    val usedProgress = if (state.storageTotalBytes <= 0L) {
        0f
    } else {
        state.storageUsedBytes.toFloat() / state.storageTotalBytes.toFloat()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(
            onClick = onQuickCleanClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.quick_clean))
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.storage_overview_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = if (state.isLoadingStorageStats) {
                        stringResource(R.string.storage_overview_loading)
                    } else {
                        stringResource(R.string.storage_overview_value, storageUsed, storageTotal)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (!state.isLoadingStorageStats) {
                    LinearProgressIndicator(
                        progress = { usedProgress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (!state.isLoadingStorageStats) {
                    Text(
                        text = stringResource(R.string.storage_free_value, storageFree),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.latest_scan_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = if (state.lastScanAtMillis == null) {
                        stringResource(R.string.latest_scan_empty)
                    } else {
                        stringResource(
                            R.string.latest_scan_value,
                            latestScanBytes,
                            state.latestScanItemCount,
                        )
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
                lastScan?.let { value ->
                    Text(
                        text = stringResource(R.string.last_scan_value, value),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                lastClean?.let { value ->
                    Text(
                        text = stringResource(R.string.last_clean_value, value),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.own_cache_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = if (state.isLoadingCacheSize) {
                        stringResource(R.string.cache_size_loading)
                    } else {
                        stringResource(R.string.cache_size_value, cacheSize)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
                lastCleared?.let { cleared ->
                    Text(
                        text = stringResource(R.string.cache_cleared_value, cleared),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                state.errorMessageResId?.let { errorMessageResId ->
                    Text(
                        text = stringResource(errorMessageResId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (state.isClearingCache) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                OutlinedButton(
                    onClick = onRefreshCacheClick,
                    enabled = !state.isLoadingCacheSize && !state.isClearingCache,
                ) {
                    Text(text = stringResource(R.string.refresh_cache))
                }
                Button(
                    onClick = onClearCacheClick,
                    enabled = !state.isLoadingCacheSize &&
                        !state.isClearingCache &&
                        state.cacheSizeBytes > 0L,
                ) {
                    Text(text = stringResource(R.string.clear_own_cache))
                }
            }
        }
    }
}

private fun Long.toDisplayTime(): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(this))
