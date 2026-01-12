package com.indusjs.fleet.domain.repository.costs

import com.indusjs.error.result.Result
import com.indusjs.fleet.data.model.costs.CostTypeCategoryDto
import com.indusjs.fleet.domain.repository.Repository

/**
 * Repository interface for cost types operations.
 *
 * Cost types are fetched once on first app launch and persisted forever.
 * They are never updated unless the app data is cleared.
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
     * Check if cost types are already cached.
     */
    suspend fun hasCostTypesCached(): Boolean
}

