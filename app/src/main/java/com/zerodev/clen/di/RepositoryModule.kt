package com.zerodev.clen.di

import com.zerodev.clen.data.repository.OwnCacheRepositoryImpl
import com.zerodev.clen.data.repository.ScanRepositoryImpl
import com.zerodev.clen.data.repository.StorageRepositoryImpl
import com.zerodev.clen.data.repository.TrashRepositoryImpl
import com.zerodev.clen.domain.repository.OwnCacheRepository
import com.zerodev.clen.domain.repository.ScanRepository
import com.zerodev.clen.domain.repository.StorageRepository
import com.zerodev.clen.domain.repository.TrashRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    @Singleton
    fun bindOwnCacheRepository(
        implementation: OwnCacheRepositoryImpl,
    ): OwnCacheRepository

    @Binds
    @Singleton
    fun bindScanRepository(
        implementation: ScanRepositoryImpl,
    ): ScanRepository

    @Binds
    @Singleton
    fun bindStorageRepository(
        implementation: StorageRepositoryImpl,
    ): StorageRepository

    @Binds
    @Singleton
    fun bindTrashRepository(
        implementation: TrashRepositoryImpl,
    ): TrashRepository
}
