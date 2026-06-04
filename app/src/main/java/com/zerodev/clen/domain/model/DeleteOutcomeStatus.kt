package com.zerodev.clen.domain.model

enum class DeleteOutcomeStatus {
    RESTORABLE_DELETED,
    PERMANENT_DELETED,
    PLATFORM_CONFIRMATION_REQUIRED,
    SKIPPED,
    FAILED,
}
