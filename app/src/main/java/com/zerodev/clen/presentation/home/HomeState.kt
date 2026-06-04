package com.zerodev.clen.presentation.home

data class HomeState(
    val cacheSizeBytes: Long = 0L,
    val isLoadingCacheSize: Boolean = true,
    val isClearingCache: Boolean = false,
    val lastClearedBytes: Long? = null,
    val errorMessage: String? = null,
)
