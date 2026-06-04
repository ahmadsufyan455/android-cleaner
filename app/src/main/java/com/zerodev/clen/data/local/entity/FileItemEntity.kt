package com.zerodev.clen.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zerodev.clen.domain.model.JunkCategory
import com.zerodev.clen.domain.model.ScanSource

@Entity(tableName = "file_items")
data class FileItemEntity(
    @PrimaryKey val uri: String,
    val displayName: String,
    val sizeBytes: Long,
    val mimeType: String?,
    val lastModified: Long,
    val category: JunkCategory,
    val source: ScanSource = ScanSource.APP_PRIVATE,
    val sha256: String?,
    val perceptualHash: Long?,
    val whitelisted: Boolean = false,
)
