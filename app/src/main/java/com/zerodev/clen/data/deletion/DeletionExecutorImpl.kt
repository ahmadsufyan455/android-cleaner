package com.zerodev.clen.data.deletion

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import com.zerodev.clen.core.dispatcher.IoDispatcher
import com.zerodev.clen.data.local.dao.FileItemDao
import com.zerodev.clen.data.local.dao.TrashEntryDao
import com.zerodev.clen.data.local.entity.TrashEntryEntity
import com.zerodev.clen.data.system.OwnCacheDataSource
import com.zerodev.clen.domain.deletion.DeletionExecutor
import com.zerodev.clen.domain.model.DeleteOutcome
import com.zerodev.clen.domain.model.DeleteOutcomeStatus
import com.zerodev.clen.domain.model.DeleteRequest
import com.zerodev.clen.domain.model.DeleteResult
import com.zerodev.clen.domain.model.FileItem
import com.zerodev.clen.domain.model.JunkCategory
import com.zerodev.clen.domain.model.ScanSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class DeletionExecutorImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val ownCacheDataSource: OwnCacheDataSource,
    private val fileItemDao: FileItemDao,
    private val trashEntryDao: TrashEntryDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : DeletionExecutor {
    override suspend fun delete(request: DeleteRequest): DeleteResult = withContext(ioDispatcher) {
        val outcomes = request.items.map { item ->
            runCatching {
                when {
                    item.category == JunkCategory.OWN_CACHE -> deleteOwnCache(item)
                    item.source == ScanSource.SAF_TREE -> deleteSafItem(item, request.allowPermanentDelete)
                    item.source == ScanSource.MEDIASTORE || item.source == ScanSource.DOWNLOADS ->
                        deleteContentItem(item, request.allowPermanentDelete)
                    else -> skip(item, "This item source is not deletable yet.")
                }
            }.getOrElse { throwable ->
                DeleteOutcome(
                    fileItem = item,
                    status = DeleteOutcomeStatus.FAILED,
                    message = throwable.message ?: "Delete failed.",
                )
            }
        }

        val deletedUris = outcomes
            .filter { outcome ->
                outcome.status == DeleteOutcomeStatus.RESTORABLE_DELETED ||
                    outcome.status == DeleteOutcomeStatus.PERMANENT_DELETED
            }
            .map { outcome -> outcome.fileItem.uri }

        if (deletedUris.isNotEmpty()) {
            fileItemDao.deleteByUris(deletedUris)
        }

        DeleteResult(outcomes)
    }

    private fun deleteOwnCache(item: FileItem): DeleteOutcome {
        val result = ownCacheDataSource.clearCache()
        return DeleteOutcome(
            fileItem = item,
            status = DeleteOutcomeStatus.PERMANENT_DELETED,
            bytesDeleted = result.bytesDeleted,
        )
    }

    private suspend fun deleteSafItem(item: FileItem, allowPermanentDelete: Boolean): DeleteOutcome {
        val uri = Uri.parse(item.uri)
        val isDirectory = item.mimeType == DocumentsContract.Document.MIME_TYPE_DIR
        if (isDirectory && !allowPermanentDelete) {
            return skip(item, "Empty folders cannot be restored from trash.")
        }

        val trashEntry = if (isDirectory) null else copyToTrash(item)
        val deleted = DocumentsContract.deleteDocument(context.contentResolver, uri)
        if (!deleted) {
            return DeleteOutcome(
                fileItem = item,
                status = DeleteOutcomeStatus.FAILED,
                message = "Android did not allow this SAF deletion.",
            )
        }

        return if (trashEntry != null) {
            DeleteOutcome(
                fileItem = item,
                status = DeleteOutcomeStatus.RESTORABLE_DELETED,
                bytesDeleted = item.sizeBytes,
                trashEntryId = trashEntry.id,
            )
        } else {
            DeleteOutcome(
                fileItem = item,
                status = DeleteOutcomeStatus.PERMANENT_DELETED,
                bytesDeleted = item.sizeBytes,
            )
        }
    }

    private suspend fun deleteContentItem(item: FileItem, allowPermanentDelete: Boolean): DeleteOutcome {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return DeleteOutcome(
                fileItem = item,
                status = DeleteOutcomeStatus.PLATFORM_CONFIRMATION_REQUIRED,
                message = "Android requires platform confirmation for this file.",
            )
        }

        if (!allowPermanentDelete) {
            return skip(item, "Shared-storage files need explicit permanent-delete confirmation.")
        }

        val trashEntry = copyToTrash(item)
        val deletedRows = context.contentResolver.delete(Uri.parse(item.uri), null, null)
        return if (deletedRows > 0) {
            DeleteOutcome(
                fileItem = item,
                status = DeleteOutcomeStatus.RESTORABLE_DELETED,
                bytesDeleted = item.sizeBytes,
                trashEntryId = trashEntry.id,
            )
        } else {
            DeleteOutcome(
                fileItem = item,
                status = DeleteOutcomeStatus.FAILED,
                message = "Android did not delete this file.",
            )
        }
    }

    private suspend fun copyToTrash(item: FileItem): TrashEntryEntity {
        val id = UUID.randomUUID().toString()
        val trashDir = File(context.cacheDir, TRASH_DIRECTORY).also { directory ->
            directory.mkdirs()
        }
        val cachedFile = File(trashDir, id)
        context.contentResolver.openInputStream(Uri.parse(item.uri))?.use { input ->
            cachedFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to read file before deletion.")

        val now = System.currentTimeMillis()
        val entry = TrashEntryEntity(
            id = id,
            originalUri = item.uri,
            cachedPath = cachedFile.absolutePath,
            sizeBytes = item.sizeBytes,
            deletedAt = now,
            expiresAt = now + TRASH_RETENTION_MILLIS,
            restorable = true,
        )
        return entry.also { trashEntryDao.upsert(it) }
    }

    private fun skip(item: FileItem, message: String): DeleteOutcome = DeleteOutcome(
        fileItem = item,
        status = DeleteOutcomeStatus.SKIPPED,
        message = message,
    )

    private companion object {
        const val TRASH_DIRECTORY = "trash"
        const val TRASH_RETENTION_MILLIS = 7L * 24L * 60L * 60L * 1_000L
    }
}
