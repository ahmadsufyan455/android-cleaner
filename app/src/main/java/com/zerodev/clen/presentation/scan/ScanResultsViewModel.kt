package com.zerodev.clen.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerodev.clen.domain.deletion.DeletionExecutor
import com.zerodev.clen.data.deletion.MediaStoreDeleteRequestFactory
import com.zerodev.clen.domain.model.DeleteOutcomeStatus
import com.zerodev.clen.domain.model.DeleteRequest
import com.zerodev.clen.domain.model.JunkCategory
import com.zerodev.clen.domain.repository.ScanRepository
import com.zerodev.clen.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ScanResultsViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val settingsRepository: SettingsRepository,
    private val deletionExecutor: DeletionExecutor,
    private val mediaStoreDeleteRequestFactory: MediaStoreDeleteRequestFactory,
) : ViewModel() {
    private val selectedUris = MutableStateFlow<Set<String>>(emptySet())
    private val expandedCategories = MutableStateFlow<Set<JunkCategory>>(emptySet())
    private val cleanMessage = MutableStateFlow<String?>(null)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val showPermanentDeleteWarning = MutableStateFlow(false)
    private val isCleaning = MutableStateFlow(false)
    private val pendingMediaDeleteRequest = MutableStateFlow<MediaDeleteRequest?>(null)

    val state: StateFlow<ScanResultsState> = combine(
        scanRepository.observeLatestRun(),
        scanRepository.observeResults(),
        selectedUris,
        expandedCategories,
        cleanMessage,
        errorMessage,
        showPermanentDeleteWarning,
        isCleaning,
        pendingMediaDeleteRequest,
    ) { values ->
        val latestRun = values[0] as com.zerodev.clen.domain.model.ScanRun?
        @Suppress("UNCHECKED_CAST")
        val items = values[1] as List<com.zerodev.clen.domain.model.FileItem>
        @Suppress("UNCHECKED_CAST")
        val selectedUris = values[2] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val expandedCategories = values[3] as Set<JunkCategory>
        val cleanMessage = values[4] as String?
        val errorMessage = values[5] as String?
        val showPermanentDeleteWarning = values[6] as Boolean
        val isCleaning = values[7] as Boolean
        val pendingMediaDeleteRequest = values[8] as MediaDeleteRequest?

        ScanResultsState(
            latestRun = latestRun,
            items = items,
            selectedUris = selectedUris.intersect(items.map { it.uri }.toSet()),
            expandedCategories = expandedCategories.ifEmpty {
                items.map { it.category }.toSet()
            },
            cleanMessage = cleanMessage,
            errorMessage = errorMessage,
            showPermanentDeleteWarning = showPermanentDeleteWarning,
            isCleaning = isCleaning,
            pendingMediaDeleteRequest = pendingMediaDeleteRequest,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = ScanResultsState(),
    )

    fun toggleItem(uri: String) {
        selectedUris.update { current ->
            if (uri in current) current - uri else current + uri
        }
        cleanMessage.value = null
        errorMessage.value = null
    }

    fun toggleCategory(category: JunkCategory) {
        expandedCategories.update { current ->
            if (category in current) current - category else current + category
        }
    }

    fun setCategorySelected(category: JunkCategory, selected: Boolean) {
        val categoryUris = state.value.items
            .filter { item -> item.category == category }
            .map { item -> item.uri }
            .toSet()

        selectedUris.update { current ->
            if (selected) current + categoryUris else current - categoryUris
        }
        cleanMessage.value = null
        errorMessage.value = null
    }

    fun whitelistSelected() {
        viewModelScope.launch {
            selectedUris.value.forEach { uri ->
                settingsRepository.addWhitelistedUri(uri)
            }
            cleanMessage.value = "Selected items will be ignored in future scans."
            selectedUris.value = emptySet()
        }
    }

    fun requestCleanSelected() {
        if (selectedUris.value.isEmpty()) return
        showPermanentDeleteWarning.value = true
    }

    fun dismissCleanConfirmation() {
        showPermanentDeleteWarning.value = false
    }

    fun confirmCleanSelected() {
        val selectedItems = state.value.items.filter { item -> item.uri in selectedUris.value }
        if (selectedItems.isEmpty()) return

        viewModelScope.launch {
            showPermanentDeleteWarning.value = false
            isCleaning.value = true
            cleanMessage.value = null
            errorMessage.value = null

            runCatching {
                deletionExecutor.delete(
                    DeleteRequest(
                        items = selectedItems,
                        allowPermanentDelete = true,
                    ),
                )
            }.onSuccess { result ->
                selectedUris.value = emptySet()
                cleanMessage.value = buildString {
                    append("${result.successfulCount} items cleaned")
                    if (result.bytesDeleted > 0L) {
                        append(".")
                    }
                if (result.platformConfirmationCount > 0) {
                        val platformUris = result.outcomes
                            .filter { outcome ->
                                outcome.status == DeleteOutcomeStatus.PLATFORM_CONFIRMATION_REQUIRED
                            }
                            .map { outcome -> outcome.fileItem.uri }
                        val pendingIntent = mediaStoreDeleteRequestFactory
                            .createDeleteRequest(platformUris)
                        if (pendingIntent != null) {
                            pendingMediaDeleteRequest.value = MediaDeleteRequest(
                                intentSender = pendingIntent.intentSender,
                                uris = platformUris,
                            )
                            append(" Android confirmation is required for ${platformUris.size} items.")
                        } else {
                            append(" ${result.platformConfirmationCount} items need Android confirmation.")
                        }
                    }
                    if (result.failedCount > 0) {
                        append(" ${result.failedCount} items failed.")
                    }
                    val skipped = result.outcomes.count { outcome ->
                        outcome.status == DeleteOutcomeStatus.SKIPPED
                    }
                    if (skipped > 0) {
                        append(" $skipped items skipped.")
                    }
                }
            }.onFailure { throwable ->
                errorMessage.value = throwable.message ?: "Unable to clean selected items."
            }

            isCleaning.value = false
        }
    }

    fun consumeMediaDeleteRequest() {
        pendingMediaDeleteRequest.value = null
    }

    fun onMediaDeleteResult(confirmed: Boolean, uris: List<String>) {
        viewModelScope.launch {
            if (confirmed) {
                scanRepository.removeResultsByUris(uris)
                selectedUris.update { current -> current - uris.toSet() }
                cleanMessage.value = "Android deleted ${uris.size} items."
            } else {
                cleanMessage.value = "Android delete confirmation was cancelled."
            }
        }
    }
}
