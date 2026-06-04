package com.zerodev.clen.domain.repository

import com.zerodev.clen.domain.model.ClearOwnCacheResult

interface OwnCacheRepository {
    suspend fun getCacheSizeBytes(): Long

    suspend fun clearCache(): ClearOwnCacheResult
}
