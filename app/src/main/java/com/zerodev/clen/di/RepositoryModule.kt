package com.zerodev.clen.di

import com.zerodev.clen.data.repository.OwnCacheRepositoryImpl
import com.zerodev.clen.domain.repository.OwnCacheRepository
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
}
