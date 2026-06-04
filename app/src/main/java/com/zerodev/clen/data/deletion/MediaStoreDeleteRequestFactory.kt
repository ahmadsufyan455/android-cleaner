package com.zerodev.clen.data.deletion

import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MediaStoreDeleteRequestFactory @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun createDeleteRequest(uris: List<String>): PendingIntent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || uris.isEmpty()) return null
        val mediaUris = uris
            .map { uri -> Uri.parse(uri) }
            .filter { uri -> uri.isDeleteRequestMediaUri() }
        if (mediaUris.isEmpty()) return null

        return runCatching {
            MediaStore.createDeleteRequest(context.contentResolver, mediaUris)
        }.getOrNull()
    }

    fun filterDeleteRequestUris(uris: List<String>): List<String> =
        uris.filter { uri -> Uri.parse(uri).isDeleteRequestMediaUri() }

    private fun Uri.isDeleteRequestMediaUri(): Boolean {
        if (scheme != "content" || authority != MediaStore.AUTHORITY) return false
        val pathValue = path.orEmpty()
        return "/images/media/" in pathValue ||
            "/video/media/" in pathValue ||
            "/audio/media/" in pathValue
    }
}
