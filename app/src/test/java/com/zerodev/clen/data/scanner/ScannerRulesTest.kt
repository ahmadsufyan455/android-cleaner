package com.zerodev.clen.data.scanner

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScannerRulesTest {
    @Test
    fun residualApkRequiresApkExtensionAndMinimumAge() {
        val nowMillis = 10L * 24L * 60L * 60L * 1_000L
        val oldEnough = nowMillis - ScannerRules.RESIDUAL_APK_MIN_AGE_MILLIS

        assertTrue(
            ScannerRules.isResidualApk(
                displayName = "installer.APK",
                lastModifiedMillis = oldEnough,
                nowMillis = nowMillis,
            ),
        )
        assertFalse(
            ScannerRules.isResidualApk(
                displayName = "installer.zip",
                lastModifiedMillis = oldEnough,
                nowMillis = nowMillis,
            ),
        )
        assertFalse(
            ScannerRules.isResidualApk(
                displayName = "installer.apk",
                lastModifiedMillis = oldEnough + 1L,
                nowMillis = nowMillis,
            ),
        )
    }

    @Test
    fun largeFileUsesInclusiveThreshold() {
        assertFalse(ScannerRules.isLargeFile(sizeBytes = 99L, thresholdBytes = 100L))
        assertTrue(ScannerRules.isLargeFile(sizeBytes = 100L, thresholdBytes = 100L))
        assertTrue(ScannerRules.isLargeFile(sizeBytes = 101L, thresholdBytes = 100L))
    }

    @Test
    fun oldDownloadUsesInclusiveDayThreshold() {
        val nowMillis = 40L * 24L * 60L * 60L * 1_000L
        val thresholdDays = 30
        val cutoffSeconds = (nowMillis / 1_000L) - thresholdDays * 24L * 60L * 60L

        assertTrue(
            ScannerRules.isOldDownload(
                lastModifiedSeconds = cutoffSeconds,
                nowMillis = nowMillis,
                oldFileThresholdDays = thresholdDays,
            ),
        )
        assertFalse(
            ScannerRules.isOldDownload(
                lastModifiedSeconds = cutoffSeconds + 1L,
                nowMillis = nowMillis,
                oldFileThresholdDays = thresholdDays,
            ),
        )
    }

    @Test
    fun emptyFolderRequiresNoFilesAndEmptyChildFolders() {
        assertTrue(
            ScannerRules.isEmptyFolder(
                nonDirectoryChildCount = 0,
                childDirectoryEmptyStates = emptyList(),
            ),
        )
        assertTrue(
            ScannerRules.isEmptyFolder(
                nonDirectoryChildCount = 0,
                childDirectoryEmptyStates = listOf(true, true),
            ),
        )
        assertFalse(
            ScannerRules.isEmptyFolder(
                nonDirectoryChildCount = 1,
                childDirectoryEmptyStates = listOf(true),
            ),
        )
        assertFalse(
            ScannerRules.isEmptyFolder(
                nonDirectoryChildCount = 0,
                childDirectoryEmptyStates = listOf(true, false),
            ),
        )
    }
}
