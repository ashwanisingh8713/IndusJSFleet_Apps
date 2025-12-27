package com.indusjs.fleet.domain.entity.dashboard

/**
 * Dashboard statistics entity - comprehensive data from unified dashboard API.
 */
data class DashboardStats(
    // Fleet Overview
    val totalVehicles: Int = 0,
    val activeVehicles: Int = 0,
    val maintenanceVehicles: Int = 0,
    val inactiveVehicles: Int = 0,
    val totalDrivers: Int = 0,
    val activeDrivers: Int = 0,
    val driversOnTrip: Int = 0,
    val driversOnLeave: Int = 0,
    val totalTrips: Int = 0,
    val ongoingTrips: Int = 0,
    val plannedTrips: Int = 0,
    val completedTrips: Int = 0,
    val totalDistance: Double = 0.0,

    // Today's Summary
    val completedTripsToday: Int = 0,
    val totalDistanceToday: Double = 0.0,
    val fuelConsumption: Double = 0.0,
    val activeVehiclesNow: Int = 0,
    val newTripsToday: Int = 0,
    val alertsCount: Int = 0,

    // Alerts
    val alerts: List<Alert> = emptyList(),
    val totalAlerts: Int = 0,

    // Quick Actions
    val quickActions: QuickActions = QuickActions(),

    // Live Status
    val liveStatus: LiveStatus = LiveStatus(),

    // Team Stats (Owner only)
    val teamStats: TeamStats? = null,

    // Document Stats (Owner/Manager)
    val documentStats: DocumentStats? = null,

    // Metadata
    val lastUpdated: String? = null
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
    SYSTEM,
    DOCUMENT_EXPIRY,
    LICENSE_EXPIRY;

    companion object {
        fun fromString(value: String): AlertType {
            return when (value.uppercase()) {
                "MAINTENANCE" -> MAINTENANCE
                "FUEL_LOW" -> FUEL_LOW
                "SPEED_VIOLATION" -> SPEED_VIOLATION
                "GEOFENCE_VIOLATION" -> GEOFENCE_VIOLATION
                "DRIVER_BEHAVIOR" -> DRIVER_BEHAVIOR
                "SYSTEM" -> SYSTEM
                "DOCUMENT_EXPIRY" -> DOCUMENT_EXPIRY
                "LICENSE_EXPIRY" -> LICENSE_EXPIRY
                else -> SYSTEM
            }
        }
    }
}

/**
 * Alert priority levels.
 */
enum class AlertPriority {
    CRITICAL,
    WARNING,
    INFO;

    companion object {
        fun fromString(value: String): AlertPriority {
            return when (value.lowercase()) {
                "critical" -> CRITICAL
                "warning" -> WARNING
                "info" -> INFO
                else -> INFO
            }
        }
    }
}

/**
 * Alert entity for dashboard notifications.
 */
data class Alert(
    val id: String,
    val type: AlertType,
    val priority: AlertPriority = AlertPriority.INFO,
    val title: String,
    val message: String,
    val entityType: String? = null,
    val entityId: Int? = null,
    val entityName: String? = null,
    val createdAt: String? = null,
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)

/**
 * Quick actions counts.
 */
data class QuickActions(
    val vehiclesCount: Int = 0,
    val driversCount: Int = 0,
    val tripsCount: Int = 0,
    val alertsCount: Int = 0
)

/**
 * Live status information.
 */
data class LiveStatus(
    val liveTrackingVehicles: Int = 0,
    val ongoingTripsCount: Int = 0,
    val driversOnTrip: Int = 0,
    val ongoingTrips: List<OngoingTrip> = emptyList(),
    val liveVehicles: List<LiveVehicle> = emptyList()
)

/**
 * Ongoing trip information.
 */
data class OngoingTrip(
    val tripId: Int,
    val vehicleRegistration: String,
    val driverName: String,
    val startLocation: String,
    val endLocation: String,
    val status: String,
    val startedAt: String? = null
)

/**
 * Live vehicle tracking information.
 */
data class LiveVehicle(
    val vehicleId: Int,
    val registrationNumber: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speed: Double = 0.0,
    val lastUpdated: String? = null,
    val driverName: String? = null,
    val tripId: Int? = null
)

/**
 * Team statistics (Owner only).
 */
data class TeamStats(
    val totalManagers: Int = 0,
    val totalSupervisors: Int = 0,
    val totalMembers: Int = 0
)

/**
 * Document statistics (Owner/Manager).
 */
data class DocumentStats(
    val totalDocuments: Int = 0,
    val expiringDocuments: Int = 0,
    val expiredDocuments: Int = 0
)

/**
 * User info from dashboard.
 */
data class DashboardUserInfo(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val role: String,
    val email: String
) {
    val fullName: String get() = "$firstName $lastName"
}

