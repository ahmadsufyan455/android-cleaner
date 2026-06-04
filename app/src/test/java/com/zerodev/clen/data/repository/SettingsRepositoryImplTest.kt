package com.zerodev.clen.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import com.zerodev.clen.domain.model.LanguageMode
import com.zerodev.clen.domain.model.ScheduleMode
import com.zerodev.clen.domain.model.SettingsModel
import com.zerodev.clen.domain.model.ThemeMode
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryImplTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val testFile = File.createTempFile("settings", ".preferences_pb")
    private val dataStore = PreferenceDataStoreFactory.create(
        scope = testScope,
        produceFile = { testFile },
    )
    private val repository = SettingsRepositoryImpl(dataStore)

    @After
    fun tearDown() {
        testFile.delete()
    }

    @Test
    fun settingsEmitsDefaults() = runTest {
        repository.settings.test {
            assertEquals(SettingsModel(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun settingsPersistsUpdates() = runTest {
        repository.setThemeMode(ThemeMode.DARK)
        repository.setDynamicColorEnabled(false)
        repository.setLanguageMode(LanguageMode.INDONESIAN)
        repository.setScheduleMode(ScheduleMode.WEEKLY)
        repository.setLargeFileThresholdBytes(256L * 1024L * 1024L)
        repository.setOldFileThresholdDays(45)
        repository.setOnboardingCompleted(true)
        repository.setLastScanAtMillis(10_000L)
        repository.setLastCleanAtMillis(20_000L)

        repository.settings.test {
            assertEquals(
                SettingsModel(
                    themeMode = ThemeMode.DARK,
                    dynamicColorEnabled = false,
                    languageMode = LanguageMode.INDONESIAN,
                    scheduleMode = ScheduleMode.WEEKLY,
                    largeFileThresholdBytes = 256L * 1024L * 1024L,
                    oldFileThresholdDays = 45,
                    onboardingCompleted = true,
                    lastScanAtMillis = 10_000L,
                    lastCleanAtMillis = 20_000L,
                ),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun nullableTimestampsCanBeCleared() = runTest {
        repository.setLastScanAtMillis(10_000L)
        repository.setLastCleanAtMillis(20_000L)

        repository.setLastScanAtMillis(null)
        repository.setLastCleanAtMillis(null)

        repository.settings.test {
            assertEquals(null, awaitItem().lastScanAtMillis)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invalidEnumFallsBackToDefault() = runTest {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("theme_mode")] = "NOT_A_THEME"
        }

        repository.settings.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem().themeMode)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
