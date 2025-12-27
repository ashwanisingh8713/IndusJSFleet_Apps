package com.indusjs.fleet.data.mapper.dashboard

import com.indusjs.fleet.core.util.currentTimeMillis
import com.indusjs.fleet.data.database.entity.DashboardCacheEntity
import com.indusjs.fleet.data.model.dashboard.*
import dev.zacsweers.metro.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Mapper for converting between DashboardCacheEntity and DashboardDataDto.
 *
 * Handles:
 * - Flat field mapping for simple types
 * - JSON serialization/deserialization for complex nested objects
 * - Safe parsing with fallbacks for corrupted cache
 */
@Inject
class DashboardCacheMapper(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
) {

    /**
     * Convert DTO (from API) to Entity (for storage).
     */
    fun toEntity(dto: DashboardDataDto): DashboardCacheEntity {
        return DashboardCacheEntity(
            id = 1,
            // User Info
            userId = dto.userInfo.id,
            firstName = dto.userInfo.firstName,
            lastName = dto.userInfo.lastName,
            userRole = dto.userInfo.role,
            email = dto.userInfo.email,
            // Fleet Overview
            totalVehicles = dto.fleetOverview.totalVehicles,
            activeVehicles = dto.fleetOverview.activeVehicles,
            maintenanceVehicles = dto.fleetOverview.maintenanceVehicles,
            inactiveVehicles = dto.fleetOverview.inactiveVehicles,
            totalDrivers = dto.fleetOverview.totalDrivers,
            activeDrivers = dto.fleetOverview.activeDrivers,
            driversOnTrip = dto.fleetOverview.driversOnTrip,
            driversOnLeave = dto.fleetOverview.driversOnLeave,
            totalTrips = dto.fleetOverview.totalTrips,
            ongoingTrips = dto.fleetOverview.ongoingTrips,
            plannedTrips = dto.fleetOverview.plannedTrips,
            completedTrips = dto.fleetOverview.completedTrips,
            totalDistance = dto.fleetOverview.totalDistance,
            // Today Summary
            completedTripsToday = dto.todaySummary.completedTripsToday,
            totalDistanceToday = dto.todaySummary.totalDistanceToday,
            fuelConsumption = dto.todaySummary.fuelConsumption,
            activeVehiclesNow = dto.todaySummary.activeVehiclesNow,
            newTripsToday = dto.todaySummary.newTripsToday,
            alertsCount = dto.todaySummary.alertsCount,
            // Complex objects as JSON
            alertsJson = json.encodeToString(dto.alerts),
            totalAlerts = dto.totalAlerts,
            quickActionsJson = json.encodeToString(dto.quickActions),
            liveStatusJson = json.encodeToString(dto.liveStatus),
            teamStatsJson = dto.teamStats?.let { json.encodeToString(it) },
            documentStatsJson = dto.documentStats?.let { json.encodeToString(it) },
            // Metadata
            lastUpdated = dto.lastUpdated,
            cachedAt = currentTimeMillis()
        )
    }

    /**
     * Convert Entity to DTO.
     * Uses safe parsing with fallbacks for corrupted data.
     */
    fun toDto(entity: DashboardCacheEntity): DashboardDataDto {
        return DashboardDataDto(
            userInfo = UserInfoDto(
                id = entity.userId,
                firstName = entity.firstName,
                lastName = entity.lastName,
                role = entity.userRole,
                email = entity.email
            ),
            fleetOverview = FleetOverviewDto(
                totalVehicles = entity.totalVehicles,
                activeVehicles = entity.activeVehicles,
                maintenanceVehicles = entity.maintenanceVehicles,
                inactiveVehicles = entity.inactiveVehicles,
                totalDrivers = entity.totalDrivers,
                activeDrivers = entity.activeDrivers,
                driversOnTrip = entity.driversOnTrip,
                driversOnLeave = entity.driversOnLeave,
                totalTrips = entity.totalTrips,
                ongoingTrips = entity.ongoingTrips,
                plannedTrips = entity.plannedTrips,
                completedTrips = entity.completedTrips,
                totalDistance = entity.totalDistance
            ),
            todaySummary = TodaySummaryDto(
                completedTripsToday = entity.completedTripsToday,
                totalDistanceToday = entity.totalDistanceToday,
                fuelConsumption = entity.fuelConsumption,
                activeVehiclesNow = entity.activeVehiclesNow,
                newTripsToday = entity.newTripsToday,
                alertsCount = entity.alertsCount
            ),
            alerts = safeDecodeList(entity.alertsJson),
            totalAlerts = entity.totalAlerts,
            quickActions = safeDecode(entity.quickActionsJson) ?: QuickActionsDto(
                vehiclesCount = entity.totalVehicles,
                driversCount = entity.totalDrivers,
                tripsCount = entity.totalTrips,
                alertsCount = entity.totalAlerts
            ),
            liveStatus = safeDecode(entity.liveStatusJson) ?: LiveStatusDto(),
            teamStats = entity.teamStatsJson?.let { safeDecode<TeamStatsDto>(it) },
            documentStats = entity.documentStatsJson?.let { safeDecode<DocumentStatsDto>(it) },
            lastUpdated = entity.lastUpdated
        )
    }

    private inline fun <reified T> safeDecode(jsonString: String): T? {
        return try {
            json.decodeFromString<T>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    private inline fun <reified T> safeDecodeList(jsonString: String): List<T> {
        return try {
            json.decodeFromString<List<T>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}


