package com.indusjs.fleet.data.database.entity

import com.indusjs.fleet.data.model.DbEntity
import kotlinx.serialization.Serializable

/**
 * Data class for caching Dashboard data locally.
 *
 * Design decisions:
 * - Single row with id=1 (dashboard is a singleton view)
 * - Flat fields for simple data (fast queries)
 * - JSON strings for complex nested objects (alerts, live status, etc.)
 * - cachedAt timestamp for cache invalidation logic
 */
@Serializable
data class DashboardCacheEntity(
    val id: Int = 1,

    // ===== User Info =====
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val userRole: String,
    val email: String,

    // ===== Fleet Overview =====
    val totalVehicles: Int,
    val activeVehicles: Int,
    val maintenanceVehicles: Int,
    val inactiveVehicles: Int,
    val totalDrivers: Int,
    val activeDrivers: Int,
    val driversOnTrip: Int,
    val driversOnLeave: Int,
    val totalTrips: Int,
    val ongoingTrips: Int,
    val plannedTrips: Int,
    val completedTrips: Int,
    val totalDistance: Double,

    // ===== Today Summary =====
    val completedTripsToday: Int,
    val totalDistanceToday: Double,
    val fuelConsumption: Double,
    val activeVehiclesNow: Int,
    val newTripsToday: Int,
    val alertsCount: Int,

    // ===== Complex Objects (JSON serialized) =====
    val alertsJson: String,           // List<AlertDto>
    val totalAlerts: Int,
    val quickActionsJson: String,     // QuickActionsDto
    val liveStatusJson: String,       // LiveStatusDto
    val teamStatsJson: String?,       // TeamStatsDto (nullable - owner only)
    val documentStatsJson: String?,   // DocumentStatsDto (nullable)

    // ===== Cache Metadata =====
    val lastUpdated: String?,
    val cachedAt: Long                // Timestamp for cache validation
) : DbEntity
