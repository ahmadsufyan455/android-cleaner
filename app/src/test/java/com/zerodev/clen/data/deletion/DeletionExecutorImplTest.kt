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
}
