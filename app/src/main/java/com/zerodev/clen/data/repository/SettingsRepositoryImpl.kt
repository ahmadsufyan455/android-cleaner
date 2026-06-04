package com.zerodev.clen.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zerodev.clen.domain.model.LanguageMode
import com.zerodev.clen.domain.model.ScheduleMode
import com.zerodev.clen.domain.model.SettingsModel
import com.zerodev.clen.domain.model.ThemeMode
import com.zerodev.clen.domain.repository.SettingsRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    override val settings: Flow<SettingsModel> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences -> preferences.toSettingsModel() }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = themeMode.name
        }
    }

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.DYNAMIC_COLOR_ENABLED] = enabled
        }
    }

    override suspend fun setLanguageMode(languageMode: LanguageMode) {
        dataStore.edit { preferences ->
            preferences[Keys.LANGUAGE_MODE] = languageMode.name
        }
    }

    override suspend fun setScheduleMode(scheduleMode: ScheduleMode) {
        dataStore.edit { preferences ->
            preferences[Keys.SCHEDULE_MODE] = scheduleMode.name
        }
    }

    override suspend fun setLargeFileThresholdBytes(bytes: Long) {
        dataStore.edit { preferences ->
            preferences[Keys.LARGE_FILE_THRESHOLD_BYTES] = bytes
        }
    }

    override suspend fun setOldFileThresholdDays(days: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.OLD_FILE_THRESHOLD_DAYS] = days
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    override suspend fun setLastScanAtMillis(timestampMillis: Long?) {
        dataStore.edit { preferences ->
            if (timestampMillis == null) {
                preferences.remove(Keys.LAST_SCAN_AT_MILLIS)
            } else {
                preferences[Keys.LAST_SCAN_AT_MILLIS] = timestampMillis
            }
        }
    }

    override suspend fun setLastCleanAtMillis(timestampMillis: Long?) {
        dataStore.edit { preferences ->
            if (timestampMillis == null) {
                preferences.remove(Keys.LAST_CLEAN_AT_MILLIS)
            } else {
                preferences[Keys.LAST_CLEAN_AT_MILLIS] = timestampMillis
            }
        }
    }

    private fun Preferences.toSettingsModel(): SettingsModel = SettingsModel(
        themeMode = enumOrDefault(this[Keys.THEME_MODE], ThemeMode.SYSTEM),
        dynamicColorEnabled = this[Keys.DYNAMIC_COLOR_ENABLED] ?: true,
        languageMode = enumOrDefault(this[Keys.LANGUAGE_MODE], LanguageMode.SYSTEM),
        scheduleMode = enumOrDefault(this[Keys.SCHEDULE_MODE], ScheduleMode.OFF),
        largeFileThresholdBytes = this[Keys.LARGE_FILE_THRESHOLD_BYTES]
            ?: SettingsModel.DEFAULT_LARGE_FILE_THRESHOLD_BYTES,
        oldFileThresholdDays = this[Keys.OLD_FILE_THRESHOLD_DAYS]
            ?: SettingsModel.DEFAULT_OLD_FILE_THRESHOLD_DAYS,
        onboardingCompleted = this[Keys.ONBOARDING_COMPLETED] ?: false,
        lastScanAtMillis = this[Keys.LAST_SCAN_AT_MILLIS],
        lastCleanAtMillis = this[Keys.LAST_CLEAN_AT_MILLIS],
    )

    private inline fun <reified T : Enum<T>> enumOrDefault(
        value: String?,
        defaultValue: T,
    ): T = value?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: defaultValue

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color_enabled")
        val LANGUAGE_MODE = stringPreferencesKey("language_mode")
        val SCHEDULE_MODE = stringPreferencesKey("schedule_mode")
        val LARGE_FILE_THRESHOLD_BYTES = longPreferencesKey("large_file_threshold_bytes")
        val OLD_FILE_THRESHOLD_DAYS = intPreferencesKey("old_file_threshold_days")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LAST_SCAN_AT_MILLIS = longPreferencesKey("last_scan_at_millis")
        val LAST_CLEAN_AT_MILLIS = longPreferencesKey("last_clean_at_millis")
    }
}
