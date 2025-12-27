package com.indusjs.fleet.data.database.dao

import com.indusjs.fleet.data.database.entity.DashboardCacheEntity
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Data Access Object for Dashboard cache operations.
 */
interface DashboardDao {
    suspend fun getDashboardCache(): DashboardCacheEntity?
    fun observeDashboardCache(): Flow<DashboardCacheEntity?>
    suspend fun insertOrUpdate(cache: DashboardCacheEntity)
    suspend fun clearCache()
    suspend fun hasCachedData(): Boolean
    suspend fun getCacheTime(): Long?
}

/**
 * Settings-based implementation of DashboardDao.
 * Uses multiplatform-settings for cross-platform persistence.
 */
class SettingsDashboardDao(
    private val settings: Settings,
    private val json: Json
) : DashboardDao {

    private val cacheFlow = MutableStateFlow<DashboardCacheEntity?>(null)

    init {
        // Load cached data on initialization
        loadFromSettings()
    }

    private fun loadFromSettings() {
        settings.getStringOrNull(KEY_DASHBOARD_CACHE)?.let { cached ->
            try {
                cacheFlow.value = json.decodeFromString<DashboardCacheEntity>(cached)
            } catch (_: Exception) {
                // Invalid cache, ignore
            }
        }
    }

    override suspend fun getDashboardCache(): DashboardCacheEntity? {
        return cacheFlow.value ?: run {
            loadFromSettings()
            cacheFlow.value
        }
    }

    override fun observeDashboardCache(): Flow<DashboardCacheEntity?> = cacheFlow

    override suspend fun insertOrUpdate(cache: DashboardCacheEntity) {
        try {
            settings.putString(KEY_DASHBOARD_CACHE, json.encodeToString(cache))
            settings.putLong(KEY_CACHE_TIME, cache.cachedAt)
            cacheFlow.value = cache
        } catch (e: Exception) {
            println("Failed to cache dashboard: ${e.message}")
        }
    }

    override suspend fun clearCache() {
        settings.remove(KEY_DASHBOARD_CACHE)
        settings.remove(KEY_CACHE_TIME)
        cacheFlow.value = null
    }

    override suspend fun hasCachedData(): Boolean {
        return cacheFlow.value != null || settings.getStringOrNull(KEY_DASHBOARD_CACHE) != null
    }

    override suspend fun getCacheTime(): Long? = settings.getLongOrNull(KEY_CACHE_TIME)

    companion object {
        private const val KEY_DASHBOARD_CACHE = "dashboard_cache"
        private const val KEY_CACHE_TIME = "dashboard_cache_time"
    }
}

