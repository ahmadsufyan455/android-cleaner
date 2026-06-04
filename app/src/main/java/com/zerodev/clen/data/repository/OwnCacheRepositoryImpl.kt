package com.zerodev.clen.data.repository

import com.zerodev.clen.data.system.OwnCacheDataSource
import com.zerodev.clen.domain.model.ClearOwnCacheResult
import com.zerodev.clen.domain.repository.OwnCacheRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OwnCacheRepositoryImpl @Inject constructor(
    private val ownCacheDataSource: OwnCacheDataSource,
) : OwnCacheRepository {
    override suspend fun getCacheSizeBytes(): Long = ownCacheDataSource.getCacheSizeBytes()

    override suspend fun clearCache(): ClearOwnCacheResult = ownCacheDataSource.clearCache()
}
