package com.zerodev.clen.domain.model

data class ClearOwnCacheResult(
    val bytesBefore: Long,
    val bytesDeleted: Long,
    val remainingBytes: Long,
)
