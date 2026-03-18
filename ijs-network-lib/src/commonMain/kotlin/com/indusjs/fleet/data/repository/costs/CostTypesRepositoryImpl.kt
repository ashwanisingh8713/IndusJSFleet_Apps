package com.indusjs.fleet.data.repository.costs

import co.touchlab.kermit.Logger
import com.indusjs.error.result.Result
import com.indusjs.fleet.data.datasource.costs.CostsLocalDataSource
import com.indusjs.fleet.data.datasource.costs.CostsRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.model.costs.CostTypeCategoryDto
import com.indusjs.fleet.data.model.costs.CostTypeGroupDto
import com.indusjs.fleet.data.model.costs.MaintenanceCostTypes
import com.indusjs.fleet.data.model.costs.TripCostTypes
import com.indusjs.fleet.data.model.costs.toFlatList
import com.indusjs.fleet.data.model.driver.DriverCostTypes
import com.indusjs.fleet.domain.repository.costs.CostTypesRepository
import dev.zacsweers.metro.Inject

/**
 * Implementation of CostTypesRepository.
 *
 * Handles fetching cost types from API and caching them locally.
 * Cost types are fetched on first app launch and can be refreshed manually.
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
        var driverTypesFetched = false

        // Fetch trip cost types
        try {
            val tripResponse = remoteDataSource.getTripCostTypes()
            log.d { "Trip cost types API response: success=${tripResponse.success}, message=${tripResponse.message}" }
            val tripData = tripResponse.data
            if (tripResponse.success && tripData != null) {
                log.d { "Trip cost types data: ${tripData.groups.size} groups" }
                localDataSource.saveTripCostTypes(tripData)
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
            val maintenanceData = maintenanceResponse.data
            if (maintenanceResponse.success && maintenanceData != null) {
                log.d { "Maintenance cost types data: ${maintenanceData.groups.size} groups" }
                localDataSource.saveMaintenanceCostTypes(maintenanceData)
                maintenanceTypesFetched = true
                log.d { "Maintenance cost types fetched and cached successfully" }
            } else {
                log.w { "Failed to fetch maintenance cost types: ${maintenanceResponse.message}" }
            }
        } catch (e: Exception) {
            log.e(e) { "Error fetching maintenance cost types: ${e.message}" }
        }

        // Fetch driver cost types
        try {
            val driverResponse = remoteDataSource.getDriverCostTypes()
            log.d { "Driver cost types API response: success=${driverResponse.success}, message=${driverResponse.message}" }
            val driverData = driverResponse.data
            if (driverResponse.success && driverData != null) {
                log.d { "Driver cost types data: ${driverData.groups.size} groups" }
                localDataSource.saveDriverCostTypes(driverData)
                driverTypesFetched = true
                log.d { "Driver cost types fetched and cached successfully" }
            } else {
                log.w { "Failed to fetch driver cost types: ${driverResponse.message}" }
            }
        } catch (e: Exception) {
            log.e(e) { "Error fetching driver cost types: ${e.message}" }
        }

        return if (tripTypesFetched && maintenanceTypesFetched) {
            Result.Success(Unit)
        } else if (tripTypesFetched || maintenanceTypesFetched || driverTypesFetched) {
            log.w { "Partial success: trip=$tripTypesFetched, maintenance=$maintenanceTypesFetched, driver=$driverTypesFetched" }
            Result.Success(Unit)
        } else {
            log.w { "Failed to fetch any cost types, will use hardcoded fallback" }
            Result.Success(Unit)
        }
    }

    override suspend fun refreshTripCostTypes(): Result<Unit> {
        log.d { "Refreshing trip cost types from API..." }

        try {
            val response = remoteDataSource.getTripCostTypes()
            val data = response.data
            if (response.success && data != null) {
                localDataSource.clearTripCostTypes()
                localDataSource.saveTripCostTypes(data)
                log.d { "Trip cost types refreshed successfully: ${data.groups.size} groups" }
                return Result.Success(Unit)
            } else {
                log.w { "Failed to refresh trip cost types: ${response.message}" }
                return Result.Error(Exception(response.message ?: "Failed to fetch trip cost types"))
            }
        } catch (e: Exception) {
            log.e(e) { "Error refreshing trip cost types: ${e.message}" }
            return Result.Error(e, "Failed to refresh trip cost types: ${e.message}")
        }
    }

    override suspend fun refreshMaintenanceCostTypes(): Result<Unit> {
        log.d { "Refreshing maintenance cost types from API..." }

        try {
            val response = remoteDataSource.getMaintenanceCostTypes()
            val data = response.data
            if (response.success && data != null) {
                localDataSource.clearMaintenanceCostTypes()
                localDataSource.saveMaintenanceCostTypes(data)
                log.d { "Maintenance cost types refreshed successfully: ${data.groups.size} groups" }
                return Result.Success(Unit)
            } else {
                log.w { "Failed to refresh maintenance cost types: ${response.message}" }
                return Result.Error(Exception(response.message ?: "Failed to fetch maintenance cost types"))
            }
        } catch (e: Exception) {
            log.e(e) { "Error refreshing maintenance cost types: ${e.message}" }
            return Result.Error(e, "Failed to refresh maintenance cost types: ${e.message}")
        }
    }

    override suspend fun refreshDriverCostTypes(): Result<Unit> {
        log.d { "Refreshing driver cost types from API..." }

        try {
            val response = remoteDataSource.getDriverCostTypes()
            val data = response.data
            if (response.success && data != null) {
                localDataSource.clearDriverCostTypes()
                localDataSource.saveDriverCostTypes(data)
                log.d { "Driver cost types refreshed successfully: ${data.groups.size} groups" }
                return Result.Success(Unit)
            } else {
                log.w { "Failed to refresh driver cost types: ${response.message}" }
                return Result.Error(Exception(response.message ?: "Failed to fetch driver cost types"))
            }
        } catch (e: Exception) {
            log.e(e) { "Error refreshing driver cost types: ${e.message}" }
            return Result.Error(e, "Failed to refresh driver cost types: ${e.message}")
        }
    }

    override suspend fun getTripCostTypes(): CostTypeCategoryDto? {
        return localDataSource.getTripCostTypes()
    }

    override suspend fun getMaintenanceCostTypes(): CostTypeCategoryDto? {
        return localDataSource.getMaintenanceCostTypes()
    }

    override suspend fun getDriverCostTypes(): CostTypeCategoryDto? {
        return localDataSource.getDriverCostTypes()
    }

    override suspend fun getTripCostTypesFlat(): List<Pair<String, String>> {
        val cached = localDataSource.getTripCostTypes()
        log.d { "getTripCostTypesFlat: cached=${cached != null}, groups=${cached?.groups?.size ?: 0}" }
        val result = cached?.toFlatList() ?: groupsToFlatList(TripCostTypes.groups)
        log.d { "getTripCostTypesFlat: returning ${result.size} items" }
        return result
    }

    override suspend fun getMaintenanceCostTypesFlat(): List<Pair<String, String>> {
        val cached = localDataSource.getMaintenanceCostTypes()
        log.d { "getMaintenanceCostTypesFlat: cached=${cached != null}, groups=${cached?.groups?.size ?: 0}" }
        val result = cached?.toFlatList() ?: groupsToFlatList(MaintenanceCostTypes.groups)
        log.d { "getMaintenanceCostTypesFlat: returning ${result.size} items" }
        return result
    }

    override suspend fun getDriverCostTypesFlat(): List<Pair<String, String>> {
        val cached = localDataSource.getDriverCostTypes()
        log.d { "getDriverCostTypesFlat: cached=${cached != null}, groups=${cached?.groups?.size ?: 0}" }
        val result = cached?.toFlatList() ?: DriverCostTypes.types
        log.d { "getDriverCostTypesFlat: returning ${result.size} items" }
        return result
    }

    override suspend fun hasCostTypesCached(): Boolean {
        return localDataSource.hasCostTypesCached()
    }

    /**
     * Convert a list of CostTypeGroupDto to a flat list of (id, label) pairs.
     */
    private fun groupsToFlatList(groups: List<CostTypeGroupDto>): List<Pair<String, String>> {
        return groups.flatMap { group ->
            group.items.map { item -> item.id to item.label }
        }
    }
}
