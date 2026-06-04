package com.zerodev.clen.presentation.trash

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.zerodev.clen.domain.model.TrashEntry

@Composable
fun TrashScreen(
    state: TrashState,
    onRestoreClick: (TrashEntry) -> Unit,
    onEmptyTrashClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val totalBytes = Formatter.formatFileSize(context, state.entries.sumOf { it.sizeBytes })

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(R.string.trash_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = if (state.entries.isEmpty()) {
                stringResource(R.string.trash_empty)
            } else {
                stringResource(R.string.trash_summary, totalBytes, state.entries.size)
            },
            style = MaterialTheme.typography.bodyLarge,
        )
        if (state.entries.isNotEmpty()) {
            OutlinedButton(
                onClick = onEmptyTrashClick,
                enabled = !state.isWorking,
            ) {
                Text(text = stringResource(R.string.empty_trash))
            }
        }
        state.messageResId?.let { messageResId ->
            Text(
                text = stringResource(
                    id = messageResId,
                    *state.messageArgs.toTypedArray(),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        state.errorMessageResId?.let { messageResId ->
            Text(
                text = stringResource(messageResId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(
                items = state.entries,
                key = { entry -> entry.id },
            ) { entry ->
                TrashEntryRow(
                    entry = entry,
                    onRestoreClick = onRestoreClick,
                    restoreEnabled = !state.isWorking && entry.restorable,
                )
            }
        }
    }
}

@Composable
private fun TrashEntryRow(
    entry: TrashEntry,
    onRestoreClick: (TrashEntry) -> Unit,
    restoreEnabled: Boolean,
) {
    val context = LocalContext.current

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
                    text = entry.originalUri.substringAfterLast('/'),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = Formatter.formatFileSize(context, entry.sizeBytes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = { onRestoreClick(entry) },
                enabled = restoreEnabled,
            ) {
                Text(text = stringResource(R.string.restore))
            }
        }
    }
}
