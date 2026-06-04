package com.zerodev.clen.data.system

import android.content.Context
import android.os.StatFs
import com.zerodev.clen.domain.model.StorageStats
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StorageProbe @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun getPrimaryStorageStats(): StorageStats {
        val statFs = StatFs(context.dataDir.absolutePath)
        val totalBytes = statFs.blockCountLong * statFs.blockSizeLong
        val freeBytes = statFs.availableBlocksLong * statFs.blockSizeLong
        val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)

        return StorageStats(
            totalBytes = totalBytes,
            usedBytes = usedBytes,
            freeBytes = freeBytes,
        )
    }
}
