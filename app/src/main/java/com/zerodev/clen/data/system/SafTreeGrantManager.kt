package com.zerodev.clen.data.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.zerodev.clen.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SafTreeGrantManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun persistGrant(uri: Uri, resultFlags: Int) {
        val persistableFlags = resultFlags and (
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        if (persistableFlags != 0) {
            context.contentResolver.takePersistableUriPermission(uri, persistableFlags)
        }
        settingsRepository.addSafTreeUri(uri.toString())
    }

    suspend fun releaseGrant(uri: Uri) {
        val persistedPermission = context.contentResolver.persistedUriPermissions
            .firstOrNull { permission -> permission.uri == uri }

        if (persistedPermission != null) {
            var releaseFlags = 0
            if (persistedPermission.isReadPermission) {
                releaseFlags = releaseFlags or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            if (persistedPermission.isWritePermission) {
                releaseFlags = releaseFlags or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            }
            if (releaseFlags != 0) {
                context.contentResolver.releasePersistableUriPermission(uri, releaseFlags)
            }
        }

        settingsRepository.removeSafTreeUri(uri.toString())
    }
}
