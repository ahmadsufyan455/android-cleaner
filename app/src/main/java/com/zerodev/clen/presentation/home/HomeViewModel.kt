package com.zerodev.clen.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerodev.clen.domain.usecase.ClearOwnCacheUseCase
import com.zerodev.clen.domain.usecase.GetOwnCacheSizeUseCase
import com.zerodev.clen.domain.repository.SettingsRepository
import com.zerodev.clen.domain.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getOwnCacheSizeUseCase: GetOwnCacheSizeUseCase,
    private val clearOwnCacheUseCase: ClearOwnCacheUseCase,
    private val settingsRepository: SettingsRepository,
    private val storageRepository: StorageRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        refreshCacheSize()
        refreshStorageStats()
    }

    fun refreshCacheSize() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoadingCacheSize = true,
                    errorMessage = null,
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
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoadingCacheSize = false,
                            errorMessage = throwable.message ?: "Unable to read cache size",
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
                    errorMessage = null,
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
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isClearingCache = false,
                            errorMessage = throwable.message ?: "Unable to clear cache",
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
                    errorMessage = null,
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
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoadingStorageStats = false,
                            errorMessage = throwable.message ?: "Unable to read storage",
                        )
                    }
                }
        }
    }
}
