package com.zerodev.clen.di

import com.zerodev.clen.data.deletion.DeletionExecutorImpl
import com.zerodev.clen.domain.deletion.DeletionExecutor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DeletionModule {
    @Binds
    @Singleton
    fun bindDeletionExecutor(
        implementation: DeletionExecutorImpl,
    ): DeletionExecutor
}
