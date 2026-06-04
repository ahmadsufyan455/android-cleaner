package com.zerodev.clen.data.local

import com.zerodev.clen.data.local.entity.TrashEntryEntity
import com.zerodev.clen.domain.model.TrashEntry

fun TrashEntryEntity.toDomain(): TrashEntry = TrashEntry(
    id = id,
    originalUri = originalUri,
    cachedPath = cachedPath,
    sizeBytes = sizeBytes,
    deletedAt = deletedAt,
    expiresAt = expiresAt,
    restorable = restorable,
)

fun TrashEntry.toEntity(): TrashEntryEntity = TrashEntryEntity(
    id = id,
    originalUri = originalUri,
    cachedPath = cachedPath,
    sizeBytes = sizeBytes,
    deletedAt = deletedAt,
    expiresAt = expiresAt,
    restorable = restorable,
)
