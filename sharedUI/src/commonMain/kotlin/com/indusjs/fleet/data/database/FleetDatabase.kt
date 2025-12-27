package com.indusjs.fleet.data.database

import com.indusjs.fleet.data.database.dao.DashboardDao
import com.indusjs.fleet.data.database.dao.SettingsDashboardDao
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

    /**
     * Dashboard DAO for caching dashboard data.
     */
    fun dashboardDao(): DashboardDao = dashboardDaoImpl

    companion object {
        const val DATABASE_NAME = "fleet_database"
    }
}

