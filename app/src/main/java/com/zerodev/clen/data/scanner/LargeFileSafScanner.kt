package com.zerodev.clen.data.scanner

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.zerodev.clen.core.dispatcher.IoDispatcher
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

class LargeFileSafScanner @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CategoryScanner {
    override val category: JunkCategory = JunkCategory.LARGE_FILE
    override val source: ScanSource = ScanSource.SAF_TREE

    override suspend fun scan(): List<FileItem> = withContext(ioDispatcher) {
        val settings = settingsRepository.settings.first()
        settings.safTreeUris
            .flatMap { uriString -> scanTree(Uri.parse(uriString), settings.largeFileThresholdBytes) }
            .sortedByDescending { it.sizeBytes }
    }

    private fun scanTree(treeUri: Uri, thresholdBytes: Long): List<FileItem> {
        val rootDocumentId = DocumentsContract.getTreeDocumentId(treeUri)
        val rootDocumentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, rootDocumentId)
        val largeFiles = mutableListOf<FileItem>()
        visitDocument(treeUri, rootDocumentUri, thresholdBytes, largeFiles)
        return largeFiles
    }

    private fun visitDocument(
        treeUri: Uri,
        documentUri: Uri,
        thresholdBytes: Long,
        largeFiles: MutableList<FileItem>,
    ) {
        val documentId = DocumentsContract.getDocumentId(documentUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
        context.contentResolver.query(
            childrenUri,
            DOCUMENT_PROJECTION,
            null,
            null,
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val childDocumentId = cursor.getString(0)
                val displayName = cursor.getString(1) ?: "Unknown file"
                val mimeType = cursor.getString(2)
                val sizeBytes = cursor.getLong(3).coerceAtLeast(0L)
                val lastModified = cursor.getLong(4)
                val childUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childDocumentId)

                if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                    visitDocument(treeUri, childUri, thresholdBytes, largeFiles)
                } else if (sizeBytes >= thresholdBytes) {
                    largeFiles += FileItem(
                        uri = childUri.toString(),
                        displayName = displayName,
                        sizeBytes = sizeBytes,
                        mimeType = mimeType,
                        lastModified = lastModified,
                        category = category,
                        source = source,
                    )
                }
            }
        }
    }

    private companion object {
        val DOCUMENT_PROJECTION = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        )
    }
}
