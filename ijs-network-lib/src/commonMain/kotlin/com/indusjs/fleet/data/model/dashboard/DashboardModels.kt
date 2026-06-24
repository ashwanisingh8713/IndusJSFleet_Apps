package com.indusjs.fleet.data.model.dashboard

import com.indusjs.fleet.data.model.Dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Dashboard API response wrapper.
 */
@Serializable
data class DashboardApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: DashboardDataDto? = null
)

/**
 * Complete dashboard data from API.
 */
@Serializable
data class DashboardDataDto(
    @SerialName("user_info")
    val userInfo: UserInfoDto,
    @SerialName("fleet_overview")
    val fleetOverview: FleetOverviewDto,
    @SerialName("today_summary")
    val todaySummary: TodaySummaryDto,
    val alerts: List<AlertDto> = emptyList(),
    @SerialName("total_alerts")
    val totalAlerts: Int = 0,
    @SerialName("quick_actions")
    val quickActions: QuickActionsDto,
    @SerialName("live_status")
    val liveStatus: LiveStatusDto,
    @SerialName("team_stats")
    val teamStats: TeamStatsDto? = null,
    @SerialName("document_stats")
    val documentStats: DocumentStatsDto? = null,
    // UTC epoch-millis (JSON number). 0/null = unset.
    @SerialName("last_updated")
    val lastUpdated: Long? = null
) : Dto

/**
 * User information in dashboard.
 */
@Serializable
data class UserInfoDto(
    val id: Int = 0,
    @SerialName("first_name")
    val firstName: String = "",
    @SerialName("last_name")
    val lastName: String = "",
    val role: String = "",
    // backend serializes email with omitempty → key absent for users with no email; default avoids MissingFieldException
    val email: String = ""
) : Dto

/**
 * Fleet overview statistics.
 */
@Serializable
data class FleetOverviewDto(
    @SerialName("total_vehicles")
    val totalVehicles: Int = 0,
    @SerialName("active_vehicles")
    val activeVehicles: Int = 0,
    @SerialName("maintenance_vehicles")
    val maintenanceVehicles: Int = 0,
    @SerialName("inactive_vehicles")
    val inactiveVehicles: Int = 0,
    @SerialName("total_drivers")
    val totalDrivers: Int = 0,
    @SerialName("active_drivers")
    val activeDrivers: Int = 0,
    @SerialName("drivers_on_trip")
    val driversOnTrip: Int = 0,
    @SerialName("drivers_on_leave")
    val driversOnLeave: Int = 0,
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("ongoing_trips")
    val ongoingTrips: Int = 0,
    @SerialName("planned_trips")
    val plannedTrips: Int = 0,
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("total_distance")
    val totalDistance: Double = 0.0
) : Dto

/**
 * Today's summary statistics.
 */
@Serializable
data class TodaySummaryDto(
    @SerialName("completed_trips_today")
    val completedTripsToday: Int = 0,
    @SerialName("total_distance_today")
    val totalDistanceToday: Double = 0.0,
    @SerialName("fuel_consumption")
    val fuelConsumption: Double = 0.0,
    // Additive fuel fields from backend TodaySummary.
    @SerialName("total_fuel_filled")
    val totalFuelFilled: Double = 0.0,
    @SerialName("total_fuel_used")
    val totalFuelUsed: Double = 0.0,
    @SerialName("total_fuel_cost")
    val totalFuelCost: Double = 0.0,
    @SerialName("active_vehicles_now")
    val activeVehiclesNow: Int = 0,
    @SerialName("new_trips_today")
    val newTripsToday: Int = 0,
    @SerialName("alerts_count")
    val alertsCount: Int = 0
) : Dto

/**
 * Alert item from API.
 */
@Serializable
data class AlertDto(
    val id: String,
    val type: String,
    val priority: String,
    val title: String,
    val message: String,
    @SerialName("entity_type")
    val entityType: String? = null,
    @SerialName("entity_id")
    val entityId: Int? = null,
    @SerialName("entity_name")
    val entityName: String? = null,
    // UTC epoch-millis (JSON number). 0/null = unset.
    @SerialName("created_at")
    val createdAt: Long? = null,
    // NEW: Enhanced alert fields for vehicle/driver association
    @SerialName("vehicle_id")
    val vehicleId: Int? = null,
    @SerialName("vehicle_registration_number")
    val vehicleRegistrationNumber: String? = null,
    @SerialName("driver_id")
    val driverId: Int? = null,
    @SerialName("driver_name")
    val driverName: String? = null,
    @SerialName("days_until_expiry")
    val daysUntilExpiry: Int? = null,
    // UTC epoch-millis (JSON number). 0/null = unset.
    @SerialName("expiry_date")
    val expiryDate: Long? = null
) : Dto

