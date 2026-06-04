package com.zerodev.clen.presentation.scan

import com.zerodev.clen.domain.model.FileItem
import com.zerodev.clen.domain.model.ScanRun

data class ScanResultsState(
    val latestRun: ScanRun? = null,
    val items: List<FileItem> = emptyList(),
    val selectedUris: Set<String> = emptySet(),
    val expandedCategories: Set<com.zerodev.clen.domain.model.JunkCategory> = emptySet(),
    val isCleaning: Boolean = false,
    val cleanMessage: String? = null,
    val errorMessage: String? = null,
    val showPermanentDeleteWarning: Boolean = false,
    val pendingMediaDeleteRequest: MediaDeleteRequest? = null,
    val selectedBytes: Long = items
        .filter { item -> item.uri in selectedUris }
        .sumOf { item -> item.sizeBytes },
)
