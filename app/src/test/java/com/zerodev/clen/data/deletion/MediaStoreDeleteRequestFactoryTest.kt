package com.zerodev.clen.data.deletion

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MediaStoreDeleteRequestFactoryTest {
    private val factory = MediaStoreDeleteRequestFactory(
        context = ApplicationProvider.getApplicationContext(),
    )

    @Test
    fun filterDeleteRequestUrisKeepsOnlyMediaCollectionUris() {
        val uris = listOf(
            "content://media/external/images/media/1",
            "content://media/external/video/media/2",
            "content://media/external/audio/media/3",
            "content://media/external/file/4",
            "content://downloads/public_downloads/5",
            "file:///cache/item",
        )

        assertEquals(
            listOf(
                "content://media/external/images/media/1",
                "content://media/external/video/media/2",
                "content://media/external/audio/media/3",
            ),
            factory.filterDeleteRequestUris(uris),
        )
    }
}
