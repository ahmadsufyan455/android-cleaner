package com.zerodev.clen.domain.model

data class DeleteOutcome(
    val fileItem: FileItem,
    val status: DeleteOutcomeStatus,
    val bytesDeleted: Long = 0L,
    val trashEntryId: String? = null,
    val message: String? = null,
)
