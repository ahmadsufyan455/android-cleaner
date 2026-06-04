package com.zerodev.clen.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerodev.clen.data.system.SafTreeGrantManager
import com.zerodev.clen.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val safTreeGrantManager: SafTreeGrantManager,
) : ViewModel() {
    val state: StateFlow<SettingsState> = settingsRepository.settings
        .map { settings -> SettingsState(safGrantCount = settings.safTreeUris.size) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = SettingsState(),
        )

    fun persistSafGrant(uri: Uri) {
        viewModelScope.launch {
            safTreeGrantManager.persistGrant(
                uri = uri,
                resultFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        }
    }
}
