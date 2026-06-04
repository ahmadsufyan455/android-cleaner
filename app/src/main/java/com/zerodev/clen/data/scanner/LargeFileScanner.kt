package com.zerodev.clen.data.scanner

import android.content.Context
import android.provider.MediaStore
import com.zerodev.clen.core.dispatcher.IoDispatcher
import com.zerodev.clen.data.permission.MediaPermissionCoordinator
import com.zerodev.clen.data.scanner.MediaStoreScannerHelpers.queryFiles
import com.zerodev.clen.data.scanner.MediaStoreScannerHelpers.toFileItem
import com.zerodev.clen.domain.model.FileItem
import com.zerodev.clen.domain.model.JunkCategory
import com.zerodev.clen.domain.model.ScanSource
import com.zerodev.clen.domain.repository.SettingsRepository
import com.zerodev.clen.domain.scanner.CategoryScanner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class LargeFileScanner @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val mediaPermissionCoordinator: MediaPermissionCoordinator,
    private val settingsRepository: SettingsRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CategoryScanner {
    override val category: JunkCategory = JunkCategory.LARGE_FILE
    override val source: ScanSource = ScanSource.MEDIASTORE

    override suspend fun scan(): List<FileItem> = withContext(ioDispatcher) {
        if (!mediaPermissionCoordinator.hasAnyMediaReadAccess()) return@withContext emptyList()

        val thresholdBytes = settingsRepository.settings.first().largeFileThresholdBytes
        context.contentResolver.queryFiles(
            selection = "${MediaStore.Files.FileColumns.SIZE} >= ?",
            selectionArgs = arrayOf(thresholdBytes.toString()),
            sortOrder = "${MediaStore.Files.FileColumns.SIZE} DESC",
        ).map { it.toFileItem(category = category, source = source) }
    }
}
