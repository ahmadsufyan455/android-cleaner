package com.zerodev.clen.data.permission

import android.Manifest
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MediaPermissionCoordinatorTest {
    private val coordinator = MediaPermissionCoordinator(
        context = ApplicationProvider.getApplicationContext(),
    )

    @Test
    fun requiredPermissionsForApi26UsesLegacyReadStorage() {
        assertEquals(
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            coordinator.requiredPermissions(apiLevel = 26),
        )
    }

    @Test
    fun requiredPermissionsForApi33UsesMediaPermissions() {
        assertEquals(
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
            ),
            coordinator.requiredPermissions(apiLevel = 33),
        )
    }

    @Test
    fun requiredPermissionsForApi34IncludesPartialVisualAccessPermission() {
        assertEquals(
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
            ),
            coordinator.requiredPermissions(apiLevel = 34),
        )
    }
}
