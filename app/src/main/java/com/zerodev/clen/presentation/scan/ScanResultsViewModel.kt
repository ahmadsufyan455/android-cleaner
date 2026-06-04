package com.zerodev.clen.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerodev.clen.domain.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ScanResultsViewModel @Inject constructor(
    scanRepository: ScanRepository,
) : ViewModel() {
    val state: StateFlow<ScanResultsState> = combine(
        scanRepository.observeLatestRun(),
        scanRepository.observeResults(),
    ) { latestRun, items ->
        ScanResultsState(
            latestRun = latestRun,
            items = items,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = ScanResultsState(),
    )
}
