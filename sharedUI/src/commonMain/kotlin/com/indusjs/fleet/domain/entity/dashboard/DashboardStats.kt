package com.indusjs.fleet.domain.entity.dashboard

/**
 * Dashboard statistics entity.
 */
data class DashboardStats(
    val totalVehicles: Int = 0,
    val activeVehicles: Int = 0,
    val totalDrivers: Int = 0,
    val activeDrivers: Int = 0,
    val ongoingTrips: Int = 0,
    val completedTripsToday: Int = 0,
    val totalDistance: Double = 0.0,
    val fuelConsumption: Double = 0.0,
    val alerts: List<Alert> = emptyList()
)

/**
 * Alert types for the dashboard.
 */
enum class AlertType {
    MAINTENANCE,
    FUEL_LOW,
    SPEED_VIOLATION,
    GEOFENCE_VIOLATION,
    DRIVER_BEHAVIOR,
    SYSTEM
}

/**
 * Alert entity for dashboard notifications.
 */
data class Alert(
    val id: String,
    val type: AlertType,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

