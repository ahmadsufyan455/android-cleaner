package com.zerodev.clen.domain.model

data class ScanRun(
    val id: Long,
    val startedAt: Long,
    val finishedAt: Long?,
    val totalBytesFound: Long,
    val status: ScanStatus,
)
