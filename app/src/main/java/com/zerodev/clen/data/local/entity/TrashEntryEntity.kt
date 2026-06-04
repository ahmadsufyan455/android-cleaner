package com.zerodev.clen.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trash_entries")
data class TrashEntryEntity(
    @PrimaryKey val id: String,
    val originalUri: String,
    val cachedPath: String,
    val sizeBytes: Long,
    val deletedAt: Long,
    val expiresAt: Long,
    val restorable: Boolean,
)
