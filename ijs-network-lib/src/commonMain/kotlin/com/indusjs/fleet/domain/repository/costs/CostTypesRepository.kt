package com.indusjs.fleet.domain.repository.costs

import com.indusjs.error.result.Result
import com.indusjs.fleet.data.model.costs.CostTypeCategoryDto
import com.indusjs.fleet.domain.repository.Repository

/**
 * Repository interface for cost types operations.
 *
 * Cost types are fetched on first app launch and persisted.
 * They can be refreshed manually from the cost entry screens.
 */
interface CostTypesRepository : Repository {

    /**
     * Fetch and cache cost types from API if not already cached.
     * This is a one-time operation that happens on first app launch.
     *
     * @return Result.Success if cached or successfully fetched and cached
     * @return Result.Error if not cached and API fetch failed
     */
    suspend fun initializeCostTypesIfNeeded(): Result<Unit>

    /**
     * Force refresh trip cost types from API.
     * Clears existing cache and fetches fresh data.
     *
     * @return Result.Success if successfully fetched and cached
     * @return Result.Error if API fetch failed
     */
    suspend fun refreshTripCostTypes(): Result<Unit>

    /**
     * Force refresh maintenance cost types from API.
     * Clears existing cache and fetches fresh data.
     *
     * @return Result.Success if successfully fetched and cached
     * @return Result.Error if API fetch failed
     */
    suspend fun refreshMaintenanceCostTypes(): Result<Unit>

    /**
     * Force refresh driver cost types from API.
     * Clears existing cache and fetches fresh data.
     *
     * @return Result.Success if successfully fetched and cached
     * @return Result.Error if API fetch failed
     */
    suspend fun refreshDriverCostTypes(): Result<Unit>

    /**
     * Get cached trip cost types.
     * Returns the full category with grouped cost types.
     *
     * @return Cached trip cost types or null if not yet cached
     */
    suspend fun getTripCostTypes(): CostTypeCategoryDto?

    /**
     * Get cached maintenance cost types.
     * Returns the full category with grouped cost types.
     *
     * @return Cached maintenance cost types or null if not yet cached
     */
    suspend fun getMaintenanceCostTypes(): CostTypeCategoryDto?

    /**
     * Get cached driver cost types.
     * Returns the full category with grouped cost types.
     *
     * @return Cached driver cost types or null if not yet cached
     */
    suspend fun getDriverCostTypes(): CostTypeCategoryDto?

    /**
     * Get trip cost types as a flat list of id to name pairs.
     * Useful for dropdowns and selection.
     */
    suspend fun getTripCostTypesFlat(): List<Pair<String, String>>

    /**
     * Get maintenance cost types as a flat list of id to name pairs.
     * Useful for dropdowns and selection.
     */
    suspend fun getMaintenanceCostTypesFlat(): List<Pair<String, String>>

    /**
     * Get driver cost types as a flat list of id to name pairs.
     * Useful for dropdowns and selection.
     */
    suspend fun getDriverCostTypesFlat(): List<Pair<String, String>>

    /**
     * Check if cost types are already cached.
     */
    suspend fun hasCostTypesCached(): Boolean
}
