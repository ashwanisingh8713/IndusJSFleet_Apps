package com.indusjs.fleet.data.database

import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.data.database.dao.CostTypesDao
import com.indusjs.fleet.data.database.dao.CustomerDao
import com.indusjs.fleet.data.database.dao.DashboardDao
import com.indusjs.fleet.data.database.dao.SettingsCostTypesDao
import com.indusjs.fleet.data.database.dao.SettingsCustomerDao
import com.indusjs.fleet.data.database.dao.SettingsDashboardDao
import com.indusjs.fleet.data.database.dao.SettingsStatesDao
import com.indusjs.fleet.data.database.dao.SettingsTeamMembersDao
import com.indusjs.fleet.data.database.dao.StatesDao
import com.indusjs.fleet.data.database.dao.TeamMembersDao
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

/**
 * Fleet Management Database using multiplatform-settings.
 * Provides offline caching support across all platforms (Android, iOS, Web).
 *
 * This uses the same Settings library already used in the project for UserLocalDataSource,
 * ensuring consistent cross-platform behavior without Room's platform limitations.
 *
 * IMPORTANT: The Settings instance MUST be injected from DI to ensure
 * the same instance is used throughout the app.
 */
class FleetDatabase(
    settings: Settings,
    json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
    logger: FleetLogger
) {
    init {
        // Cache-version gate. When the on-wire/cached timestamp representation
        // changes (e.g. ISO-8601 String -> UTC epoch-millis Long), old JSON
        // blobs can no longer be deserialized into the new entity shapes.
        // Bumping CACHE_SCHEMA_VERSION drops every known cache key on first
        // launch so we start clean. No data migration (dev phase).
        val storedVersion = settings.getIntOrNull(KEY_CACHE_SCHEMA_VERSION)
        if (storedVersion != CACHE_SCHEMA_VERSION) {
            logger.i(
                "FleetDatabase",
                "Cache schema $storedVersion -> $CACHE_SCHEMA_VERSION; clearing local caches"
            )
            CACHE_KEYS.forEach { settings.remove(it) }
            settings.putInt(KEY_CACHE_SCHEMA_VERSION, CACHE_SCHEMA_VERSION)
        }
    }

    private val dashboardDaoImpl = SettingsDashboardDao(settings, json)
    private val costTypesDaoImpl = SettingsCostTypesDao(settings, json)
    private val statesDaoImpl = SettingsStatesDao(settings, json)
    private val teamMembersDaoImpl = SettingsTeamMembersDao(settings, json, logger)
    private val customerDaoImpl = SettingsCustomerDao(settings, json)

    /**
     * Dashboard DAO for caching dashboard data.
     */
    fun dashboardDao(): DashboardDao = dashboardDaoImpl

    /**
     * Cost Types DAO for caching trip and maintenance cost types.
     * Cost types are fetched once on first app launch and persisted forever.
     */
    fun costTypesDao(): CostTypesDao = costTypesDaoImpl

    /**
     * States DAO for caching entity states (vehicle, driver, trip, payment).
     * States are fetched once on first app launch and persisted.
     */
    fun statesDao(): StatesDao = statesDaoImpl

    /**
     * Team Members DAO for caching team members data.
     * Team members are synced from API and stored locally for offline access.
     */
    fun teamMembersDao(): TeamMembersDao = teamMembersDaoImpl

    /**
     * Customer DAO for caching customer data.
     * Customers are synced from API and stored locally for offline access.
     */
    fun customerDao(): CustomerDao = customerDaoImpl

    companion object {
        const val DATABASE_NAME = "fleet_database"

        /**
         * Local cache schema version. Bump this whenever a cached entity's
         * shape changes in a non-backward-compatible way (e.g. the UTC
         * epoch-millis migration: timestamp String -> Long). On mismatch,
         * [CACHE_KEYS] are cleared on first launch.
         */
        private const val CACHE_SCHEMA_VERSION = 3
        private const val KEY_CACHE_SCHEMA_VERSION = "cache_schema_version"

        /**
         * Every multiplatform-settings key holding a serialized cache blob.
         * Keep in sync with the per-DAO key constants. Auth/session keys are
         * intentionally excluded so the user stays logged in.
         */
        private val CACHE_KEYS = listOf(
            "dashboard_cache",
            "dashboard_cache_time",
            "trip_cost_types_cache",
            "maintenance_cost_types_cache",
            "driver_cost_types_cache",
            "entity_states_cache",
            "team_members_cache",
            "customers_cache"
        )
    }
}

