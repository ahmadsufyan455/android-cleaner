package com.zerodev.clen.presentation.home

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zerodev.clen.R

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
    val lastCleared = state.lastClearedBytes?.let { Formatter.formatFileSize(context, it) }

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
                state.errorMessage?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (state.isClearingCache) {
                    CircularProgressIndicator()
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
        Button(
            onClick = onQuickCleanClick,
            modifier = Modifier.padding(top = 24.dp),
        ) {
            Text(text = stringResource(R.string.quick_clean))
        }
    }
}
