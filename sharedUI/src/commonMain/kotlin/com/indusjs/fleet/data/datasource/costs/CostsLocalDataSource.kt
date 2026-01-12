package com.indusjs.fleet.data.datasource.costs

import co.touchlab.kermit.Logger
import com.indusjs.fleet.core.util.currentTimeMillis
import com.indusjs.fleet.data.database.dao.CostTypesDao
import com.indusjs.fleet.data.database.entity.DriverCostTypesEntity
import com.indusjs.fleet.data.database.entity.MaintenanceCostTypesEntity
import com.indusjs.fleet.data.database.entity.TripCostTypesEntity
import com.indusjs.fleet.data.datasource.LocalDataSource
import com.indusjs.fleet.data.model.costs.CostTypeCategoryDto
import com.indusjs.fleet.data.model.costs.CostTypeGroupDto
import dev.zacsweers.metro.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Local data source interface for Cost Types caching.
 * Abstracts the underlying storage mechanism (Settings-based).
 *
 * Cost types are fetched on first app launch and can be refreshed manually.
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

/**
 * Implementation of CostsLocalDataSource using Settings-based DAO.
 */
@Inject
class CostsLocalDataSourceImpl(
    private val costTypesDao: CostTypesDao,
    private val json: Json
) : CostsLocalDataSource {

    private val log = Logger.withTag("CostsLocalDataSource")

    override suspend fun getTripCostTypes(): CostTypeCategoryDto? {
        return try {
            costTypesDao.getTripCostTypes()?.let { entity ->
                val groups = json.decodeFromString<List<CostTypeGroupDto>>(entity.groupsJson)
                CostTypeCategoryDto(
                    categoryId = entity.categoryId,
                    categoryName = entity.categoryName,
                    groups = groups
                )
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to get trip cost types from cache: ${e.message}" }
            null
        }
    }

    override suspend fun saveTripCostTypes(data: CostTypeCategoryDto) {
        try {
            val entity = TripCostTypesEntity(
                id = 1,
                categoryId = data.categoryId,
                categoryName = data.categoryName,
                groupsJson = json.encodeToString(data.groups),
                savedAt = currentTimeMillis()
            )
            costTypesDao.saveTripCostTypes(entity)
            log.d { "Saved trip cost types to cache" }
        } catch (e: Exception) {
            log.e(e) { "Failed to save trip cost types: ${e.message}" }
        }
    }

    override suspend fun hasTripCostTypes(): Boolean {
        return costTypesDao.hasTripCostTypes()
    }

    override suspend fun clearTripCostTypes() {
        costTypesDao.clearTripCostTypes()
        log.d { "Cleared trip cost types cache" }
    }

    override suspend fun getMaintenanceCostTypes(): CostTypeCategoryDto? {
        return try {
            costTypesDao.getMaintenanceCostTypes()?.let { entity ->
                val groups = json.decodeFromString<List<CostTypeGroupDto>>(entity.groupsJson)
                CostTypeCategoryDto(
                    categoryId = entity.categoryId,
                    categoryName = entity.categoryName,
                    groups = groups
                )
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to get maintenance cost types from cache: ${e.message}" }
            null
        }
    }

    override suspend fun saveMaintenanceCostTypes(data: CostTypeCategoryDto) {
        try {
            val entity = MaintenanceCostTypesEntity(
                id = 1,
                categoryId = data.categoryId,
                categoryName = data.categoryName,
                groupsJson = json.encodeToString(data.groups),
                savedAt = currentTimeMillis()
            )
            costTypesDao.saveMaintenanceCostTypes(entity)
            log.d { "Saved maintenance cost types to cache" }
        } catch (e: Exception) {
            log.e(e) { "Failed to save maintenance cost types: ${e.message}" }
        }
    }

    override suspend fun hasMaintenanceCostTypes(): Boolean {
        return costTypesDao.hasMaintenanceCostTypes()
    }

    override suspend fun clearMaintenanceCostTypes() {
        costTypesDao.clearMaintenanceCostTypes()
        log.d { "Cleared maintenance cost types cache" }
    }

    override suspend fun getDriverCostTypes(): CostTypeCategoryDto? {
        return try {
            costTypesDao.getDriverCostTypes()?.let { entity ->
                val groups = json.decodeFromString<List<CostTypeGroupDto>>(entity.groupsJson)
                CostTypeCategoryDto(
                    categoryId = entity.categoryId,
                    categoryName = entity.categoryName,
                    groups = groups
                )
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to get driver cost types from cache: ${e.message}" }
            null
        }
    }

    override suspend fun saveDriverCostTypes(data: CostTypeCategoryDto) {
        try {
            val entity = DriverCostTypesEntity(
                id = 1,
                categoryId = data.categoryId,
                categoryName = data.categoryName,
                groupsJson = json.encodeToString(data.groups),
                savedAt = currentTimeMillis()
            )
            costTypesDao.saveDriverCostTypes(entity)
            log.d { "Saved driver cost types to cache" }
        } catch (e: Exception) {
            log.e(e) { "Failed to save driver cost types: ${e.message}" }
        }
    }

    override suspend fun hasDriverCostTypes(): Boolean {
        return costTypesDao.hasDriverCostTypes()
    }

    override suspend fun clearDriverCostTypes() {
        costTypesDao.clearDriverCostTypes()
        log.d { "Cleared driver cost types cache" }
    }

    override suspend fun hasCostTypesCached(): Boolean {
        return costTypesDao.hasCostTypesCached()
    }

    override suspend fun clearCache() {
        costTypesDao.clearCache()
        log.d { "Cleared all cost types cache" }
    }
}
