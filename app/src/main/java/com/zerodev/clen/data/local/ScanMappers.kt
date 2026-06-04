package com.zerodev.clen.data.local

import com.zerodev.clen.data.local.entity.FileItemEntity
import com.zerodev.clen.data.local.entity.ScanRunEntity
import com.zerodev.clen.domain.model.FileItem
import com.zerodev.clen.domain.model.ScanRun
import com.zerodev.clen.domain.model.ScanSource

fun FileItemEntity.toDomain(): FileItem = FileItem(
    uri = uri,
    displayName = displayName,
    sizeBytes = sizeBytes,
    mimeType = mimeType,
    lastModified = lastModified,
    category = category,
    source = ScanSource.APP_PRIVATE,
    sha256 = sha256,
    perceptualHash = perceptualHash,
    whitelisted = whitelisted,
)

fun FileItem.toEntity(): FileItemEntity = FileItemEntity(
    uri = uri,
    displayName = displayName,
    sizeBytes = sizeBytes,
    mimeType = mimeType,
    lastModified = lastModified,
    category = category,
    sha256 = sha256,
    perceptualHash = perceptualHash,
    whitelisted = whitelisted,
)

fun ScanRunEntity.toDomain(): ScanRun = ScanRun(
    id = id,
    startedAt = startedAt,
    finishedAt = finishedAt,
    totalBytesFound = totalBytesFound,
    status = status,
)
