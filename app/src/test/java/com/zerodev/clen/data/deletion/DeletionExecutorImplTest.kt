package com.zerodev.clen.data.deletion

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.zerodev.clen.data.local.ClenDatabase
import com.zerodev.clen.data.local.entity.FileItemEntity
import com.zerodev.clen.data.system.FileTreeOperations
import com.zerodev.clen.data.system.OwnCacheDataSource
import com.zerodev.clen.domain.model.DeleteOutcomeStatus
import com.zerodev.clen.domain.model.DeleteRequest
import com.zerodev.clen.domain.model.FileItem
import com.zerodev.clen.domain.model.JunkCategory
import com.zerodev.clen.domain.model.ScanSource
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class DeletionExecutorImplTest {
    private lateinit var database: ClenDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ClenDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun deleteOwnCacheClearsCacheAndRemovesScanItem() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val cacheFile = File(context.cacheDir, "temporary-cache.bin")
        cacheFile.writeText("cache")
        val item = FileItem(
            uri = context.cacheDir.toURI().toString(),
            displayName = "Clen cache",
            sizeBytes = cacheFile.length(),
            mimeType = null,
            lastModified = cacheFile.lastModified(),
            category = JunkCategory.OWN_CACHE,
            source = ScanSource.APP_PRIVATE,
        )
        database.fileItemDao().upsertAll(
            listOf(
                FileItemEntity(
                    uri = item.uri,
                    displayName = item.displayName,
                    sizeBytes = item.sizeBytes,
                    mimeType = item.mimeType,
                    lastModified = item.lastModified,
                    category = item.category,
                    source = item.source,
                    sha256 = null,
                    perceptualHash = null,
                ),
            ),
        )
        val executor = DeletionExecutorImpl(
            context = context,
            ownCacheDataSource = OwnCacheDataSource(context, FileTreeOperations()),
            fileItemDao = database.fileItemDao(),
            trashEntryDao = database.trashEntryDao(),
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val result = executor.delete(
            DeleteRequest(
                items = listOf(item),
                allowPermanentDelete = true,
            ),
        )

        assertEquals(DeleteOutcomeStatus.PERMANENT_DELETED, result.outcomes.single().status)
        assertFalse(cacheFile.exists())
        database.fileItemDao().observeAll().test {
            assertEquals(emptyList<FileItemEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun unsupportedSourceIsSkipped() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val item = FileItem(
            uri = "file:///unsupported",
            displayName = "unsupported",
            sizeBytes = 1L,
            mimeType = null,
            lastModified = 1L,
            category = JunkCategory.LARGE_FILE,
            source = ScanSource.APP_PRIVATE,
        )
        val executor = createExecutor(context)

        val result = executor.delete(
            DeleteRequest(
                items = listOf(item),
                allowPermanentDelete = true,
            ),
        )

        assertEquals(DeleteOutcomeStatus.SKIPPED, result.outcomes.single().status)
    }

    @Test
    @Config(sdk = [30])
    fun mediaStoreDeleteOnApi30RequiresPlatformConfirmation() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val item = FileItem(
            uri = "content://media/external/file/1",
            displayName = "video.mp4",
            sizeBytes = 1L,
            mimeType = "video/mp4",
            lastModified = 1L,
            category = JunkCategory.LARGE_FILE,
            source = ScanSource.MEDIASTORE,
        )
        val executor = createExecutor(context)

        val result = executor.delete(
            DeleteRequest(
                items = listOf(item),
                allowPermanentDelete = true,
            ),
        )

        assertEquals(
            DeleteOutcomeStatus.PLATFORM_CONFIRMATION_REQUIRED,
            result.outcomes.single().status,
        )
    }

    private fun createExecutor(
        context: android.content.Context,
    ): DeletionExecutorImpl = DeletionExecutorImpl(
        context = context,
        ownCacheDataSource = OwnCacheDataSource(context, FileTreeOperations()),
        fileItemDao = database.fileItemDao(),
        trashEntryDao = database.trashEntryDao(),
        ioDispatcher = UnconfinedTestDispatcher(),
    )
}
