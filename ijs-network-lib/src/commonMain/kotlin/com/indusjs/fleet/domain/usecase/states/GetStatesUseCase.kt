package com.indusjs.fleet.domain.usecase.states

import com.indusjs.fleet.data.model.states.StatesDataDto
import com.indusjs.fleet.domain.repository.states.StatesRepository
import com.indusjs.error.result.Result
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Use case to fetch entity state reference data.
 * Returns cached states if available, or fetches from API.
 */
@Inject
class GetStatesUseCase(
    private val statesRepository: StatesRepository
) {
    operator fun invoke(): Flow<Result<StatesDataDto>> = statesRepository.getStates()

    suspend fun refresh(): Result<StatesDataDto> = statesRepository.refreshStates()

    suspend fun cached(): StatesDataDto? = statesRepository.getCachedStates()
}
