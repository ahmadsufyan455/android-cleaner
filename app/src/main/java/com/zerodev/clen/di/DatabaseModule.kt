package com.zerodev.clen.di

import android.content.Context
import androidx.room.Room
import com.zerodev.clen.data.local.ClenDatabase
import com.zerodev.clen.data.local.DatabaseNames
import com.zerodev.clen.data.local.dao.FileItemDao
import com.zerodev.clen.data.local.dao.ScanRunDao
import com.zerodev.clen.data.local.dao.TrashEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideClenDatabase(
        @ApplicationContext context: Context,
    ): ClenDatabase = Room.databaseBuilder(
        context = context,
        klass = ClenDatabase::class.java,
        name = DatabaseNames.CLEN_DATABASE,
    )
        .fallbackToDestructiveMigration(true)
        .build()

    @Provides
    fun provideFileItemDao(database: ClenDatabase): FileItemDao = database.fileItemDao()

    @Provides
    fun provideScanRunDao(database: ClenDatabase): ScanRunDao = database.scanRunDao()

    @Provides
    fun provideTrashEntryDao(database: ClenDatabase): TrashEntryDao = database.trashEntryDao()
}
