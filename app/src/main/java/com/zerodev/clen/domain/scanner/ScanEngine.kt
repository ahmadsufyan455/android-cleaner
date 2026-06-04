package com.zerodev.clen.domain.scanner

import com.zerodev.clen.domain.model.ScanProgress
import kotlinx.coroutines.flow.Flow

interface ScanEngine {
    fun scan(): Flow<ScanProgress>
}
