package com.zerodev.clen.presentation.scan

import com.zerodev.clen.domain.model.FileItem
import com.zerodev.clen.domain.model.ScanRun

data class ScanResultsState(
    val latestRun: ScanRun? = null,
    val items: List<FileItem> = emptyList(),
)