/**
 * Quick actions counts.
 */
@Serializable
data class QuickActionsDto(
    @SerialName("vehicles_count")
    val vehiclesCount: Int = 0,
    @SerialName("drivers_count")
    val driversCount: Int = 0,
    @SerialName("trips_count")
    val tripsCount: Int = 0,
    @SerialName("alerts_count")
    val alertsCount: Int = 0
) : Dto

/**
 * Live status information.
 */
@Serializable
data class LiveStatusDto(
    @SerialName("live_tracking_vehicles")
    val liveTrackingVehicles: Int = 0,
    @SerialName("ongoing_trips_count")
    val ongoingTripsCount: Int = 0,
    @SerialName("drivers_on_trip")
    val driversOnTrip: Int = 0,
    @SerialName("ongoing_trips")
    val ongoingTrips: List<OngoingTripDto> = emptyList(),
    @SerialName("live_vehicles")
    val liveVehicles: List<LiveVehicleDto> = emptyList()
) : Dto

/**
 * Ongoing trip information.
 */
@Serializable
data class OngoingTripDto(
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("vehicle_registration")
    val vehicleRegistration: String,
    @SerialName("driver_name")
    val driverName: String,
    @SerialName("start_location")
    val startLocation: String,
    @SerialName("end_location")
    val endLocation: String,
    val status: String,
    // UTC epoch-millis (JSON number). 0/null = unset.
    @SerialName("started_at")
    val startedAt: Long? = null
) : Dto

/**
 * Live vehicle tracking information.
 */
@Serializable
data class LiveVehicleDto(
    @SerialName("vehicle_id")
    val vehicleId: Int,
    @SerialName("registration_number")
    val registrationNumber: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speed: Double = 0.0,
    // UTC epoch-millis (JSON number). 0/null = unset.
    @SerialName("last_updated")
    val lastUpdated: Long? = null,
    @SerialName("driver_name")
    val driverName: String? = null,
    @SerialName("trip_id")
    val tripId: Int? = null
) : Dto

/**
 * Team statistics (Owner only).
 */
@Serializable
data class TeamStatsDto(
    // Backend dashboard.TeamStats exposes only total_members.
    @SerialName("total_members")
    val totalMembers: Int = 0
) : Dto

/**
 * Document statistics (Owner/Manager).
 */
@Serializable
data class DocumentStatsDto(
    @SerialName("total_documents")
    val totalDocuments: Int = 0,
    @SerialName("expiring_documents")
    val expiringDocuments: Int = 0,
    @SerialName("expired_documents")
    val expiredDocuments: Int = 0
) : Dto

// ============ DASHBOARD V2 APIs ============

/**
 * Cost Overview filter types.
 */
enum class CostOverviewFilter(val value: String, val label: String) {
    TODAY("today", "Today"),
    WEEKLY("weekly", "Weekly"),
    MONTHLY("monthly", "Monthly")
}

/**
 * Cost Overview API response.
 */
@Serializable
data class CostOverviewApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: CostOverviewDto? = null
)

/**
 * Cost Overview data from API.
 */
@Serializable
data class CostOverviewDto(
    // Backend report.CostOverview: "today" | "weekly" | "monthly".
    @SerialName("period")
    val period: String = "today",
    @SerialName("total_expenses")
    val totalExpenses: Double = 0.0,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_profit")
    val totalProfit: Double = 0.0,
    @SerialName("total_loss")
    val totalLoss: Double = 0.0,
    @SerialName("net_profit_loss")
    val netProfitLoss: Double = 0.0,
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("fuel_expenses")
    val fuelExpenses: Double = 0.0,
    @SerialName("toll_expenses")
    val tollExpenses: Double = 0.0,
    @SerialName("maintenance_expenses")
    val maintenanceExpenses: Double = 0.0,
    @SerialName("other_expenses")
    val otherExpenses: Double = 0.0,
    @SerialName("pending_payments")
    val pendingPayments: Double = 0.0,
    @SerialName("received_payments")
    val receivedPayments: Double = 0.0,
    // Detailed trip-cost expense breakdowns
    @SerialName("driver_allowance_expenses")
    val driverAllowanceExpenses: Double = 0.0,
    @SerialName("parking_expenses")
    val parkingExpenses: Double = 0.0,
    @SerialName("loading_charges")
    val loadingCharges: Double = 0.0,
    @SerialName("unloading_charges")
    val unloadingCharges: Double = 0.0,
    @SerialName("chalan_expenses")
    val chalanExpenses: Double = 0.0,
    @SerialName("permit_expenses")
    val permitExpenses: Double = 0.0,
    @SerialName("insurance_expenses")
    val insuranceExpenses: Double = 0.0,
    // omitempty on backend -> default to empty list
    @SerialName("trip_cost_breakdown")
    val tripCostBreakdown: List<CostBreakdownItemDto> = emptyList(),
    @SerialName("maintenance_cost_breakdown")
    val maintenanceCostBreakdown: List<CostBreakdownItemDto> = emptyList()
) : Dto

