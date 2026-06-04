package com.zerodev.clen.presentation.scan

import android.app.Activity
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zerodev.clen.R
import com.zerodev.clen.domain.model.FileItem
import com.zerodev.clen.domain.model.JunkCategory

@Composable
fun ScanResultsScreen(
    state: ScanResultsState,
    onToggleItem: (String) -> Unit,
    onToggleCategory: (JunkCategory) -> Unit,
    onSetCategorySelected: (JunkCategory, Boolean) -> Unit,
    onWhitelistSelected: () -> Unit,
    onCleanSelected: () -> Unit,
    onConfirmCleanSelected: () -> Unit,
    onDismissCleanConfirmation: () -> Unit,
    onMediaDeleteRequestConsumed: () -> Unit,
    onMediaDeleteResult: (Boolean, List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val totalBytes = Formatter.formatFileSize(context, state.items.sumOf { it.sizeBytes })
    val selectedBytes = Formatter.formatFileSize(context, state.selectedBytes)
    val groupedItems = state.items.groupBy { item -> item.category }
    val launchedMediaDeleteRequest = remember { mutableStateOf<MediaDeleteRequest?>(null) }
    val mediaDeleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        val request = launchedMediaDeleteRequest.value
        if (request != null) {
            onMediaDeleteResult(result.resultCode == Activity.RESULT_OK, request.uris)
            launchedMediaDeleteRequest.value = null
        }
    }

    LaunchedEffect(state.pendingMediaDeleteRequest) {
        val request = state.pendingMediaDeleteRequest ?: return@LaunchedEffect
        launchedMediaDeleteRequest.value = request
        mediaDeleteLauncher.launch(
            IntentSenderRequest.Builder(request.intentSender).build(),
        )
        onMediaDeleteRequestConsumed()
    }

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
        if (state.selectedUris.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(
                            R.string.scan_results_selected_summary,
                            selectedBytes,
                            state.selectedUris.size,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onWhitelistSelected) {
                            Text(text = stringResource(R.string.whitelist_selected))
                        }
                        Button(
                            onClick = onCleanSelected,
                            enabled = !state.isCleaning,
                        ) {
                            Text(text = stringResource(R.string.clean_selected))
                        }
                    }
                }
            }
        }
        if (state.isCleaning) {
            Text(
                text = stringResource(R.string.cleaning_selected),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        state.cleanMessageResId?.let { messageResId ->
            Text(
                text = stringResource(
                    id = messageResId,
                    *state.cleanMessageArgs.toTypedArray(),
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
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            groupedItems.forEach { (category, items) ->
                item(key = category.name) {
                    CategoryHeader(
                        category = category,
                        items = items,
                        selectedUris = state.selectedUris,
                        expanded = category in state.expandedCategories,
                        onToggleCategory = onToggleCategory,
                        onSetCategorySelected = onSetCategorySelected,
                    )
                }
                if (category in state.expandedCategories) {
                    items(
                        items = items,
                        key = { it.uri },
                    ) { item ->
                        ResultItemRow(
                            item = item,
                            selected = item.uri in state.selectedUris,
                            onToggleItem = onToggleItem,
                        )
                    }
                }
            }
        }
    }

    if (state.showPermanentDeleteWarning) {
        AlertDialog(
            onDismissRequest = onDismissCleanConfirmation,
            title = {
                Text(text = stringResource(R.string.permanent_delete_warning_title))
            },
            text = {
                Text(text = stringResource(R.string.permanent_delete_warning_body))
            },
            confirmButton = {
                Button(onClick = onConfirmCleanSelected) {
                    Text(text = stringResource(R.string.permanently_delete))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismissCleanConfirmation) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun CategoryHeader(
    category: JunkCategory,
    items: List<FileItem>,
    selectedUris: Set<String>,
    expanded: Boolean,
    onToggleCategory: (JunkCategory) -> Unit,
    onSetCategorySelected: (JunkCategory, Boolean) -> Unit,
) {
    val context = LocalContext.current
    val categoryUris = items.map { item -> item.uri }.toSet()
    val allSelected = categoryUris.isNotEmpty() && categoryUris.all { uri -> uri in selectedUris }
    val categoryBytes = Formatter.formatFileSize(context, items.sumOf { item -> item.sizeBytes })

    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = allSelected,
                    onCheckedChange = { checked ->
                        onSetCategorySelected(category, checked)
                    },
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = categoryLabel(category),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(
                            R.string.category_result_summary,
                            categoryBytes,
                            items.size,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { onToggleCategory(category) }) {
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Outlined.ExpandLess
                        } else {
                            Icons.Outlined.ExpandMore
                        },
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultItemRow(
    item: FileItem,
    selected: Boolean,
    onToggleItem: (String) -> Unit,
) {
    val context = LocalContext.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = selected,
                    onCheckedChange = { onToggleItem(item.uri) },
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.displayName,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = item.source.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = Formatter.formatFileSize(context, item.sizeBytes),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun categoryLabel(category: JunkCategory): String =
    stringResource(
        when (category) {
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
