package com.indusjs.fleet.domain.repository.states

import com.indusjs.fleet.data.model.states.StatesDataDto
import com.indusjs.error.result.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository for entity state reference data (vehicle, driver, trip, payment states).
 *
 * States are fetched from GET /states and cached locally so they
 * don't need a network call on every screen load.
 *
 * Follows the same cache-first pattern as CostTypesRepository:
 * 1. Check if states are cached locally
 * 2. If cached and valid → return cached data
 * 3. If not cached → fetch from API → save locally
 * 4. If API fails and no cache → use hardcoded fallback
 */
interface StatesRepository {
    /**
     * Initialize states by fetching from API if not already cached.
     * Called on app launch by AppInitializer.
     */
    suspend fun initializeStatesIfNeeded(): Result<Unit>

    /**
     * Get all states as a flow (cache-first, then API).
     */
    fun getStates(): Flow<Result<StatesDataDto>>

    /**
     * Force refresh states from API and update local cache.
     */
    suspend fun refreshStates(): Result<StatesDataDto>

    /**
     * Get cached states (in-memory or local storage).
     */
    suspend fun getCachedStates(): StatesDataDto?

    /**
     * Get vehicle states as a flat list of (value, label) pairs.
     * Falls back to hardcoded values if cache is empty.
     */
    suspend fun getVehicleStatesFlat(): List<Pair<String, String>>

    /**
     * Get driver states as a flat list of (value, label) pairs.
     * Falls back to hardcoded values if cache is empty.
     */
    suspend fun getDriverStatesFlat(): List<Pair<String, String>>

    /**
     * Get trip states as a flat list of (value, label) pairs.
     * Falls back to hardcoded values if cache is empty.
     */
    suspend fun getTripStatesFlat(): List<Pair<String, String>>

    /**
     * Get payment states as a flat list of (value, label) pairs.
     * Falls back to hardcoded values if cache is empty.
     */
    suspend fun getPaymentStatesFlat(): List<Pair<String, String>>

    /**
     * Check if states are already cached.
     */
    suspend fun hasStatesCached(): Boolean
}
