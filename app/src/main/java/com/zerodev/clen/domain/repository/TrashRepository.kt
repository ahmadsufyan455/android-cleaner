package com.zerodev.clen.domain.repository

import com.zerodev.clen.domain.model.TrashEntry
import kotlinx.coroutines.flow.Flow

interface TrashRepository {
    fun observeTrash(): Flow<List<TrashEntry>>

    suspend fun restore(entry: TrashEntry): Boolean

    suspend fun emptyTrash(): Int

    suspend fun purgeExpired(nowMillis: Long = System.currentTimeMillis()): Int
}
