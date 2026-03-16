package com.indusjs.fleet.domain.usecase.costs

import co.touchlab.kermit.Logger
import com.indusjs.error.result.Result
import com.indusjs.fleet.domain.repository.costs.CostTypesRepository
import dev.zacsweers.metro.Inject

/**
 * Use case to initialize cost types on app launch.
 *
 * This use case checks if cost types are already cached:
 * - If cached: Does nothing (one-time save)
 * - If not cached: Fetches from API and saves to local storage
 *
 * This should be called on app startup before any cost entry screens are shown.
 */
@Inject
class InitializeCostTypesUseCase(
    private val costTypesRepository: CostTypesRepository
) {
    private val log = Logger.withTag("InitializeCostTypesUseCase")

    /**
     * Initialize cost types if not already cached.
     *
     * @return Result.Success when initialization completes (whether from cache or API)
     * @return Result.Error only if something critical fails
     */
    suspend operator fun invoke(): Result<Unit> {
        log.d { "Initializing cost types..." }
        return costTypesRepository.initializeCostTypesIfNeeded()
    }
}

