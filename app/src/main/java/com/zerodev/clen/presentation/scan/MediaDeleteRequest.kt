package com.zerodev.clen.presentation.scan

import android.content.IntentSender

data class MediaDeleteRequest(
    val intentSender: IntentSender,
    val uris: List<String>,
)
