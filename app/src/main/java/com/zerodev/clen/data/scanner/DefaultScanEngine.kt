package com.zerodev.clen.data.scanner

import com.zerodev.clen.domain.model.FileItem
import com.zerodev.clen.domain.model.ScanProgress
import com.zerodev.clen.domain.model.ScanStatus
import com.zerodev.clen.domain.scanner.CategoryScanner
import com.zerodev.clen.domain.scanner.ScanEngine
import javax.inject.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DefaultScanEngine @Inject constructor(
    private val scanners: Set<@JvmSuppressWildcards CategoryScanner>,
) : ScanEngine {
    override fun scan(): Flow<ScanProgress> = flow {
        val foundItems = mutableListOf<FileItem>()
        val scannedSources = mutableSetOf<com.zerodev.clen.domain.model.ScanSource>()
        var scannedItemCount = 0

        emit(
            ScanProgress(
                status = ScanStatus.RUNNING,
                currentCategory = null,
            ),
        )

        scanners.sortedBy { it.category.name }.forEach { scanner ->
            currentCoroutineContext().ensureActive()
            scannedSources += scanner.source
            emit(
                ScanProgress(
                    status = ScanStatus.RUNNING,
                    currentCategory = scanner.category,
                    scannedSources = scannedSources,
                    scannedItemCount = scannedItemCount,
                    foundItemCount = foundItems.size,
                    totalBytesFound = foundItems.sumOf { it.sizeBytes },
                    items = foundItems.toList(),
                ),
            )

            val categoryItems = scanner.scan()
            scannedItemCount += categoryItems.size
            foundItems += categoryItems

            emit(
                ScanProgress(
                    status = ScanStatus.RUNNING,
                    currentCategory = scanner.category,
                    scannedSources = scannedSources,
                    scannedItemCount = scannedItemCount,
                    foundItemCount = foundItems.size,
                    totalBytesFound = foundItems.sumOf { it.sizeBytes },
                    items = foundItems.toList(),
                ),
            )
        }

        emit(
            ScanProgress(
                status = ScanStatus.COMPLETED,
                currentCategory = null,
                scannedSources = scannedSources,
                scannedItemCount = scannedItemCount,
                foundItemCount = foundItems.size,
                totalBytesFound = foundItems.sumOf { it.sizeBytes },
                items = foundItems.toList(),
            ),
        )
    }
}
