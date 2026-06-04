package com.zerodev.clen.data.repository

import com.zerodev.clen.data.system.StorageProbe
import com.zerodev.clen.domain.model.StorageStats
import com.zerodev.clen.domain.repository.StorageRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val storageProbe: StorageProbe,
) : StorageRepository {
    override fun getPrimaryStorageStats(): StorageStats =
        storageProbe.getPrimaryStorageStats()
}
