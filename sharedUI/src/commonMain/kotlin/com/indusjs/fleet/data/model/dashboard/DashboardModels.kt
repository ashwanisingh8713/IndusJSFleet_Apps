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
    @SerialName("last_updated")
    val lastUpdated: String? = null
) : Dto

/**
 * User information in dashboard.
 */
@Serializable
data class UserInfoDto(
    val id: Int,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val role: String,
    val email: String
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
    @SerialName("created_at")
    val createdAt: String? = null
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
    @SerialName("started_at")
    val startedAt: String? = null
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
    @SerialName("last_updated")
    val lastUpdated: String? = null,
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
    @SerialName("total_managers")
    val totalManagers: Int = 0,
    @SerialName("total_supervisors")
    val totalSupervisors: Int = 0,
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

