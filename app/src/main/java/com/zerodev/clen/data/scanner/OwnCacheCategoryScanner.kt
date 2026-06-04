package com.zerodev.clen.data.scanner

import android.content.Context
import com.zerodev.clen.data.system.OwnCacheDataSource
import com.zerodev.clen.domain.model.FileItem
import com.zerodev.clen.domain.model.JunkCategory
import com.zerodev.clen.domain.model.ScanSource
import com.zerodev.clen.domain.scanner.CategoryScanner
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class OwnCacheCategoryScanner @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val ownCacheDataSource: OwnCacheDataSource,
) : CategoryScanner {
    override val category: JunkCategory = JunkCategory.OWN_CACHE
    override val source: ScanSource = ScanSource.APP_PRIVATE

    override suspend fun scan(): List<FileItem> {
        val cacheSizeBytes = ownCacheDataSource.getCacheSizeBytes()
        if (cacheSizeBytes <= 0L) return emptyList()

        return listOf(
            FileItem(
                uri = context.cacheDir.toFileUriString(),
                displayName = "Clen cache",
                sizeBytes = cacheSizeBytes,
                mimeType = null,
                lastModified = context.cacheDir.lastModified(),
                category = category,
                source = source,
            ),
        )
    }

    private fun File.toFileUriString(): String = toURI().toString()
}
