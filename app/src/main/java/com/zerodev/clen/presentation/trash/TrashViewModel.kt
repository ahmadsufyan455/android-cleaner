package com.zerodev.clen.presentation.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerodev.clen.domain.model.TrashEntry
import com.zerodev.clen.domain.repository.TrashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val trashRepository: TrashRepository,
) : ViewModel() {
    private val isWorking = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)
    private val errorMessage = MutableStateFlow<String?>(null)

    val state: StateFlow<TrashState> = combine(
        trashRepository.observeTrash(),
        isWorking,
        message,
        errorMessage,
    ) { entries, isWorking, message, errorMessage ->
        TrashState(
            entries = entries,
            isWorking = isWorking,
            message = message,
            errorMessage = errorMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = TrashState(),
    )

    fun restore(entry: TrashEntry) {
        viewModelScope.launch {
            isWorking.value = true
            message.value = null
            errorMessage.value = null
            val restored = trashRepository.restore(entry)
            if (restored) {
                message.value = "File restored."
            } else {
                errorMessage.value = "Unable to restore this file."
            }
            isWorking.value = false
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            isWorking.value = true
            message.value = null
            errorMessage.value = null
            val count = trashRepository.emptyTrash()
            message.value = "$count trash entries removed."
            isWorking.value = false
        }
    }
}
