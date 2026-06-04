package com.zerodev.clen.domain.repository

import com.zerodev.clen.domain.model.FileItem
import com.zerodev.clen.domain.model.ScanProgress
import com.zerodev.clen.domain.model.ScanRun
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    fun observeLatestRun(): Flow<ScanRun?>

    fun observeResults(): Flow<List<FileItem>>

    fun runScan(): Flow<ScanProgress>
}
