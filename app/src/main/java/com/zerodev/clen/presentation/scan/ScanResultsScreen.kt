package com.zerodev.clen.presentation.scan

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zerodev.clen.R

@Composable
fun ScanResultsScreen(
    state: ScanResultsState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val totalBytes = Formatter.formatFileSize(context, state.items.sumOf { it.sizeBytes })

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(R.string.scan_results_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = if (state.items.isEmpty()) {
                stringResource(R.string.scan_results_empty)
            } else {
                stringResource(R.string.scan_results_summary, totalBytes, state.items.size)
            },
            style = MaterialTheme.typography.bodyLarge,
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = state.items,
                key = { it.uri },
            ) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.displayName,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = categoryLabel(item.category),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = Formatter.formatFileSize(context, item.sizeBytes),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun categoryLabel(category: com.zerodev.clen.domain.model.JunkCategory): String =
    stringResource(
        when (category) {
            com.zerodev.clen.domain.model.JunkCategory.OWN_CACHE -> R.string.category_own_cache
            com.zerodev.clen.domain.model.JunkCategory.RESIDUAL_APK -> R.string.category_residual_apk
            com.zerodev.clen.domain.model.JunkCategory.LARGE_FILE -> R.string.category_large_file
            com.zerodev.clen.domain.model.JunkCategory.EMPTY_FOLDER -> R.string.category_empty_folder
            com.zerodev.clen.domain.model.JunkCategory.OLD_DOWNLOAD -> R.string.category_old_download
            com.zerodev.clen.domain.model.JunkCategory.DUPLICATE_FILE -> R.string.category_duplicate_file
            com.zerodev.clen.domain.model.JunkCategory.DUPLICATE_PHOTO -> R.string.category_duplicate_photo
            com.zerodev.clen.domain.model.JunkCategory.OLD_SCREENSHOT -> R.string.category_old_screenshot
            com.zerodev.clen.domain.model.JunkCategory.WHATSAPP_MEDIA -> R.string.category_whatsapp_media
        },
    )
