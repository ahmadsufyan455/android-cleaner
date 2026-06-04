package com.zerodev.clen.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerodev.clen.R
import com.zerodev.clen.domain.repository.ScanRepository
import com.zerodev.clen.domain.usecase.ClearOwnCacheUseCase
import com.zerodev.clen.domain.usecase.GetOwnCacheSizeUseCase
import com.zerodev.clen.domain.repository.SettingsRepository
import com.zerodev.clen.domain.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getOwnCacheSizeUseCase: GetOwnCacheSizeUseCase,
    private val clearOwnCacheUseCase: ClearOwnCacheUseCase,
    private val settingsRepository: SettingsRepository,
    private val storageRepository: StorageRepository,
    private val scanRepository: ScanRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        refreshCacheSize()
        refreshStorageStats()
        observeDashboardState()
    }

    fun refreshCacheSize() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoadingCacheSize = true,
                    errorMessageResId = null,
                )
            }

            runCatching { getOwnCacheSizeUseCase() }
                .onSuccess { cacheSizeBytes ->
                    _state.update {
                        it.copy(
                            cacheSizeBytes = cacheSizeBytes,
                            isLoadingCacheSize = false,
                        )
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            isLoadingCacheSize = false,
                            errorMessageResId = R.string.cache_size_error,
                        )
                    }
                }
        }
    }

    fun clearOwnCache() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isClearingCache = true,
                    errorMessageResId = null,
                )
            }

            runCatching { clearOwnCacheUseCase() }
                .onSuccess { result ->
                    settingsRepository.setLastCleanAtMillis(System.currentTimeMillis())
                    _state.update {
                        it.copy(
                            cacheSizeBytes = result.remainingBytes,
                            isClearingCache = false,
                            lastClearedBytes = result.bytesDeleted,
                        )
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            isClearingCache = false,
                            errorMessageResId = R.string.clear_cache_error,
                        )
                    }
                }
        }
    }

    fun refreshStorageStats() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoadingStorageStats = true,
                    errorMessageResId = null,
                )
            }

            runCatching { storageRepository.getPrimaryStorageStats() }
                .onSuccess { storageStats ->
                    _state.update {
                        it.copy(
                            storageTotalBytes = storageStats.totalBytes,
                            storageUsedBytes = storageStats.usedBytes,
                            storageFreeBytes = storageStats.freeBytes,
                            isLoadingStorageStats = false,
                        )
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            isLoadingStorageStats = false,
                            errorMessageResId = R.string.storage_overview_error,
                        )
                    }
                }
        }
    }

    private fun observeDashboardState() {
        viewModelScope.launch {
            combine(
                settingsRepository.settings,
                scanRepository.observeResults(),
            ) { settings, scanItems ->
                settings to scanItems
            }.collect { (settings, scanItems) ->
                _state.update {
                    it.copy(
                        latestScanBytes = scanItems.sumOf { item -> item.sizeBytes },
                        latestScanItemCount = scanItems.size,
                        lastScanAtMillis = settings.lastScanAtMillis,
                        lastCleanAtMillis = settings.lastCleanAtMillis,
                    )
                }
            }
        }
    }
}
