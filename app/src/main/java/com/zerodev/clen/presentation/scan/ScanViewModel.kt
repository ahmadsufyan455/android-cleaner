package com.zerodev.clen.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerodev.clen.data.permission.MediaPermissionCoordinator
import com.zerodev.clen.domain.model.ScanStatus
import com.zerodev.clen.domain.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    mediaPermissionCoordinator: MediaPermissionCoordinator,
) : ViewModel() {
    private val _state = MutableStateFlow(
        ScanState(mediaPermissions = mediaPermissionCoordinator.requiredPermissions()),
    )
    val state: StateFlow<ScanState> = _state.asStateFlow()

    private var scanJob: Job? = null

    fun startScan() {
        if (scanJob?.isActive == true) return

        scanJob = viewModelScope.launch {
            scanRepository.runScan()
                .catch { throwable ->
                    _state.update {
                        it.copy(
                            isScanning = false,
                            status = ScanStatus.FAILED,
                            errorMessage = throwable.message ?: "Scan failed",
                        )
                    }
                }
                .onCompletion { throwable ->
                    if (throwable == null) {
                        _state.update { it.copy(isScanning = false) }
                    }
                }
                .collect { progress ->
                    _state.update {
                        it.copy(
                            isScanning = progress.status == ScanStatus.RUNNING,
                            status = progress.status,
                            currentCategory = progress.currentCategory,
                            foundItemCount = progress.foundItemCount,
                            totalBytesFound = progress.totalBytesFound,
                            progressMessage = progress.message,
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    fun cancelScan() {
        scanJob?.cancel()
        scanJob = null
        _state.update {
            it.copy(
                isScanning = false,
                status = ScanStatus.CANCELLED,
                currentCategory = null,
            )
        }
    }
}
