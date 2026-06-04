package com.zerodev.clen.di

import com.zerodev.clen.data.scanner.DefaultScanEngine
import com.zerodev.clen.data.scanner.EmptyFolderSafScanner
import com.zerodev.clen.data.scanner.LargeFileScanner
import com.zerodev.clen.data.scanner.LargeFileSafScanner
import com.zerodev.clen.data.scanner.OldDownloadsScanner
import com.zerodev.clen.data.scanner.OwnCacheCategoryScanner
import com.zerodev.clen.data.scanner.ResidualApkScanner
import com.zerodev.clen.domain.scanner.CategoryScanner
import com.zerodev.clen.domain.scanner.ScanEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ScannerModule {
    @Binds
    @Singleton
    fun bindScanEngine(
        implementation: DefaultScanEngine,
    ): ScanEngine

    @Binds
    @IntoSet
    fun bindOwnCacheCategoryScanner(
        implementation: OwnCacheCategoryScanner,
    ): CategoryScanner

    @Binds
    @IntoSet
    fun bindResidualApkScanner(
        implementation: ResidualApkScanner,
    ): CategoryScanner

    @Binds
    @IntoSet
    fun bindLargeFileScanner(
        implementation: LargeFileScanner,
    ): CategoryScanner

    @Binds
    @IntoSet
    fun bindLargeFileSafScanner(
        implementation: LargeFileSafScanner,
    ): CategoryScanner

    @Binds
    @IntoSet
    fun bindOldDownloadsScanner(
        implementation: OldDownloadsScanner,
    ): CategoryScanner

    @Binds
    @IntoSet
    fun bindEmptyFolderSafScanner(
        implementation: EmptyFolderSafScanner,
    ): CategoryScanner
}