/**
 * Cost breakdown item for detailed expense tracking.
 * Mirrors backend report.CostTypeBreakdown.
 */
@Serializable
data class CostBreakdownItemDto(
    @SerialName("cost_id")
    val costId: String = "",
    @SerialName("cost_label")
    val costLabel: String = "",
    // omitempty on backend
    @SerialName("group_id")
    val groupId: String = "",
    val amount: Double = 0.0,
    val count: Int = 0
) : Dto

/**
 * Pending Payments API response.
 */
@Serializable
data class PendingPaymentsApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: PendingPaymentsDataDto? = null
)

/**
 * Pending Payments data wrapper.
 */
@Serializable
data class PendingPaymentsDataDto(
    val payments: List<PendingPaymentDto> = emptyList(),
    @SerialName("total_pending")
    val totalPending: Double = 0.0,
    // Backend application/report.PendingPaymentsResult uses "count".
    @SerialName("count")
    val totalCount: Int = 0,
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1
) : Dto

/**
 * Individual pending payment item.
 */
@Serializable
data class PendingPaymentDto(
    @SerialName("trip_id")
    val tripId: Int = 0,
    @SerialName("vehicle_id")
    val vehicleId: Int = 0,
    @SerialName("vehicle_registration")
    val vehicleRegistration: String = "",
    @SerialName("customer_name")
    val customerName: String = "",
    @SerialName("customer_contact")
    val customerContact: String = "",
    // Backend report.PendingPayment amount fields.
    @SerialName("total_amount")
    val totalAmount: Double = 0.0,
    @SerialName("received_amount")
    val receivedAmount: Double = 0.0,
    @SerialName("pending_amount")
    val pendingAmount: Double = 0.0,
    // UTC epoch-millis (JSON number). 0/null = unset.
    @SerialName("trip_date")
    val tripDate: Long? = null,
    @SerialName("days_overdue")
    val daysOverdue: Int = 0
) : Dto

/**
 * Vehicle Status summary for dashboard.
 */
@Serializable
data class VehicleStatusSummaryDto(
    @SerialName("on_trip_planned")
    val onTripPlanned: Int = 0,
    @SerialName("on_trip_in_progress")
    val onTripInProgress: Int = 0,
    @SerialName("under_maintenance")
    val underMaintenance: Int = 0,
    val available: Int = 0,
    val inactive: Int = 0,
    val total: Int = 0
) : Dto

/**
 * Driver Status summary for dashboard.
 */
@Serializable
data class DriverStatusSummaryDto(
    @SerialName("on_trip_planned")
    val onTripPlanned: Int = 0,
    @SerialName("on_trip_in_progress")
    val onTripInProgress: Int = 0,
    val available: Int = 0,
    @SerialName("on_leave")
    val onLeave: Int = 0,
    val total: Int = 0
) : Dto

/**
 * Trip summary for dashboard sections.
 */
@Serializable
data class TripSummaryDto(
    @SerialName("in_progress")
    val inProgress: Int = 0,
    val planned: Int = 0,
    val delayed: Int = 0,
    val completed: Int = 0,
    val total: Int = 0
) : Dto

/**
 * Trip with fuel details for dashboard.
 */
@Serializable
data class TripWithFuelDto(
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("vehicle_registration")
    val vehicleRegistration: String = "",
    @SerialName("driver_name")
    val driverName: String = "",
    @SerialName("start_location")
    val startLocation: String = "",
    @SerialName("end_location")
    val endLocation: String = "",
    val status: String = "",
    val state: String = "",
    // UTC epoch-millis (JSON number). 0/null = unset.
    @SerialName("scheduled_date")
    val scheduledDate: Long? = null,
    @SerialName("fuel_type")
    val fuelType: String = "",
    @SerialName("filled_fuel_quantity")
    val filledFuelQuantity: Double = 0.0,
    @SerialName("used_fuel_quantity")
    val usedFuelQuantity: Double = 0.0,
    @SerialName("fuel_rate")
    val fuelRate: Double = 0.0,
    @SerialName("fuel_cost")
    val fuelCost: Double = 0.0,
    @SerialName("km_per_liter")
    val kmPerLiter: Double = 0.0,
    @SerialName("estimated_distance")
    val estimatedDistance: Double = 0.0,
    @SerialName("actual_distance")
    val actualDistance: Double = 0.0,
    @SerialName("is_delayed")
    val isDelayed: Boolean = false,
    @SerialName("delay_reason")
    val delayReason: String? = null
) : Dto

