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

// ============ DASHBOARD V2 Domain Entities ============

/**
 * Cost Overview domain entity.
 */
data class CostOverview(
    val filter: String = "today",
    val periodLabel: String = "",
    val totalExpenses: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val profitLoss: Double = 0.0,
    val isProfit: Boolean = true,
    val completedTrips: Int = 0,
    val tripCosts: Double = 0.0,
    val maintenanceCosts: Double = 0.0,
    val fuelCosts: Double = 0.0,
    val tollCosts: Double = 0.0,
    val otherCosts: Double = 0.0
)

/**
 * Pending Payment domain entity.
 */
data class PendingPayment(
    val tripId: Int,
    val vehicleRegistration: String = "",
    val customerName: String = "",
    val customerContact: String? = null,
    val sellingValue: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val paymentStatus: String = "pending",
    val tripDate: String? = null,
    val startLocation: String = "",
    val endLocation: String = "",
    val daysOverdue: Int = 0
)

/**
 * Pending Payments data wrapper.
 */
data class PendingPaymentsData(
    val payments: List<PendingPayment> = emptyList(),
    val totalPending: Double = 0.0,
    val totalCount: Int = 0
)

/**
 * Vehicle Status summary.
 */
data class VehicleStatusSummary(
    val onTripPlanned: Int = 0,
    val onTripInProgress: Int = 0,
    val underMaintenance: Int = 0,
    val available: Int = 0,
    val inactive: Int = 0,
    val total: Int = 0
)

/**
 * Driver Status summary.
 */
data class DriverStatusSummary(
    val onTripPlanned: Int = 0,
    val onTripInProgress: Int = 0,
    val available: Int = 0,
    val onLeave: Int = 0,
    val total: Int = 0
)

/**
 * Trip summary for dashboard.
 */
data class TripSummary(
    val inProgress: Int = 0,
    val planned: Int = 0,
    val delayed: Int = 0,
    val completed: Int = 0,
    val total: Int = 0
)

/**
 * Trip with fuel details.
 */
data class TripWithFuel(
    val tripId: Int,
    val vehicleRegistration: String = "",
    val driverName: String = "",
    val startLocation: String = "",
    val endLocation: String = "",
    val status: String = "",
    val state: String = "",
    val scheduledDate: String? = null,
    val fuelType: String = "",
    val filledFuelQuantity: Double = 0.0,
    val usedFuelQuantity: Double = 0.0,
    val fuelRate: Double = 0.0,
    val fuelCost: Double = 0.0,
    val kmPerLiter: Double = 0.0,
    val estimatedDistance: Double = 0.0,
    val actualDistance: Double = 0.0,
    val isDelayed: Boolean = false,
    val delayReason: String? = null
)

/**
 * Expiry alert for documents.
 */
data class ExpiryAlert(
    val id: String,
    val alertType: String = "",
    val vehicleId: Int = 0,
    val vehicleRegistration: String = "",
    val documentType: String = "",
    val expiryDate: String = "",
    val daysRemaining: Int = 0,
    val isExpired: Boolean = false,
    val priority: String = "warning"
)

