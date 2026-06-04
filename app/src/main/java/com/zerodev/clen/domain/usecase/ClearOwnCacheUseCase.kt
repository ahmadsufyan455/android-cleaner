package com.zerodev.clen.domain.usecase

import com.zerodev.clen.core.dispatcher.IoDispatcher
import com.zerodev.clen.domain.model.ClearOwnCacheResult
import com.zerodev.clen.domain.repository.OwnCacheRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ClearOwnCacheUseCase @Inject constructor(
    private val ownCacheRepository: OwnCacheRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(): ClearOwnCacheResult = withContext(ioDispatcher) {
        ownCacheRepository.clearCache()
    }
}
