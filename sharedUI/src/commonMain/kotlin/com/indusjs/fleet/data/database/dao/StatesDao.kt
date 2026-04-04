package com.indusjs.fleet.data.database.dao

import com.indusjs.fleet.data.database.entity.StatesEntity
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

/**
 * Data Access Object for Entity States cache operations.
 *
 * States are fetched on first app launch and persisted.
 * They can be refreshed manually if needed.
 */
interface StatesDao {
    suspend fun getStates(): StatesEntity?
    suspend fun saveStates(entity: StatesEntity)
    suspend fun hasStates(): Boolean
    suspend fun clearStates()
}

/**
 * Settings-based implementation of StatesDao.
 * Uses multiplatform-settings for cross-platform persistence.
 */
class SettingsStatesDao(
    private val settings: Settings,
    private val json: Json
) : StatesDao {

    override suspend fun getStates(): StatesEntity? {
        return settings.getStringOrNull(KEY_STATES_CACHE)?.let { cached ->
            try {
                json.decodeFromString<StatesEntity>(cached)
            } catch (_: Exception) {
                null
            }
        }
    }

    override suspend fun saveStates(entity: StatesEntity) {
        try {
            settings.putString(KEY_STATES_CACHE, json.encodeToString(entity))
        } catch (e: Exception) {
            println("Failed to save states: ${e.message}")
        }
    }

    override suspend fun hasStates(): Boolean {
        return settings.getStringOrNull(KEY_STATES_CACHE) != null
    }

    override suspend fun clearStates() {
        settings.remove(KEY_STATES_CACHE)
    }

    companion object {
        private const val KEY_STATES_CACHE = "entity_states_cache"
    }
}

