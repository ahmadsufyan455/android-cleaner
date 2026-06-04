package com.zerodev.clen.domain.deletion

import com.zerodev.clen.domain.model.DeleteRequest
import com.zerodev.clen.domain.model.DeleteResult

interface DeletionExecutor {
    suspend fun delete(request: DeleteRequest): DeleteResult
}
