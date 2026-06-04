package com.zerodev.clen.domain.repository

import com.zerodev.clen.domain.model.StorageStats

interface StorageRepository {
    fun getPrimaryStorageStats(): StorageStats
}
