package com.zerodev.clen.domain.model

data class FileItem(
    val uri: String,
    val displayName: String,
    val sizeBytes: Long,
    val mimeType: String?,
    val lastModified: Long,
    val category: JunkCategory,
    val source: ScanSource,
    val sha256: String? = null,
    val perceptualHash: Long? = null,
    val whitelisted: Boolean = false,
)
