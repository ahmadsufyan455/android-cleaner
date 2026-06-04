package com.zerodev.clen.domain.scanner

import com.zerodev.clen.domain.model.FileItem
import com.zerodev.clen.domain.model.JunkCategory
import com.zerodev.clen.domain.model.ScanSource

interface CategoryScanner {
    val category: JunkCategory
    val source: ScanSource

    suspend fun scan(): List<FileItem>
}
