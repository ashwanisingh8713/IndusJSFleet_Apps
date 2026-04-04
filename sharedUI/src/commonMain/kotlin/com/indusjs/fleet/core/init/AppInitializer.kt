package com.indusjs.fleet.core.init

import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.TAG_APP_INITIALIZER
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.error.result.Result
import com.indusjs.fleet.domain.usecase.costs.InitializeCostTypesUseCase
import com.indusjs.fleet.domain.usecase.states.InitializeStatesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * App Initializer - handles one-time initialization tasks on app launch.
 *
 * Current responsibilities:
 * - Initialize cost types from API (one-time save to local storage)
 * - Initialize entity states from API (one-time save to local storage)
 *
 * Future responsibilities can be added here:
 * - Initialize user preferences
 * - Sync offline data
 * - Initialize analytics
 */
class AppInitializer(
    private val initializeCostTypesUseCase: InitializeCostTypesUseCase,
    private val initializeStatesUseCase: InitializeStatesUseCase,
    private val dispatcherProvider: DispatcherProvider,
    private val logger: FleetLogger
) {

    private var isInitialized = false

    /**
     * Initialize the app with one-time setup tasks.
     * This runs on a background thread and doesn't block the UI.
     *
     * @param scope The coroutine scope to run initialization in
     */
    fun initialize(scope: CoroutineScope) {
        if (isInitialized) {
            logger.d(TAG_APP_INITIALIZER, "App already initialized, skipping")
            return
        }

        scope.launch(dispatcherProvider.io) {
            logger.d(TAG_APP_INITIALIZER, "Starting app initialization...")

            // Initialize cost types (one-time fetch and save)
            when (val result = initializeCostTypesUseCase()) {
                is Result.Success -> {
                    logger.d(TAG_APP_INITIALIZER, "Cost types initialization completed successfully")
                }
                is Result.Error -> {
                    logger.w(TAG_APP_INITIALIZER, "Cost types initialization failed: ${result.message}")
                    // Don't block app - cost type screens will use fallback hardcoded types
                }
                is Result.Loading -> {
                    // Shouldn't happen for this use case
                }
            }

            // Initialize entity states (one-time fetch and save)
            when (val result = initializeStatesUseCase()) {
                is Result.Success -> {
                    logger.d(TAG_APP_INITIALIZER, "Entity states initialization completed successfully")
                }
                is Result.Error -> {
                    logger.w(TAG_APP_INITIALIZER, "Entity states initialization failed: ${result.message}")
                    // Don't block app - screens will use fallback hardcoded states from StatusConstants
                }
                is Result.Loading -> {
                    // Shouldn't happen for this use case
                }
            }

            isInitialized = true
            logger.d(TAG_APP_INITIALIZER, "App initialization completed")
        }
    }

    /**
     * Reset initialization state (useful for testing or re-initialization).
     */
    fun reset() {
        isInitialized = false
    }
}
