package com.zerodev.clen.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.zerodev.clen.data.local.settingsDataStore
import com.zerodev.clen.data.repository.SettingsRepositoryImpl
import com.zerodev.clen.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface SettingsModule {
    @Binds
    @Singleton
    fun bindSettingsRepository(
        implementation: SettingsRepositoryImpl,
    ): SettingsRepository

    companion object {
        @Provides
        @Singleton
        fun provideSettingsDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> = context.settingsDataStore
    }
}
