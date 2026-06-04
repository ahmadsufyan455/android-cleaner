package com.zerodev.clen.domain.model

data class SettingsModel(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val languageMode: LanguageMode = LanguageMode.SYSTEM,
    val scheduleMode: ScheduleMode = ScheduleMode.OFF,
    val largeFileThresholdBytes: Long = DEFAULT_LARGE_FILE_THRESHOLD_BYTES,
    val oldFileThresholdDays: Int = DEFAULT_OLD_FILE_THRESHOLD_DAYS,
    val onboardingCompleted: Boolean = false,
    val lastScanAtMillis: Long? = null,
    val lastCleanAtMillis: Long? = null,
    val safTreeUris: Set<String> = emptySet(),
    val whitelistedUris: Set<String> = emptySet(),
) {
    companion object {
        const val DEFAULT_LARGE_FILE_THRESHOLD_BYTES = 100L * 1024L * 1024L
        const val DEFAULT_OLD_FILE_THRESHOLD_DAYS = 30
    }
}
