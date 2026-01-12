package com.indusjs.fleet.data.database.dao

import com.indusjs.fleet.data.database.entity.MaintenanceCostTypesEntity
import com.indusjs.fleet.data.database.entity.TripCostTypesEntity
import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Data Access Object for Cost Types cache operations.
 *
 * Cost types are fetched once on first app launch and persisted forever.
 * They are never updated again unless the app data is cleared.
 */
interface CostTypesDao {
    suspend fun getTripCostTypes(): TripCostTypesEntity?
    suspend fun saveTripCostTypes(entity: TripCostTypesEntity)
    suspend fun hasTripCostTypes(): Boolean

    suspend fun getMaintenanceCostTypes(): MaintenanceCostTypesEntity?
    suspend fun saveMaintenanceCostTypes(entity: MaintenanceCostTypesEntity)
    suspend fun hasMaintenanceCostTypes(): Boolean

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

    override suspend fun hasCostTypesCached(): Boolean {
        return hasTripCostTypes() && hasMaintenanceCostTypes()
    }

    override suspend fun clearCache() {
        settings.remove(KEY_TRIP_COST_TYPES)
        settings.remove(KEY_MAINTENANCE_COST_TYPES)
    }

    companion object {
        private const val KEY_TRIP_COST_TYPES = "trip_cost_types_cache"
        private const val KEY_MAINTENANCE_COST_TYPES = "maintenance_cost_types_cache"
    }
}

