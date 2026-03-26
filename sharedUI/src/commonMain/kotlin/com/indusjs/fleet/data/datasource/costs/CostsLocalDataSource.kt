package com.indusjs.fleet.data.datasource.costs

import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.TAG_COSTS_LOCAL_DS
import com.indusjs.fleet.core.util.currentTimeMillis
import com.indusjs.fleet.data.database.dao.CostTypesDao
import com.indusjs.fleet.data.database.entity.DriverCostTypesEntity
import com.indusjs.fleet.data.database.entity.MaintenanceCostTypesEntity
import com.indusjs.fleet.data.database.entity.TripCostTypesEntity
import com.indusjs.fleet.data.model.costs.CostTypeCategoryDto
import com.indusjs.fleet.data.model.costs.CostTypeGroupDto
import dev.zacsweers.metro.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementation of CostsLocalDataSource using Room DAOs.
 *
 * The interface [CostsLocalDataSource] is defined in ijs-network-lib.
 */
@Inject
class CostsLocalDataSourceImpl(
    private val costTypesDao: CostTypesDao,
    private val json: Json,
    private val logger: FleetLogger
) : CostsLocalDataSource {

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
            logger.e(TAG_COSTS_LOCAL_DS, "Failed to get trip cost types from cache: ${e.message}", e)
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
            logger.d(TAG_COSTS_LOCAL_DS, "Saved trip cost types to cache")
        } catch (e: Exception) {
            logger.e(TAG_COSTS_LOCAL_DS, "Failed to save trip cost types: ${e.message}", e)
        }
    }

    override suspend fun hasTripCostTypes(): Boolean {
        return costTypesDao.hasTripCostTypes()
    }

    override suspend fun clearTripCostTypes() {
        costTypesDao.clearTripCostTypes()
        logger.d(TAG_COSTS_LOCAL_DS, "Cleared trip cost types cache")
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
            logger.e(TAG_COSTS_LOCAL_DS, "Failed to get maintenance cost types from cache: ${e.message}", e)
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
            logger.d(TAG_COSTS_LOCAL_DS, "Saved maintenance cost types to cache")
        } catch (e: Exception) {
            logger.e(TAG_COSTS_LOCAL_DS, "Failed to save maintenance cost types: ${e.message}", e)
        }
    }

    override suspend fun hasMaintenanceCostTypes(): Boolean {
        return costTypesDao.hasMaintenanceCostTypes()
    }

    override suspend fun clearMaintenanceCostTypes() {
        costTypesDao.clearMaintenanceCostTypes()
        logger.d(TAG_COSTS_LOCAL_DS, "Cleared maintenance cost types cache")
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
            logger.e(TAG_COSTS_LOCAL_DS, "Failed to get driver cost types from cache: ${e.message}", e)
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
            logger.d(TAG_COSTS_LOCAL_DS, "Saved driver cost types to cache")
        } catch (e: Exception) {
            logger.e(TAG_COSTS_LOCAL_DS, "Failed to save driver cost types: ${e.message}", e)
        }
    }

    override suspend fun hasDriverCostTypes(): Boolean {
        return costTypesDao.hasDriverCostTypes()
    }

    override suspend fun clearDriverCostTypes() {
        costTypesDao.clearDriverCostTypes()
        logger.d(TAG_COSTS_LOCAL_DS, "Cleared driver cost types cache")
    }

    override suspend fun hasCostTypesCached(): Boolean {
        return costTypesDao.hasCostTypesCached()
    }

    override suspend fun clearCache() {
        costTypesDao.clearCache()
        logger.d(TAG_COSTS_LOCAL_DS, "Cleared all cost types cache")
    }
}
