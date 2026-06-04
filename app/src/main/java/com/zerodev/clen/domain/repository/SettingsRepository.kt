package com.zerodev.clen.domain.repository

import com.zerodev.clen.domain.model.LanguageMode
import com.zerodev.clen.domain.model.ScheduleMode
import com.zerodev.clen.domain.model.SettingsModel
import com.zerodev.clen.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<SettingsModel>

    suspend fun setThemeMode(themeMode: ThemeMode)

    suspend fun setDynamicColorEnabled(enabled: Boolean)

    suspend fun setLanguageMode(languageMode: LanguageMode)

    suspend fun setScheduleMode(scheduleMode: ScheduleMode)

    suspend fun setLargeFileThresholdBytes(bytes: Long)

    suspend fun setOldFileThresholdDays(days: Int)

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun setLastScanAtMillis(timestampMillis: Long?)

    suspend fun setLastCleanAtMillis(timestampMillis: Long?)

    suspend fun addSafTreeUri(uri: String)

    suspend fun removeSafTreeUri(uri: String)
}
