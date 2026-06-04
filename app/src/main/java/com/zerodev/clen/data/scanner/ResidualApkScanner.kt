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
import com.zerodev.clen.domain.scanner.CategoryScanner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ResidualApkScanner @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val mediaPermissionCoordinator: MediaPermissionCoordinator,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CategoryScanner {
    override val category: JunkCategory = JunkCategory.RESIDUAL_APK
    override val source: ScanSource = ScanSource.DOWNLOADS

    override suspend fun scan(): List<FileItem> = withContext(ioDispatcher) {
        if (!mediaPermissionCoordinator.hasAnyMediaReadAccess()) return@withContext emptyList()

        val nowMillis = System.currentTimeMillis()
        val files = context.contentResolver.queryFiles(
            selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?",
            selectionArgs = arrayOf(ScannerRules.APK_MIME_TYPE),
            sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} ASC",
        )

        files
            .asSequence()
            .filter {
                ScannerRules.isResidualApk(
                    displayName = it.displayName,
                    lastModifiedMillis = it.lastModifiedMillis,
                    nowMillis = nowMillis,
                )
            }
            .map { it.toFileItem(category = category, source = source) }
            .toList()
    }
}
