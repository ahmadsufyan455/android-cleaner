package com.zerodev.clen.domain.model

data class DeleteResult(
    val outcomes: List<DeleteOutcome>,
) {
    val bytesDeleted: Long = outcomes.sumOf { outcome -> outcome.bytesDeleted }
    val successfulCount: Int = outcomes.count { outcome ->
        outcome.status == DeleteOutcomeStatus.RESTORABLE_DELETED ||
            outcome.status == DeleteOutcomeStatus.PERMANENT_DELETED
    }
    val failedCount: Int = outcomes.count { outcome -> outcome.status == DeleteOutcomeStatus.FAILED }
    val platformConfirmationCount: Int = outcomes.count { outcome ->
        outcome.status == DeleteOutcomeStatus.PLATFORM_CONFIRMATION_REQUIRED
    }
}
