package com.indusjs.fleet.data.datasource.costs

import com.indusjs.fleet.data.datasource.LocalDataSource
import com.indusjs.fleet.data.model.costs.CostTypeCategoryDto

/**
 * Local data source interface for Cost Types caching.
 * Abstracts the underlying storage mechanism.
 *
 * Cost types are fetched on first app launch and can be refreshed manually.
 *
 * NOTE: Interface lives in ijs-network-lib.
 * Implementation (CostsLocalDataSourceImpl) lives in sharedUI (uses Room DAOs).
 */
interface CostsLocalDataSource : LocalDataSource {
    suspend fun getTripCostTypes(): CostTypeCategoryDto?
    suspend fun saveTripCostTypes(data: CostTypeCategoryDto)
    suspend fun hasTripCostTypes(): Boolean
    suspend fun clearTripCostTypes()

    suspend fun getMaintenanceCostTypes(): CostTypeCategoryDto?
    suspend fun saveMaintenanceCostTypes(data: CostTypeCategoryDto)
    suspend fun hasMaintenanceCostTypes(): Boolean
    suspend fun clearMaintenanceCostTypes()

    suspend fun getDriverCostTypes(): CostTypeCategoryDto?
    suspend fun saveDriverCostTypes(data: CostTypeCategoryDto)
    suspend fun hasDriverCostTypes(): Boolean
    suspend fun clearDriverCostTypes()

    suspend fun hasCostTypesCached(): Boolean
    suspend fun clearCache()
}

