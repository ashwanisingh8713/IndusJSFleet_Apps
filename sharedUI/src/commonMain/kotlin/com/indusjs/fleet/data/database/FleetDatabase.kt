package com.indusjs.fleet.data.database

import com.indusjs.fleet.data.database.dao.CostTypesDao
import com.indusjs.fleet.data.database.dao.DashboardDao
import com.indusjs.fleet.data.database.dao.SettingsCostTypesDao
import com.indusjs.fleet.data.database.dao.SettingsDashboardDao
import com.indusjs.fleet.data.database.dao.SettingsTeamMembersDao
import com.indusjs.fleet.data.database.dao.TeamMembersDao
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

/**
 * Fleet Management Database using multiplatform-settings.
 * Provides offline caching support across all platforms (Android, iOS, Web).
 *
 * This uses the same Settings library already used in the project for UserLocalDataSource,
 * ensuring consistent cross-platform behavior without Room's platform limitations.
 */
class FleetDatabase(
    settings: Settings = Settings(),
    json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
) {
    private val dashboardDaoImpl = SettingsDashboardDao(settings, json)
    private val costTypesDaoImpl = SettingsCostTypesDao(settings, json)
    private val teamMembersDaoImpl = SettingsTeamMembersDao(settings, json)

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
     * Team Members DAO for caching team members data.
     * Team members are synced from API and stored locally for offline access.
     */
    fun teamMembersDao(): TeamMembersDao = teamMembersDaoImpl

    companion object {
        const val DATABASE_NAME = "fleet_database"
    }
}

