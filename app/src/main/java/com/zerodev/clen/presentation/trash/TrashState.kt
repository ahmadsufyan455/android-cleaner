package com.zerodev.clen.presentation.trash

import com.zerodev.clen.domain.model.TrashEntry

data class TrashState(
    val entries: List<TrashEntry> = emptyList(),
    val isWorking: Boolean = false,
    val messageResId: Int? = null,
    val messageArgs: List<Any> = emptyList(),
    val errorMessageResId: Int? = null,
)
