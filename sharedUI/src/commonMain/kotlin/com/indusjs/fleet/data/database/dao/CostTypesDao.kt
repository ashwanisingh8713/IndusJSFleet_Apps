package com.indusjs.fleet.data.database.dao

import com.indusjs.fleet.data.database.entity.DriverCostTypesEntity
import com.indusjs.fleet.data.database.entity.MaintenanceCostTypesEntity
import com.indusjs.fleet.data.database.entity.TripCostTypesEntity
import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Data Access Object for Cost Types cache operations.
 *
 * Cost types are fetched on first app launch and persisted.
 * They can be refreshed manually from the cost entry screens.
 */
interface CostTypesDao {
    suspend fun getTripCostTypes(): TripCostTypesEntity?
    suspend fun saveTripCostTypes(entity: TripCostTypesEntity)
    suspend fun hasTripCostTypes(): Boolean
    suspend fun clearTripCostTypes()

    suspend fun getMaintenanceCostTypes(): MaintenanceCostTypesEntity?
    suspend fun saveMaintenanceCostTypes(entity: MaintenanceCostTypesEntity)
    suspend fun hasMaintenanceCostTypes(): Boolean
    suspend fun clearMaintenanceCostTypes()

    suspend fun getDriverCostTypes(): DriverCostTypesEntity?
    suspend fun saveDriverCostTypes(entity: DriverCostTypesEntity)
    suspend fun hasDriverCostTypes(): Boolean
    suspend fun clearDriverCostTypes()

    suspend fun hasCostTypesCached(): Boolean
    suspend fun clearCache()
}

/**
 * Settings-based implementation of CostTypesDao.
 * Uses multiplatform-settings for cross-platform persistence.
 */
class SettingsCostTypesDao(
    private val settings: Settings,
    private val json: Json
) : CostTypesDao {

    override suspend fun getTripCostTypes(): TripCostTypesEntity? {
        return settings.getStringOrNull(KEY_TRIP_COST_TYPES)?.let { cached ->
            try {
                json.decodeFromString<TripCostTypesEntity>(cached)
            } catch (_: Exception) {
                null
            }
        }
    }

    override suspend fun saveTripCostTypes(entity: TripCostTypesEntity) {
        try {
            settings.putString(KEY_TRIP_COST_TYPES, json.encodeToString(entity))
        } catch (e: Exception) {
            println("Failed to save trip cost types: ${e.message}")
        }
    }

    override suspend fun hasTripCostTypes(): Boolean {
        return settings.getStringOrNull(KEY_TRIP_COST_TYPES) != null
    }

    override suspend fun clearTripCostTypes() {
        settings.remove(KEY_TRIP_COST_TYPES)
    }

    override suspend fun getMaintenanceCostTypes(): MaintenanceCostTypesEntity? {
        return settings.getStringOrNull(KEY_MAINTENANCE_COST_TYPES)?.let { cached ->
            try {
                json.decodeFromString<MaintenanceCostTypesEntity>(cached)
            } catch (_: Exception) {
                null
            }
        }
    }

    override suspend fun saveMaintenanceCostTypes(entity: MaintenanceCostTypesEntity) {
        try {
            settings.putString(KEY_MAINTENANCE_COST_TYPES, json.encodeToString(entity))
        } catch (e: Exception) {
            println("Failed to save maintenance cost types: ${e.message}")
        }
    }

    override suspend fun hasMaintenanceCostTypes(): Boolean {
        return settings.getStringOrNull(KEY_MAINTENANCE_COST_TYPES) != null
    }

    override suspend fun clearMaintenanceCostTypes() {
        settings.remove(KEY_MAINTENANCE_COST_TYPES)
    }

    override suspend fun getDriverCostTypes(): DriverCostTypesEntity? {
        return settings.getStringOrNull(KEY_DRIVER_COST_TYPES)?.let { cached ->
            try {
                json.decodeFromString<DriverCostTypesEntity>(cached)
            } catch (_: Exception) {
                null
            }
        }
    }

    override suspend fun saveDriverCostTypes(entity: DriverCostTypesEntity) {
        try {
            settings.putString(KEY_DRIVER_COST_TYPES, json.encodeToString(entity))
        } catch (e: Exception) {
            println("Failed to save driver cost types: ${e.message}")
        }
    }

    override suspend fun hasDriverCostTypes(): Boolean {
        return settings.getStringOrNull(KEY_DRIVER_COST_TYPES) != null
    }

    override suspend fun clearDriverCostTypes() {
        settings.remove(KEY_DRIVER_COST_TYPES)
    }

    override suspend fun hasCostTypesCached(): Boolean {
        return hasTripCostTypes() && hasMaintenanceCostTypes()
    }

    override suspend fun clearCache() {
        settings.remove(KEY_TRIP_COST_TYPES)
        settings.remove(KEY_MAINTENANCE_COST_TYPES)
        settings.remove(KEY_DRIVER_COST_TYPES)
    }

    companion object {
        private const val KEY_TRIP_COST_TYPES = "trip_cost_types_cache"
        private const val KEY_MAINTENANCE_COST_TYPES = "maintenance_cost_types_cache"
        private const val KEY_DRIVER_COST_TYPES = "driver_cost_types_cache"
    }
}
