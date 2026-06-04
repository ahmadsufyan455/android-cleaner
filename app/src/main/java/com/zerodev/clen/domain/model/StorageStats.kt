package com.zerodev.clen.domain.model

data class StorageStats(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
) {
    val usedPercent: Float =
        if (totalBytes <= 0L) 0f else usedBytes.toFloat() / totalBytes.toFloat()
}
