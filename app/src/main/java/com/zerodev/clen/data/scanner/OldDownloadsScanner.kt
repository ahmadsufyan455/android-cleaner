package com.zerodev.clen.data.scanner

import android.content.Context
import android.os.Build
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

class OldDownloadsScanner @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val mediaPermissionCoordinator: MediaPermissionCoordinator,
    private val settingsRepository: SettingsRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CategoryScanner {
    override val category: JunkCategory = JunkCategory.OLD_DOWNLOAD
    override val source: ScanSource = ScanSource.DOWNLOADS

    override suspend fun scan(): List<FileItem> = withContext(ioDispatcher) {
        if (!mediaPermissionCoordinator.hasAnyMediaReadAccess()) return@withContext emptyList()

        val oldFileThresholdDays = settingsRepository.settings.first().oldFileThresholdDays
        val olderThanSeconds = (System.currentTimeMillis() / 1_000L) -
            oldFileThresholdDays * 24L * 60L * 60L
        val pathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.MediaColumns.RELATIVE_PATH
        } else {
            MediaStore.MediaColumns.DATA
        }

        context.contentResolver.queryFiles(
            selection = "${MediaStore.Files.FileColumns.DATE_MODIFIED} <= ? AND $pathColumn LIKE ?",
            selectionArgs = arrayOf(olderThanSeconds.toString(), "%Download%"),
            sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} ASC",
        ).map { it.toFileItem(category = category, source = source) }
    }
}