/**
 * Expiry alert for documents (Insurance, PUC, Fitness).
 */
@Serializable
data class ExpiryAlertDto(
    val id: String,
    @SerialName("alert_type")
    val alertType: String = "", // insurance, puc, fitness
    @SerialName("vehicle_id")
    val vehicleId: Int = 0,
    @SerialName("vehicle_registration")
    val vehicleRegistration: String = "",
    @SerialName("document_type")
    val documentType: String = "",
    // UTC epoch-millis (JSON number). 0/null = unset.
    @SerialName("expiry_date")
    val expiryDate: Long? = null,
    @SerialName("days_remaining")
    val daysRemaining: Int = 0,
    @SerialName("is_expired")
    val isExpired: Boolean = false,
    val priority: String = "warning" // critical, warning, info
) : Dto

/**
 * Alerts Status API response.
 */
@Serializable
data class AlertsStatusApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: AlertsStatusDto? = null
)

/**
 * Alerts Status data from API with detailed counts.
 */
@Serializable
data class AlertsStatusDto(
    @SerialName("total_alerts")
    val totalAlerts: Int = 0,
    @SerialName("critical_alerts")
    val criticalAlerts: Int = 0,
    @SerialName("warning_alerts")
    val warningAlerts: Int = 0,
    @SerialName("info_alerts")
    val infoAlerts: Int = 0,
    @SerialName("document_expiring")
    val documentExpiring: Int = 0,
    @SerialName("license_expiring")
    val licenseExpiring: Int = 0,
    @SerialName("pending_payments")
    val pendingPayments: Int = 0,
    @SerialName("maintenance_vehicles")
    val maintenanceVehicles: Int = 0,
    // NEW: Detailed expiry counts
    @SerialName("document_expired")
    val documentExpired: Int = 0,
    @SerialName("document_expiring_7_days")
    val documentExpiring7Days: Int = 0,
    @SerialName("document_expiring_30_days")
    val documentExpiring30Days: Int = 0,
    @SerialName("license_expired")
    val licenseExpired: Int = 0,
    @SerialName("license_expiring_7_days")
    val licenseExpiring7Days: Int = 0,
    @SerialName("license_expiring_30_days")
    val licenseExpiring30Days: Int = 0,
    @SerialName("maintenance_vehicles_count")
    val maintenanceVehiclesCount: Int = 0,
    // Alerts list
    val alerts: List<AlertDto> = emptyList()
) : Dto

// ============ FINANCIAL SUMMARY API ============

/**
 * Financial Summary API response.
 * GET /dashboard/financial-summary?period=monthly
 */
@Serializable
data class FinancialSummaryApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: FinancialSummaryDto? = null
)

/**
 * Financial Summary DTO - Simple KPIs for dashboard.
 * Available to Owner and General Manager only.
 */
@Serializable
data class FinancialSummaryDto(
    @SerialName("period")
    val period: String = "monthly",
    // UTC epoch-millis (JSON number). 0 = unset.
    @SerialName("start_date")
    val startDate: Long = 0L,
    @SerialName("end_date")
    val endDate: Long = 0L,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_expenses")
    val totalExpenses: Double = 0.0,
    // Backend report.FinancialSummary cost split.
    @SerialName("trip_costs")
    val tripCosts: Double = 0.0,
    @SerialName("maintenance_costs")
    val maintenanceCosts: Double = 0.0,
    @SerialName("driver_costs")
    val driverCosts: Double = 0.0,
    @SerialName("net_profit")
    val netProfit: Double = 0.0,
    @SerialName("profit_margin")
    val profitMargin: Double = 0.0,
    @SerialName("profit_status")
    val profitStatus: String = "neutral", // "profit", "loss", "break_even"
    @SerialName("pending_payments")
    val pendingPayments: Double = 0.0,
    @SerialName("received_payments")
    val receivedPayments: Double = 0.0,
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("avg_trip_revenue")
    val avgTripRevenue: Double = 0.0,
    @SerialName("avg_trip_cost")
    val avgTripCost: Double = 0.0,
    @SerialName("avg_trip_profit")
    val avgTripProfit: Double = 0.0
) : Dto

/**
 * Financial summary period options.
 */
enum class FinancialPeriod(val value: String, val label: String) {
    TODAY("today", "Today"),
    WEEKLY("weekly", "This Week"),
    MONTHLY("monthly", "This Month"),
    YEARLY("yearly", "This Year")
}
