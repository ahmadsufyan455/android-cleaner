package com.zerodev.clen.data.repository

import android.content.Context
import android.net.Uri
import com.zerodev.clen.core.dispatcher.IoDispatcher
import com.zerodev.clen.data.local.dao.TrashEntryDao
import com.zerodev.clen.data.local.toDomain
import com.zerodev.clen.domain.model.TrashEntry
import com.zerodev.clen.domain.repository.TrashRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class TrashRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val trashEntryDao: TrashEntryDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TrashRepository {
    override fun observeTrash(): Flow<List<TrashEntry>> =
        trashEntryDao.observeAll().map { entries -> entries.map { it.toDomain() } }

    override suspend fun restore(entry: TrashEntry): Boolean = withContext(ioDispatcher) {
        if (!entry.restorable) return@withContext false
        val cachedFile = File(entry.cachedPath)
        if (!cachedFile.exists()) return@withContext false

        val restored = runCatching {
            context.contentResolver.openOutputStream(Uri.parse(entry.originalUri))?.use { output ->
                cachedFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } != null
        }.getOrDefault(false)

        if (restored) {
            cachedFile.delete()
            trashEntryDao.deleteById(entry.id)
        }
        restored
    }

    override suspend fun emptyTrash(): Int = withContext(ioDispatcher) {
        val entries = trashEntryDao.getAll()
        entries.forEach { entry -> File(entry.cachedPath).delete() }
        trashEntryDao.deleteAll()
        entries.size
    }

    override suspend fun purgeExpired(nowMillis: Long): Int = withContext(ioDispatcher) {
        val expired = trashEntryDao.getExpired(nowMillis)
        expired.forEach { entry -> File(entry.cachedPath).delete() }
        trashEntryDao.deleteExpired(nowMillis)
        expired.size
    }
}
