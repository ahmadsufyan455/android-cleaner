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

        return MediaStore.createDeleteRequest(
            context.contentResolver,
            uris.map { uri -> Uri.parse(uri) },
        )
    }
}
