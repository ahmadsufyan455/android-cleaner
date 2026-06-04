package com.zerodev.clen.presentation.trash

import com.zerodev.clen.domain.model.TrashEntry

data class TrashState(
    val entries: List<TrashEntry> = emptyList(),
    val isWorking: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
)
