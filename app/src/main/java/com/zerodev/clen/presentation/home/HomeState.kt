package com.zerodev.clen.presentation.home

data class HomeState(
    val cacheSizeBytes: Long = 0L,
    val storageTotalBytes: Long = 0L,
    val storageUsedBytes: Long = 0L,
    val storageFreeBytes: Long = 0L,
    val latestScanBytes: Long = 0L,
    val latestScanItemCount: Int = 0,
    val lastScanAtMillis: Long? = null,
    val lastCleanAtMillis: Long? = null,
    val isLoadingCacheSize: Boolean = true,
    val isLoadingStorageStats: Boolean = true,
    val isClearingCache: Boolean = false,
    val lastClearedBytes: Long? = null,
    val errorMessageResId: Int? = null,
)
