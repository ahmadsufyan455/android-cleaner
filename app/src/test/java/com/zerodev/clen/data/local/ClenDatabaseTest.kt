package com.zerodev.clen.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.zerodev.clen.data.local.entity.FileItemEntity
import com.zerodev.clen.data.local.entity.ScanRunEntity
import com.zerodev.clen.data.local.entity.TrashEntryEntity
import com.zerodev.clen.domain.model.JunkCategory
import com.zerodev.clen.domain.model.ScanSource
import com.zerodev.clen.domain.model.ScanStatus
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ClenDatabaseTest {
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
    fun fileItemDaoStoresItemsByCategory() = runTest {
        val item = FileItemEntity(
            uri = "content://downloads/apk",
            displayName = "old.apk",
            sizeBytes = 42_000L,
            mimeType = "application/vnd.android.package-archive",
            lastModified = 1_700_000_000L,
            category = JunkCategory.RESIDUAL_APK,
            source = ScanSource.DOWNLOADS,
            sha256 = null,
            perceptualHash = null,
        )

        database.fileItemDao().upsertAll(listOf(item))

        database.fileItemDao().observeByCategory(JunkCategory.RESIDUAL_APK).test {
            assertEquals(listOf(item), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(listOf(item), database.fileItemDao().getByUris(listOf(item.uri)))

        database.fileItemDao().deleteByUris(listOf(item.uri))

        database.fileItemDao().observeAll().test {
            assertEquals(emptyList<FileItemEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun scanRunDaoStoresLatestScan() = runTest {
        val scanRun = ScanRunEntity(
            startedAt = 1_000L,
            finishedAt = 2_000L,
            totalBytesFound = 42_000L,
            status = ScanStatus.COMPLETED,
        )

        val id = database.scanRunDao().insert(scanRun)

        database.scanRunDao().observeLatest().test {
            assertEquals(scanRun.copy(id = id), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun trashEntryDaoFindsExpiredEntries() = runTest {
        val expired = TrashEntryEntity(
            id = "expired",
            originalUri = "content://downloads/expired",
            cachedPath = "/cache/trash/expired",
            sizeBytes = 12L,
            deletedAt = 1_000L,
            expiresAt = 2_000L,
            restorable = true,
        )
        val active = TrashEntryEntity(
            id = "active",
            originalUri = "content://downloads/active",
            cachedPath = "/cache/trash/active",
            sizeBytes = 24L,
            deletedAt = 2_000L,
            expiresAt = 4_000L,
            restorable = true,
        )

        database.trashEntryDao().upsert(expired)
        database.trashEntryDao().upsert(active)

        assertEquals(setOf(expired, active), database.trashEntryDao().getAll().toSet())
        assertEquals(listOf(expired), database.trashEntryDao().getExpired(nowMillis = 3_000L))

        database.trashEntryDao().deleteExpired(nowMillis = 3_000L)

        database.trashEntryDao().observeAll().test {
            val remainingEntries = awaitItem()
            assertEquals(listOf(active), remainingEntries)
            assertTrue(remainingEntries.none { it.id == expired.id })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
