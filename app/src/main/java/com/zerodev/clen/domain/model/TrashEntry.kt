package com.zerodev.clen.domain.model

data class TrashEntry(
    val id: String,
    val originalUri: String,
    val cachedPath: String,
    val sizeBytes: Long,
    val deletedAt: Long,
    val expiresAt: Long,
    val restorable: Boolean,
)
