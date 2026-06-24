package com.ijs.vehicle.domain.entity
/**
 * Complete vehicle detail data for all tabs.
 * Mapped from GET /vehicles/{id}/detail API response.
 */
data class VehicleDetail(
    val vehicle: Vehicle,
    val assignedDriver: AssignedDriver?,
    val currentLocation: Location?,
    val stats: VehicleStats,
    val documents: DocumentsSummary,
    val trips: TripsSummary,
    val route: RouteInfo?
)
/**
 * Vehicle statistics.
 */
data class VehicleStats(
    val totalTrips: Int = 0,
    val completedTrips: Int = 0,
    val totalDistance: Double = 0.0,
    val tripsThisMonth: Int = 0,
    val distanceThisMonth: Double = 0.0
)
/**
 * Documents summary for overview.
 */
data class DocumentsSummary(
    val total: Int = 0,
    val uploaded: Int = 0,
    val notUploaded: Int = 0,
    val active: Int = 0,
    val expiringSoon: Int = 0,
    val expired: Int = 0,
    val pending: Int = 0,
    val alerts: List<DocumentAlert> = emptyList()
)
/**
 * Document alert item.
 */
data class DocumentAlert(
    val type: String,
    val typeName: String,
    val alertType: String, // "expired", "expiring_soon", "not_uploaded"
    val message: String,
    val daysRemaining: Int? = null
)
/**
 * Trips summary for overview.
 */
data class TripsSummary(
    val total: Int = 0,
    val planned: Int = 0,
    val inProgress: Int = 0,
    val completed: Int = 0,
    val cancelled: Int = 0,
    val recentTrips: List<VehicleTripItem> = emptyList()
)
/**
 * Trip item for vehicle trips tab.
 */
data class VehicleTripItem(
    val id: String,
    val tripNumber: String?,
    val origin: String,
    val destination: String,
    val state: String,
    val stateLabel: String,
    val scheduledDate: Long?,
    val startTime: Long?,
    val endTime: Long?,
    val driverName: String?,
    val distance: Double?,
    val duration: String?
)
/**
 * Route info for route & stops tab.
 */
data class RouteInfo(
    val hasActiveTrip: Boolean = false,
    val tripId: String? = null,
    val tripNumber: String? = null,
    val driverName: String? = null,
    val origin: String? = null,
    val destination: String? = null,
    val currentPosition: Location? = null,
    val progress: RouteProgress? = null,
    val stops: List<RouteStop> = emptyList()
)
/**
 * Route progress information.
 */
data class RouteProgress(
    val percentage: Int = 0,
    val distanceCovered: Double = 0.0,
    val distanceRemaining: Double = 0.0,
    val timeElapsed: String? = null,
    val eta: String? = null
)
/**
 * Stop in a route.
 */
data class RouteStop(
    val id: String,
    val sequence: Int,
    val type: String, // "pickup", "delivery", "checkpoint", "rest"
    val location: String,
    val address: String?,
    val status: String, // "pending", "current", "completed"
    val scheduledTime: Long?,
    val actualTime: Long?,
    val notes: String?
)
/**
 * Document type info for documents tab.
 */
data class DocumentTypeDetail(
    val type: String,
    val tag: String,
    val typeName: String,
    val description: String?,
    val isRequired: Boolean,
    val isUploaded: Boolean,
    val document: VehicleDocumentInfo?
)
/**
 * Detailed document info.
 */
data class VehicleDocumentInfo(
    val id: String,
    val name: String,
    val documentNumber: String?,
    val expiryDate: Long?,
    val status: String,
    val statusLabel: String,
    val daysRemaining: Int?,
    val fileUrl: String?,
    val uploadedAt: Long?
)
/**
 * Documents tab data.
 */
data class VehicleDocumentsData(
    val summary: DocumentsSummary,
    val documentTypes: List<DocumentTypeDetail>,
    val alertDocs: List<DocumentAlert>,
    val otherDocuments: List<VehicleDocumentInfo>
)
/**
 * Trips tab data with pagination.
 */
data class VehicleTripsData(
    val summary: TripsSummary,
    val trips: List<VehicleTripItem>,
    val page: Int,
    val perPage: Int,
    val totalPages: Int,
    val hasMore: Boolean
)
