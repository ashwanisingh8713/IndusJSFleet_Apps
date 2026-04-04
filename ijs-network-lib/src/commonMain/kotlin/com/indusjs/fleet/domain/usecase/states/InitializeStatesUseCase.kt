package com.indusjs.fleet.domain.usecase.states

import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.network.TAG_INIT_STATES
import com.indusjs.error.result.Result
import com.indusjs.fleet.domain.repository.states.StatesRepository
import dev.zacsweers.metro.Inject

/**
 * Use case to initialize entity states on app launch.
 *
 * This use case checks if states are already cached:
 * - If cached: Does nothing (one-time save)
 * - If not cached: Fetches from API and saves to local storage
 *
 * This should be called on app startup before any state-dependent screens are shown.
 */
@Inject
class InitializeStatesUseCase(
    private val statesRepository: StatesRepository,
    private val logger: FleetLogger
) {

    /**
     * Initialize states if not already cached.
     *
     * @return Result.Success when initialization completes (whether from cache or API)
     * @return Result.Error only if something critical fails
     */
    suspend operator fun invoke(): Result<Unit> {
        logger.d(TAG_INIT_STATES, "Initializing entity states...")
        return statesRepository.initializeStatesIfNeeded()
    }
}

