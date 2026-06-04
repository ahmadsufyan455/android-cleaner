package com.zerodev.clen.domain.model

data class DeleteRequest(
    val items: List<FileItem>,
    val allowPermanentDelete: Boolean,
)
