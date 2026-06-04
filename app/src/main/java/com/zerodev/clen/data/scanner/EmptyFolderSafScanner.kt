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

class EmptyFolderSafScanner @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CategoryScanner {
    override val category: JunkCategory = JunkCategory.EMPTY_FOLDER
    override val source: ScanSource = ScanSource.SAF_TREE

    override suspend fun scan(): List<FileItem> = withContext(ioDispatcher) {
        settingsRepository.settings.first().safTreeUris
            .flatMap { uriString -> scanTree(Uri.parse(uriString)) }
    }

    private fun scanTree(treeUri: Uri): List<FileItem> {
        val rootDocumentId = DocumentsContract.getTreeDocumentId(treeUri)
        val rootDocumentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, rootDocumentId)
        val emptyFolders = mutableListOf<FileItem>()
        visitDirectory(treeUri, rootDocumentUri, emptyFolders)
        return emptyFolders.sortedBy { it.displayName }
    }

    private fun visitDirectory(
        treeUri: Uri,
        directoryUri: Uri,
        emptyFolders: MutableList<FileItem>,
    ): Boolean {
        val directoryDocumentId = DocumentsContract.getDocumentId(directoryUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            directoryDocumentId,
        )
        val children = context.contentResolver.query(
            childrenUri,
            DOCUMENT_PROJECTION,
            null,
            null,
            null,
        )?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        SafDocument(
                            documentId = cursor.getString(0),
                            displayName = cursor.getString(1) ?: "Folder",
                            mimeType = cursor.getString(2),
                            lastModified = cursor.getLong(3),
                        ),
                    )
                }
            }
        }.orEmpty()

        val childDirectories = children.filter {
            it.mimeType == DocumentsContract.Document.MIME_TYPE_DIR
        }
        val nonDirectoryChildren = children.size - childDirectories.size
        val childDirectoriesEmpty = childDirectories.map { child ->
            val childUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, child.documentId)
            val childIsEmpty = visitDirectory(treeUri, childUri, emptyFolders)
            child to childIsEmpty
        }

        val isEmpty = nonDirectoryChildren == 0 && childDirectoriesEmpty.all { it.second }
        if (isEmpty) {
            emptyFolders += FileItem(
                uri = directoryUri.toString(),
                displayName = children.firstOrNull()?.displayName ?: directoryDocumentId,
                sizeBytes = 0L,
                mimeType = DocumentsContract.Document.MIME_TYPE_DIR,
                lastModified = children.maxOfOrNull { it.lastModified } ?: 0L,
                category = category,
                source = source,
            )
        }

        return isEmpty
    }

    private data class SafDocument(
        val documentId: String,
        val displayName: String,
        val mimeType: String?,
        val lastModified: Long,
    )

    private companion object {
        val DOCUMENT_PROJECTION = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        )
    }
}
