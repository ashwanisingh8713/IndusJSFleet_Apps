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
    val totalFuelFilled: Double = 0.0,
    val totalFuelUsed: Double = 0.0,
    val totalFuelCost: Double = 0.0,
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
    LICENSE_EXPIRY,
    MISSING_DOCUMENTS,

    // Device-tracker alerts (frozen contract 2026-07-01, firmware CONTRACT_CHANGE):
    // backend authors these from raw device events; the app only renders them.
    ROUTE_DEVIATION,
    NIGHT_DRIVING,
    GPS_LOSS,
    SOS,
    IDLE,
    INCOMING_CALL;

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
                "MISSING_DOCUMENTS" -> MISSING_DOCUMENTS
                "ROUTE_DEVIATION" -> ROUTE_DEVIATION
                "NIGHT_DRIVING" -> NIGHT_DRIVING
                "GPS_LOSS" -> GPS_LOSS
                "SOS" -> SOS
                "IDLE" -> IDLE
                "INCOMING_CALL" -> INCOMING_CALL
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
    val isRead: Boolean = false,
    // NEW: Enhanced alert fields for vehicle/driver association
    val vehicleId: Int? = null,
    val vehicleRegistrationNumber: String? = null,
    val driverId: Int? = null,
    val driverName: String? = null,
    val daysUntilExpiry: Int? = null,
    val expiryDate: String? = null
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
 * Backend dashboard.TeamStats exposes only total_members.
 */
data class TeamStats(
    val totalMembers: Int = 0,
    val admins: Int = 0,
    val users: Int = 0
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
    val fullName: String get() = "$firstName $lastName".trim()
}

// ============ DASHBOARD V2 Domain Entities ============

/**
 * Cost breakdown item for detailed expense tracking.
 * Mirrors backend report.CostTypeBreakdown (cost_id/cost_label/group_id).
 */
data class CostBreakdownItem(
    val costId: String = "",
    val costLabel: String = "",
    val groupId: String = "",
    val amount: Double = 0.0,
    val count: Int = 0
)

/**
 * Cost Overview domain entity.
 *
 * Backend report.CostOverview reports `total_profit`/`total_loss`/
 * `net_profit_loss`; `profitLoss`/`isProfit` are derived at the data boundary
 * for the UI (it renders a single signed profit/loss figure). `filter`/
 * `periodLabel` are filled from the requested filter, not the wire payload.
 */
data class CostOverview(
    val filter: String = "today",
    val periodLabel: String = "",
    val totalExpenses: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalLoss: Double = 0.0,
    val netProfitLoss: Double = 0.0,
    // Derived for the UI from net_profit_loss.
    val profitLoss: Double = 0.0,
    val isProfit: Boolean = true,
    val completedTrips: Int = 0,
    // Expense category totals (backend *_expenses).
    val fuelExpenses: Double = 0.0,
    val tollExpenses: Double = 0.0,
    val maintenanceExpenses: Double = 0.0,
    val otherExpenses: Double = 0.0,
    val pendingPayments: Double = 0.0,
    val receivedPayments: Double = 0.0,
    // Detailed trip-cost expense breakdowns
    val driverAllowanceExpenses: Double = 0.0,
    val parkingExpenses: Double = 0.0,
    val loadingCharges: Double = 0.0,
    val unloadingCharges: Double = 0.0,
    val chalanExpenses: Double = 0.0,
    val permitExpenses: Double = 0.0,
    val insuranceExpenses: Double = 0.0,
    val tripCostBreakdown: List<CostBreakdownItem> = emptyList(),
    val maintenanceCostBreakdown: List<CostBreakdownItem> = emptyList()
)

/**
 * Alerts Summary with detailed counts by type/priority.
 */
data class AlertsSummary(
    val totalAlerts: Int = 0,
    val criticalAlerts: Int = 0,
    val warningAlerts: Int = 0,
    val infoAlerts: Int = 0,
    val documentExpiring: Int = 0,
    val licenseExpiring: Int = 0,
    val pendingPayments: Int = 0,
    val maintenanceVehicles: Int = 0,
    // NEW: Detailed expiry counts
    val documentExpired: Int = 0,
    val documentExpiring7Days: Int = 0,
    val documentExpiring30Days: Int = 0,
    val licenseExpired: Int = 0,
    val licenseExpiring7Days: Int = 0,
    val licenseExpiring30Days: Int = 0,
    val maintenanceVehiclesCount: Int = 0,
    // Alerts list
    val alerts: List<Alert> = emptyList()
)

/**
 * Pending Payment domain entity.
 * Mirrors backend report.PendingPayment.
 */
data class PendingPayment(
    val tripId: Int = 0,
    val vehicleId: Int = 0,
    val vehicleRegistration: String = "",
    val customerName: String = "",
    val customerContact: String = "",
    val totalAmount: Double = 0.0,
    val receivedAmount: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val tripDate: String? = null,
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

// ============ FINANCIAL SUMMARY ============

/**
 * Financial Summary domain entity - Simple KPIs for dashboard.
 * Available to Owner and General Manager only.
 */
data class FinancialSummary(
    val period: String = "monthly",
    val periodLabel: String = "",
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    // Backend report.FinancialSummary cost split.
    val tripCosts: Double = 0.0,
    val maintenanceCosts: Double = 0.0,
    val driverCosts: Double = 0.0,
    val netProfit: Double = 0.0,
    val profitMargin: Double = 0.0,
    val profitStatus: String = "neutral", // "profit", "loss", "break_even"
    val pendingPayments: Double = 0.0,
    val receivedPayments: Double = 0.0,
    val completedTrips: Int = 0,
    val totalTrips: Int = 0,
    val avgTripRevenue: Double = 0.0,
    val avgTripCost: Double = 0.0,
    val avgTripProfit: Double = 0.0
) {
    val isProfit: Boolean get() = profitStatus == "profit"
    val isLoss: Boolean get() = profitStatus == "loss"
    val isBreakEven: Boolean get() = profitStatus == "break_even"
}
