package com.zerodev.clen.data.system

import android.content.Context
import com.zerodev.clen.domain.model.ClearOwnCacheResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OwnCacheDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val fileTreeOperations: FileTreeOperations,
) {
    fun getCacheSizeBytes(): Long = fileTreeOperations.sizeBytes(context.cacheDir)

    fun clearCache(): ClearOwnCacheResult {
        val sizeBeforeBytes = getCacheSizeBytes()
        val bytesDeleted = fileTreeOperations.deleteContents(context.cacheDir)
        val remainingBytes = getCacheSizeBytes()

        return ClearOwnCacheResult(
            bytesBefore = sizeBeforeBytes,
            bytesDeleted = bytesDeleted,
            remainingBytes = remainingBytes,
        )
    }
}
