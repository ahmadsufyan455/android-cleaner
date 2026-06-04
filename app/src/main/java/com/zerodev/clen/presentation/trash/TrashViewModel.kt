package com.zerodev.clen.presentation.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerodev.clen.R
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
    private val message = MutableStateFlow<MessageState?>(null)
    private val errorMessage = MutableStateFlow<Int?>(null)

    val state: StateFlow<TrashState> = combine(
        trashRepository.observeTrash(),
        isWorking,
        message,
        errorMessage,
    ) { entries, isWorking, message, errorMessage ->
        TrashState(
            entries = entries,
            isWorking = isWorking,
            messageResId = message?.resId,
            messageArgs = message?.args.orEmpty(),
            errorMessageResId = errorMessage,
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
                message.value = MessageState(R.string.trash_restore_success)
            } else {
                errorMessage.value = R.string.trash_restore_error
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
            message.value = MessageState(R.string.trash_empty_success, listOf(count))
            isWorking.value = false
        }
    }

    private data class MessageState(
        val resId: Int,
        val args: List<Any> = emptyList(),
    )
}
