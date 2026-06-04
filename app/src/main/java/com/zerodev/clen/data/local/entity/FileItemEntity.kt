package com.zerodev.clen.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zerodev.clen.domain.model.JunkCategory

@Entity(tableName = "file_items")
data class FileItemEntity(
    @PrimaryKey val uri: String,
    val displayName: String,
    val sizeBytes: Long,
    val mimeType: String?,
    val lastModified: Long,
    val category: JunkCategory,
    val sha256: String?,
    val perceptualHash: Long?,
    val whitelisted: Boolean = false,
)
