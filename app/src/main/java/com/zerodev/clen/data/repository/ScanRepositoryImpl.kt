package com.zerodev.clen.data.repository

import com.zerodev.clen.data.local.dao.FileItemDao
import com.zerodev.clen.data.local.dao.ScanRunDao
import com.zerodev.clen.data.local.entity.ScanRunEntity
import com.zerodev.clen.data.local.toDomain
import com.zerodev.clen.data.local.toEntity
import com.zerodev.clen.domain.model.FileItem
import com.zerodev.clen.domain.model.ScanProgress
import com.zerodev.clen.domain.model.ScanRun
import com.zerodev.clen.domain.model.ScanStatus
import com.zerodev.clen.domain.repository.ScanRepository
import com.zerodev.clen.domain.repository.SettingsRepository
import com.zerodev.clen.domain.scanner.ScanEngine
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

@Singleton
class ScanRepositoryImpl @Inject constructor(
    private val scanEngine: ScanEngine,
    private val scanRunDao: ScanRunDao,
    private val fileItemDao: FileItemDao,
    private val settingsRepository: SettingsRepository,
) : ScanRepository {
    override fun observeLatestRun(): Flow<ScanRun?> =
        scanRunDao.observeLatest().map { entity -> entity?.toDomain() }

    override fun observeResults(): Flow<List<FileItem>> =
        fileItemDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun runScan(): Flow<ScanProgress> {
        var scanRun: ScanRunEntity? = null
        var latestProgress = ScanProgress(
            status = ScanStatus.RUNNING,
            currentCategory = null,
        )

        return scanEngine.scan()
            .onEach { progress ->
                currentCoroutineContext().ensureActive()
                if (scanRun == null) {
                    val now = System.currentTimeMillis()
                    val id = scanRunDao.insert(
                        ScanRunEntity(
                            startedAt = now,
                            finishedAt = null,
                            totalBytesFound = 0L,
                            status = ScanStatus.RUNNING,
                        ),
                    )
                    scanRun = ScanRunEntity(
                        id = id,
                        startedAt = now,
                        finishedAt = null,
                        totalBytesFound = 0L,
                        status = ScanStatus.RUNNING,
                    )
                    fileItemDao.deleteAll()
                }

                val whitelistedUris = settingsRepository.settings.first().whitelistedUris
                val filteredItems = progress.items.filterNot { item ->
                    item.uri in whitelistedUris
                }
                latestProgress = progress.copy(
                    items = filteredItems,
                    foundItemCount = filteredItems.size,
                    totalBytesFound = filteredItems.sumOf { it.sizeBytes },
                )
                fileItemDao.upsertAll(filteredItems.map { it.toEntity() })
            }
            .onCompletion { throwable ->
                val existingRun = scanRun ?: return@onCompletion
                val finalStatus = when {
                    throwable is CancellationException -> ScanStatus.CANCELLED
                    throwable != null -> ScanStatus.FAILED
                    else -> ScanStatus.COMPLETED
                }
                val finishedAt = System.currentTimeMillis()

                scanRunDao.update(
                    existingRun.copy(
                        finishedAt = finishedAt,
                        totalBytesFound = latestProgress.totalBytesFound,
                        status = finalStatus,
                    ),
                )

                if (finalStatus == ScanStatus.COMPLETED) {
                    settingsRepository.setLastScanAtMillis(finishedAt)
                }
            }
            .catch { throwable ->
                if (throwable is CancellationException) throw throwable
                emit(latestProgress.copy(status = ScanStatus.FAILED, message = throwable.message))
            }
    }

    override suspend fun removeResultsByUris(uris: List<String>) {
        fileItemDao.deleteByUris(uris)
    }
}
