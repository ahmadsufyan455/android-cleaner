package com.zerodev.clen.domain.model

data class ScanProgress(
    val status: ScanStatus,
    val currentCategory: JunkCategory?,
    val scannedSources: Set<ScanSource> = emptySet(),
    val scannedItemCount: Int = 0,
    val foundItemCount: Int = 0,
    val totalBytesFound: Long = 0L,
    val items: List<FileItem> = emptyList(),
    val message: String? = null,
)
