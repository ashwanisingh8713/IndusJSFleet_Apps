package com.indusjs.fleet.data.repository.costs

import co.touchlab.kermit.Logger
import com.indusjs.error.result.Result
import com.indusjs.fleet.data.datasource.costs.CostsLocalDataSource
import com.indusjs.fleet.data.datasource.costs.CostsRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.model.costs.CostTypeCategoryDto
import com.indusjs.fleet.data.model.costs.MaintenanceCostTypes
import com.indusjs.fleet.data.model.costs.TripCostTypes
import com.indusjs.fleet.domain.repository.costs.CostTypesRepository
import dev.zacsweers.metro.Inject

/**
 * Implementation of CostTypesRepository.
 *
 * Handles fetching cost types from API and caching them locally.
 * Cost types are fetched once on first app launch and never updated.
 * Falls back to hardcoded types if API fails and no cache exists.
 */
@Inject
class CostTypesRepositoryImpl(
    private val remoteDataSource: CostsRemoteDataSource,
    private val localDataSource: CostsLocalDataSource,
    private val userLocalDataSource: UserLocalDataSource? = null
) : CostTypesRepository {

    private val log = Logger.withTag("CostTypesRepository")

    override suspend fun initializeCostTypesIfNeeded(): Result<Unit> {
        // Check if already cached - if so, validate the cache first
        if (localDataSource.hasCostTypesCached()) {
            // Validate cached data is not empty/corrupt
            val tripTypes = localDataSource.getTripCostTypes()
            val maintenanceTypes = localDataSource.getMaintenanceCostTypes()

            if (tripTypes != null && tripTypes.groups.isNotEmpty() &&
                maintenanceTypes != null && maintenanceTypes.groups.isNotEmpty()) {
                log.d { "Cost types already cached and valid, skipping initialization" }
                log.d { "Cached trip types: ${tripTypes.groups.size} groups, ${tripTypes.toFlatList().size} items" }
                log.d { "Cached maintenance types: ${maintenanceTypes.groups.size} groups, ${maintenanceTypes.toFlatList().size} items" }
                return Result.Success(Unit)
            } else {
                log.w { "Cached cost types are empty or corrupt, clearing and refetching..." }
                localDataSource.clearCache()
            }
        }

        // Check if user is logged in before making API calls
        // This prevents 401 errors from clearing the session on app restart
        val authToken = userLocalDataSource?.getAuthToken()
        if (authToken == null) {
            log.d { "User not logged in, skipping cost types fetch from API" }
            return Result.Success(Unit)
        }

        log.d { "Cost types not cached, fetching from API..." }

        var tripTypesFetched = false
        var maintenanceTypesFetched = false

        // Fetch trip cost types
        try {
            val tripResponse = remoteDataSource.getTripCostTypes()
            log.d { "Trip cost types API response: success=${tripResponse.success}, message=${tripResponse.message}" }
            if (tripResponse.success && tripResponse.data != null) {
                log.d { "Trip cost types data: ${tripResponse.data.groups.size} groups" }
                tripResponse.data.groups.forEach { group ->
                    log.d { "  Group: ${group.groupName} - ${group.items.size} items" }
                }
                localDataSource.saveTripCostTypes(tripResponse.data)
                tripTypesFetched = true
                log.d { "Trip cost types fetched and cached successfully" }
            } else {
                log.w { "Failed to fetch trip cost types: ${tripResponse.message}" }
            }
        } catch (e: Exception) {
            log.e(e) { "Error fetching trip cost types: ${e.message}" }
        }

        // Fetch maintenance cost types
        try {
            val maintenanceResponse = remoteDataSource.getMaintenanceCostTypes()
            log.d { "Maintenance cost types API response: success=${maintenanceResponse.success}, message=${maintenanceResponse.message}" }
            if (maintenanceResponse.success && maintenanceResponse.data != null) {
                log.d { "Maintenance cost types data: ${maintenanceResponse.data.groups.size} groups" }
                maintenanceResponse.data.groups.forEach { group ->
                    log.d { "  Group: ${group.groupName} - ${group.items.size} items" }
                }
                localDataSource.saveMaintenanceCostTypes(maintenanceResponse.data)
                maintenanceTypesFetched = true
                log.d { "Maintenance cost types fetched and cached successfully" }
            } else {
                log.w { "Failed to fetch maintenance cost types: ${maintenanceResponse.message}" }
            }
        } catch (e: Exception) {
            log.e(e) { "Error fetching maintenance cost types: ${e.message}" }
        }

        return if (tripTypesFetched && maintenanceTypesFetched) {
            Result.Success(Unit)
        } else if (tripTypesFetched || maintenanceTypesFetched) {
            log.w { "Partial success: trip=$tripTypesFetched, maintenance=$maintenanceTypesFetched" }
            Result.Success(Unit)
        } else {
            log.w { "Failed to fetch any cost types, will use hardcoded fallback" }
            Result.Success(Unit)
        }
    }

    override suspend fun getTripCostTypes(): CostTypeCategoryDto? {
        return localDataSource.getTripCostTypes()
    }

    override suspend fun getMaintenanceCostTypes(): CostTypeCategoryDto? {
        return localDataSource.getMaintenanceCostTypes()
    }

    override suspend fun getTripCostTypesFlat(): List<Pair<String, String>> {
        val cached = localDataSource.getTripCostTypes()
        log.d { "getTripCostTypesFlat: cached=${cached != null}, groups=${cached?.groups?.size ?: 0}" }
        val result = cached?.toFlatList() ?: TripCostTypes.types
        log.d { "getTripCostTypesFlat: returning ${result.size} items" }
        return result
    }

    override suspend fun getMaintenanceCostTypesFlat(): List<Pair<String, String>> {
        val cached = localDataSource.getMaintenanceCostTypes()
        log.d { "getMaintenanceCostTypesFlat: cached=${cached != null}, groups=${cached?.groups?.size ?: 0}" }
        val result = cached?.toFlatList() ?: MaintenanceCostTypes.types
        log.d { "getMaintenanceCostTypesFlat: returning ${result.size} items" }
        return result
    }

    override suspend fun hasCostTypesCached(): Boolean {
        return localDataSource.hasCostTypesCached()
    }
}
