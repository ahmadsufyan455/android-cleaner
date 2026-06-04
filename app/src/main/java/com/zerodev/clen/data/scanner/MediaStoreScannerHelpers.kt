package com.zerodev.clen.data.scanner

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.zerodev.clen.domain.model.FileItem
import com.zerodev.clen.domain.model.JunkCategory
import com.zerodev.clen.domain.model.ScanSource

internal object MediaStoreScannerHelpers {
    val filesUri: Uri = MediaStore.Files.getContentUri("external")

    val projection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.SIZE,
        MediaStore.Files.FileColumns.MIME_TYPE,
        MediaStore.Files.FileColumns.DATE_MODIFIED,
        MediaStore.Files.FileColumns.MEDIA_TYPE,
    )

    fun ContentResolver.queryFiles(
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?,
    ): List<MediaStoreFile> {
        val results = mutableListOf<MediaStoreFile>()
        query(filesUri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            while (cursor.moveToNext()) {
                results += cursor.toMediaStoreFile()
            }
        }
        return results
    }

    fun MediaStoreFile.toFileItem(
        category: JunkCategory,
        source: ScanSource,
    ): FileItem = FileItem(
        uri = uri.toString(),
        displayName = displayName,
        sizeBytes = sizeBytes,
        mimeType = mimeType,
        lastModified = lastModifiedMillis,
        category = category,
        source = source,
    )

    private fun Cursor.toMediaStoreFile(): MediaStoreFile {
        val id = getLong(getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
        val displayName = getString(getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME))
            ?: "Unknown file"
        val sizeBytes = getLong(getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE))
            .coerceAtLeast(0L)
        val mimeType = getString(getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))
        val lastModifiedSeconds = getLong(
            getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED),
        )

        return MediaStoreFile(
        uri = ContentUris.withAppendedId(cursorMediaUri(), id),
        displayName = displayName,
        sizeBytes = sizeBytes,
        mimeType = mimeType,
        lastModifiedMillis = lastModifiedSeconds * 1_000L,
    )
    }

    private fun Cursor.cursorMediaUri(): Uri {
        val mediaType = getInt(getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE))
        return when (mediaType) {
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE ->
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO ->
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO ->
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            else -> filesUri
        }
    }
}

internal data class MediaStoreFile(
    val uri: Uri,
    val displayName: String,
    val sizeBytes: Long,
    val mimeType: String?,
    val lastModifiedMillis: Long,
)
