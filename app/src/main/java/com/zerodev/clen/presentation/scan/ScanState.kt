package com.zerodev.clen.presentation.scan

import com.zerodev.clen.domain.model.JunkCategory
import com.zerodev.clen.domain.model.ScanStatus

data class ScanState(
    val isScanning: Boolean = false,
    val mediaPermissions: List<String> = emptyList(),
    val status: ScanStatus? = null,
    val currentCategory: JunkCategory? = null,
    val foundItemCount: Int = 0,
    val totalBytesFound: Long = 0L,
    val hasAnyMediaAccess: Boolean = false,
    val hasFullMediaAccess: Boolean = false,
    val progressMessage: String? = null,
    val errorMessageResId: Int? = null,
)
