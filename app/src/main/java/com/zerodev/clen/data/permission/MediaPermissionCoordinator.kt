package com.zerodev.clen.data.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MediaPermissionCoordinator @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun requiredPermissions(apiLevel: Int = Build.VERSION.SDK_INT): List<String> =
        when {
            apiLevel >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
            )
            apiLevel >= Build.VERSION_CODES.TIRAMISU -> listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
            )
            else -> listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    fun grantedPermissions(apiLevel: Int = Build.VERSION.SDK_INT): Set<String> =
        requiredPermissions(apiLevel)
            .filter { permission ->
                ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
            }
            .toSet()

    fun hasAnyMediaReadAccess(apiLevel: Int = Build.VERSION.SDK_INT): Boolean =
        grantedPermissions(apiLevel).isNotEmpty()

    fun hasFullRequestedAccess(apiLevel: Int = Build.VERSION.SDK_INT): Boolean =
        grantedPermissions(apiLevel).containsAll(requiredPermissions(apiLevel))
}
