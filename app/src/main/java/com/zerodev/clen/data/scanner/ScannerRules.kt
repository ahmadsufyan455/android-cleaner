package com.zerodev.clen.data.scanner

internal object ScannerRules {
    const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    const val RESIDUAL_APK_MIN_AGE_MILLIS = 7L * 24L * 60L * 60L * 1_000L

    fun isResidualApk(
        displayName: String,
        lastModifiedMillis: Long,
        nowMillis: Long,
    ): Boolean =
        displayName.endsWith(".apk", ignoreCase = true) &&
            lastModifiedMillis <= nowMillis - RESIDUAL_APK_MIN_AGE_MILLIS

    fun isLargeFile(sizeBytes: Long, thresholdBytes: Long): Boolean =
        sizeBytes >= thresholdBytes

    fun isOldDownload(
        lastModifiedSeconds: Long,
        nowMillis: Long,
        oldFileThresholdDays: Int,
    ): Boolean {
        val olderThanSeconds = (nowMillis / 1_000L) -
            oldFileThresholdDays * 24L * 60L * 60L
        return lastModifiedSeconds <= olderThanSeconds
    }

    fun isEmptyFolder(
        nonDirectoryChildCount: Int,
        childDirectoryEmptyStates: List<Boolean>,
    ): Boolean =
        nonDirectoryChildCount == 0 && childDirectoryEmptyStates.all { isEmpty -> isEmpty }
}
