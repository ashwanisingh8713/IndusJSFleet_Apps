package com.indusjs.fleet.data.mapper.vehicle

import com.indusjs.fleet.domain.entity.vehicle.AssignedDriver
import com.indusjs.fleet.domain.entity.vehicle.DocumentAlert
import com.indusjs.fleet.domain.entity.vehicle.DocumentsSummary
import com.indusjs.fleet.domain.entity.vehicle.DocumentTypeDetail
import com.indusjs.fleet.domain.entity.vehicle.Location
import com.indusjs.fleet.domain.entity.vehicle.RouteInfo
import com.indusjs.fleet.domain.entity.vehicle.RouteProgress
import com.indusjs.fleet.domain.entity.vehicle.RouteStop
import com.indusjs.fleet.domain.entity.vehicle.TripsSummary
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.entity.vehicle.VehicleDetail
import com.indusjs.fleet.domain.entity.vehicle.VehicleDocumentInfo
import com.indusjs.fleet.domain.entity.vehicle.VehicleDocumentsData
import com.indusjs.fleet.domain.entity.vehicle.VehicleStats
import com.indusjs.fleet.domain.entity.vehicle.VehicleStatus
import com.indusjs.fleet.domain.entity.vehicle.VehicleTripAssignment
import com.indusjs.fleet.domain.entity.vehicle.VehicleTripItem
import com.indusjs.fleet.domain.entity.vehicle.VehicleTripsData
import com.indusjs.fleet.domain.entity.vehicle.VehicleType
import com.indusjs.fleet.data.model.vehicle.AssignedDriverDto
import com.indusjs.fleet.data.model.vehicle.CreateVehicleRequest
import com.indusjs.fleet.data.model.vehicle.UpdateVehicleRequest
import com.indusjs.fleet.data.model.vehicle.DocumentAlertDto
import com.indusjs.fleet.data.model.vehicle.DocumentsSummaryDto
import com.indusjs.fleet.data.model.vehicle.DocumentTypeDetailDto
import com.indusjs.fleet.data.model.vehicle.LocationDto
import com.indusjs.fleet.data.model.vehicle.RouteProgressDto
import com.indusjs.fleet.data.model.vehicle.RouteStopDto
import com.indusjs.fleet.data.model.vehicle.TripsSummaryDto
import com.indusjs.fleet.data.model.vehicle.VehicleDetailDto
import com.indusjs.fleet.data.model.vehicle.VehicleDocumentDto
import com.indusjs.fleet.data.model.vehicle.VehicleDocumentInfoDto
import com.indusjs.fleet.data.model.vehicle.VehicleDocumentsDetailDto
import com.indusjs.fleet.data.model.vehicle.VehicleDto
import com.indusjs.fleet.data.model.vehicle.VehicleRouteDto
import com.indusjs.fleet.data.model.vehicle.VehicleStatsDto
import com.indusjs.fleet.data.model.vehicle.VehicleTripAssignmentDto
import com.indusjs.fleet.data.model.vehicle.VehicleTripDto
import com.indusjs.fleet.data.model.vehicle.VehicleTripItemDto
import com.indusjs.fleet.data.model.vehicle.VehicleTripsDto
import dev.zacsweers.metro.Inject

/**
 * Mapper for converting between Vehicle DTOs and domain entities.
 */
@Inject
class VehicleMapper {

    /**
     * Maps VehicleDto to Vehicle domain entity.
     */
    fun mapToDomain(dto: VehicleDto): Vehicle {
        // Map assigned driver from the assigned_driver object
        val assignedDriver = dto.assignedDriver?.let { mapAssignedDriverToDomain(it) }

        // Get driver name: prefer from assigned_driver object, fallback to assignedDriverName field
        val driverName = assignedDriver?.fullName()
            ?: dto.assignedDriverName

        return Vehicle(
            id = dto.id.toString(),
            registrationNumber = dto.registrationNumber,
            make = dto.make,
            model = dto.model,
            year = dto.year,
            type = parseVehicleType(dto.type),
            status = parseVehicleStatus(dto.status),
            fuelType = dto.fuelType ?: "petrol",
            color = dto.color ?: "white",
            capacity = dto.capacity ?: 4,
            fuelLevel = dto.fuelLevel,
            mileage = dto.mileage,
            lastLocation = dto.lastLocation?.let { mapLocationToDomain(it) },
            assignedDriverId = dto.assignedDriver?.id?.toString() ?: dto.assignedDriverId?.toString(),
            assignedDriverName = driverName,
            assignedDriver = assignedDriver,
            lastServiceDate = dto.lastServiceDate?.let { parseTimestamp(it) },
            nextServiceDate = dto.nextServiceDate?.let { parseTimestamp(it) },
            isOccupied = dto.isOccupied,
            tripAssignment = dto.tripAssignment?.let { mapTripAssignmentToDomain(it) }
        )
    }

    /**
     * Maps AssignedDriverDto to AssignedDriver domain entity.
     */
    private fun mapAssignedDriverToDomain(dto: AssignedDriverDto): AssignedDriver = AssignedDriver(
        id = dto.id.toString(),
        firstName = dto.firstName,
        lastName = dto.lastName,
        mobile = dto.mobile,
        licenseNumber = dto.licenseNumber
    )

    /**
     * Maps TripAssignmentDto to VehicleTripAssignment domain entity.
     */
    private fun mapTripAssignmentToDomain(dto: VehicleTripAssignmentDto): VehicleTripAssignment = VehicleTripAssignment(
        tripId = dto.tripId.toString(),
        tripState = dto.tripState,
        scheduledDate = dto.scheduledDate,
        startTime = dto.startTime,
        plannedStart = dto.plannedStart,
        plannedEnd = dto.plannedEnd,
        startLocation = dto.startLocation,
        endLocation = dto.endLocation,
        customerName = dto.customerName
    )

    /**
     * Maps a list of VehicleDto to a list of Vehicle domain entities.
     */
    fun mapToDomainList(dtos: List<VehicleDto>): List<Vehicle> = dtos.map { mapToDomain(it) }

    /**
     * Maps Vehicle domain entity to VehicleDto.
     */
    fun mapToData(vehicle: Vehicle): VehicleDto = VehicleDto(
        id = vehicle.id.toIntOrNull() ?: 0,
        registrationNumber = vehicle.registrationNumber,
        make = vehicle.make,
        model = vehicle.model,
        year = vehicle.year,
        type = vehicleTypeToApiString(vehicle.type),
        status = vehicleStatusToApiString(vehicle.status),
        fuelLevel = vehicle.fuelLevel,
        mileage = vehicle.mileage,
        lastLocation = vehicle.lastLocation?.let { mapLocationToData(it) },
        assignedDriverId = vehicle.assignedDriverId?.toIntOrNull(),
        assignedDriverName = vehicle.assignedDriverName,
        lastServiceDate = vehicle.lastServiceDate?.toString(),
        nextServiceDate = vehicle.nextServiceDate?.toString()
    )

    /**
     * Maps Vehicle domain entity to CreateVehicleRequest.
     */
    fun mapToCreateRequest(vehicle: Vehicle): CreateVehicleRequest = CreateVehicleRequest(
        registrationNumber = vehicle.registrationNumber,
        make = vehicle.make,
        model = vehicle.model,
        year = vehicle.year,
        type = vehicleTypeToApiString(vehicle.type),
        fuelType = vehicle.fuelType.lowercase(),
        capacity = vehicle.capacity,
        color = vehicle.color.lowercase(),
        assignedDriverId = vehicle.assignedDriverId?.toIntOrNull()
    )

    /**
     * Maps Vehicle domain entity to UpdateVehicleRequest for partial updates.
     */
    fun mapToUpdateRequest(vehicle: Vehicle): UpdateVehicleRequest = UpdateVehicleRequest(
        make = vehicle.make.takeIf { it.isNotBlank() },
        model = vehicle.model.takeIf { it.isNotBlank() },
        year = vehicle.year.takeIf { it > 0 },
        type = vehicleTypeToApiString(vehicle.type),
        fuelType = vehicle.fuelType.lowercase().takeIf { it.isNotBlank() },
        capacity = vehicle.capacity.takeIf { it > 0 },
        color = vehicle.color.lowercase().takeIf { it.isNotBlank() },
        mileage = vehicle.mileage.takeIf { it > 0 },
        assignedDriverId = vehicle.assignedDriverId?.toIntOrNull()
    )

    private fun mapLocationToDomain(dto: LocationDto): Location = Location(
        latitude = dto.latitude,
        longitude = dto.longitude,
        address = dto.address
    )

    private fun mapLocationToData(location: Location): LocationDto = LocationDto(
        latitude = location.latitude,
        longitude = location.longitude,
        address = location.address
    )

    /**
     * Parse API vehicle type string to VehicleType enum.
     * API uses lowercase: truck, van, car, bus, motorcycle, trailer, suv
     */
    private fun parseVehicleType(type: String): VehicleType = when (type.lowercase()) {
        "truck" -> VehicleType.TRUCK
        "van" -> VehicleType.VAN
        "car" -> VehicleType.CAR
        "bus" -> VehicleType.BUS
        "motorcycle" -> VehicleType.MOTORCYCLE
        "trailer" -> VehicleType.TRAILER
        "suv" -> VehicleType.CAR // Map SUV to CAR
        else -> VehicleType.TRUCK
    }

    /**
     * Convert VehicleType enum to API string (lowercase).
     */
    fun vehicleTypeToApiString(type: VehicleType): String = type.name.lowercase()

    /**
     * Parse API vehicle status/state string to VehicleStatus enum.
     * API uses: active, inactive, maintenance, retired
     */
    private fun parseVehicleStatus(status: String): VehicleStatus = when (status.lowercase()) {
        "active" -> VehicleStatus.ACTIVE
        "inactive" -> VehicleStatus.INACTIVE
        "maintenance" -> VehicleStatus.IN_MAINTENANCE
        "retired" -> VehicleStatus.OUT_OF_SERVICE
        "in_maintenance" -> VehicleStatus.IN_MAINTENANCE
        "out_of_service" -> VehicleStatus.OUT_OF_SERVICE
        else -> VehicleStatus.ACTIVE
    }

    /**
     * Convert VehicleStatus enum to API string.
     */
    private fun vehicleStatusToApiString(status: VehicleStatus): String = when (status) {
        VehicleStatus.ACTIVE -> "active"
        VehicleStatus.INACTIVE -> "inactive"
        VehicleStatus.IN_MAINTENANCE -> "maintenance"
        VehicleStatus.OUT_OF_SERVICE -> "retired"
    }

    /**
     * Parse timestamp string to Long.
     * Handles ISO8601 format or epoch milliseconds.
     */
    private fun parseTimestamp(timestamp: String): Long? {
        return try {
            timestamp.toLongOrNull()
        } catch (e: Exception) {
            null
        }
    }

    // ==================== Vehicle Detail Mapping Functions ====================

    /**
     * Maps VehicleDetailDto to VehicleDetail domain entity.
     */
    fun mapToVehicleDetail(dto: VehicleDetailDto): VehicleDetail {
        val vehicle = dto.vehicle?.let { mapToDomain(it) }
            ?: throw IllegalArgumentException("Vehicle data is required")

        return VehicleDetail(
            vehicle = vehicle,
            assignedDriver = dto.assignedDriver?.let { mapAssignedDriverToDomain(it) },
            currentLocation = dto.currentLocation?.let { mapLocationToDomain(it) },
            stats = dto.stats?.let { mapVehicleStatsToDomain(it) } ?: VehicleStats(),
            documents = dto.documents?.let { mapDocumentsSummaryToDomain(it) } ?: DocumentsSummary(),
            trips = dto.trips?.let { mapTripsSummaryToDomain(it) } ?: TripsSummary(),
            route = dto.route?.let { mapToRouteInfo(it) }
        )
    }

    /**
     * Maps VehicleStatsDto to VehicleStats domain entity.
     */
    private fun mapVehicleStatsToDomain(dto: VehicleStatsDto): VehicleStats = VehicleStats(
        totalTrips = dto.totalTrips,
        completedTrips = dto.completedTrips,
        totalDistance = dto.totalDistance,
        tripsThisMonth = dto.tripsThisMonth,
        distanceThisMonth = dto.distanceThisMonth
    )

    /**
     * Maps DocumentsSummaryDto to DocumentsSummary domain entity.
     */
    private fun mapDocumentsSummaryToDomain(dto: DocumentsSummaryDto): DocumentsSummary = DocumentsSummary(
        total = dto.total,
        uploaded = dto.uploaded,
        notUploaded = dto.notUploaded,
        active = dto.active,
        expiringSoon = dto.expiringSoon,
        expired = dto.expired,
        pending = dto.pending,
        alerts = dto.alerts.map { mapDocumentAlertToDomain(it) }
    )

    /**
     * Maps DocumentAlertDto to DocumentAlert domain entity.
     */
    private fun mapDocumentAlertToDomain(dto: DocumentAlertDto): DocumentAlert = DocumentAlert(
        type = dto.type,
        typeName = dto.typeName,
        alertType = dto.alertType,
        message = dto.message,
        daysRemaining = dto.daysRemaining
    )

    /**
     * Maps TripsSummaryDto to TripsSummary domain entity.
     */
    private fun mapTripsSummaryToDomain(dto: TripsSummaryDto): TripsSummary = TripsSummary(
        total = dto.total,
        planned = dto.planned,
        inProgress = dto.inProgress,
        completed = dto.completed,
        cancelled = dto.cancelled,
        recentTrips = dto.recentTrips.map { mapVehicleTripItemToDomain(it) }
    )

    /**
     * Maps VehicleTripItemDto to VehicleTripItem domain entity.
     */
    private fun mapVehicleTripItemToDomain(dto: VehicleTripItemDto): VehicleTripItem = VehicleTripItem(
        id = dto.id.toString(),
        tripNumber = dto.tripNumber,
        origin = dto.origin,
        destination = dto.destination,
        state = dto.state,
        stateLabel = dto.stateLabel,
        scheduledDate = dto.scheduledDate,
        startTime = dto.startTime,
        endTime = dto.endTime,
        driverName = dto.driverName,
        distance = dto.distance,
        duration = dto.duration
    )

    /**
     * Maps VehicleTripsDto to VehicleTripsData domain entity.
     */
    fun mapToVehicleTripsData(dto: VehicleTripsDto): VehicleTripsData = VehicleTripsData(
        summary = dto.summary?.let { mapTripsSummaryToDomain(it) } ?: TripsSummary(),
        trips = dto.trips.map { mapVehicleTripItemToDomain(it) },
        page = dto.page,
        perPage = dto.perPage,
        totalPages = dto.totalPages,
        hasMore = dto.hasMore
    )

    /**
     * Maps VehicleRouteDto to RouteInfo domain entity.
     */
    fun mapToRouteInfo(dto: VehicleRouteDto): RouteInfo = RouteInfo(
        hasActiveTrip = dto.hasActiveTrip,
        tripId = dto.tripId?.toString(),
        tripNumber = dto.tripNumber,
        driverName = dto.driverName,
        origin = dto.origin,
        destination = dto.destination,
        currentPosition = dto.currentPosition?.let { mapLocationToDomain(it) },
        progress = dto.progress?.let { mapRouteProgressToDomain(it) },
        stops = dto.stops.map { mapRouteStopToDomain(it) }
    )

    /**
     * Maps RouteProgressDto to RouteProgress domain entity.
     */
    private fun mapRouteProgressToDomain(dto: RouteProgressDto): RouteProgress = RouteProgress(
        percentage = dto.percentage,
        distanceCovered = dto.distanceCovered,
        distanceRemaining = dto.distanceRemaining,
        timeElapsed = dto.timeElapsed,
        eta = dto.eta
    )

    /**
     * Maps RouteStopDto to RouteStop domain entity.
     */
    private fun mapRouteStopToDomain(dto: RouteStopDto): RouteStop = RouteStop(
        id = dto.id.toString(),
        sequence = dto.sequence,
        type = dto.type,
        location = dto.location,
        address = dto.address,
        status = dto.status,
        scheduledTime = dto.scheduledTime,
        actualTime = dto.actualTime,
        notes = dto.notes
    )

    /**
     * Maps VehicleDocumentsDetailDto to VehicleDocumentsData domain entity.
     */
    fun mapToVehicleDocumentsData(dto: VehicleDocumentsDetailDto): VehicleDocumentsData = VehicleDocumentsData(
        summary = dto.summary?.let { mapDocumentsSummaryToDomain(it) } ?: DocumentsSummary(),
        documentTypes = dto.documentTypes.map { mapDocumentTypeDetailToDomain(it) },
        alertDocs = dto.alertDocs.map { mapDocumentAlertToDomain(it) },
        otherDocuments = dto.otherDocuments.map { mapVehicleDocumentInfoToDomain(it) }
    )

    /**
     * Maps DocumentTypeDetailDto to DocumentTypeDetail domain entity.
     */
    private fun mapDocumentTypeDetailToDomain(dto: DocumentTypeDetailDto): DocumentTypeDetail = DocumentTypeDetail(
        type = dto.type,
        tag = dto.tag,
        typeName = dto.typeName,
        description = dto.description,
        isRequired = dto.isRequired,
        isUploaded = dto.isUploaded,
        document = dto.document?.let { mapVehicleDocumentInfoToDomain(it) }
    )

    /**
     * Maps VehicleDocumentInfoDto to VehicleDocumentInfo domain entity.
     */
    private fun mapVehicleDocumentInfoToDomain(dto: VehicleDocumentInfoDto): VehicleDocumentInfo = VehicleDocumentInfo(
        id = dto.id.toString(),
        name = dto.name,
        documentNumber = dto.documentNumber,
        expiryDate = dto.expiryDate,
        status = dto.status,
        statusLabel = dto.statusLabel,
        daysRemaining = dto.daysRemaining,
        fileUrl = dto.fileUrl,
        uploadedAt = dto.uploadedAt
    )

    // ==================== Existing Backend API Mappers ====================

    /**
     * Maps list of VehicleDocumentDto to VehicleDocumentsData.
     * Used when falling back to GET /vehicles/:id/documents API.
     */
    fun mapDocumentsToVehicleDocumentsData(documents: List<VehicleDocumentDto>): VehicleDocumentsData {
        val documentTypeMap = mapOf(
            "registration_certificate" to Pair("RC", "Registration Certificate"),
            "insurance" to Pair("INS", "Insurance"),
            "puc_certificate" to Pair("PUC", "PUC Certificate"),
            "fitness_certificate" to Pair("FC", "Fitness Certificate"),
            "road_tax" to Pair("RT", "Road Tax"),
            "permit" to Pair("PERMIT", "Permit")
        )

        val requiredTypes = setOf("registration_certificate", "insurance", "puc_certificate", "fitness_certificate", "road_tax")
        val allTypes = listOf("registration_certificate", "insurance", "puc_certificate", "fitness_certificate", "road_tax", "permit")

        val uploadedDocs = documents.associateBy { it.documentType }

        val documentTypes = allTypes.map { type ->
            val tagInfo = documentTypeMap[type] ?: Pair(type.uppercase(), type.replace("_", " ").replaceFirstChar { it.uppercase() })
            val doc = uploadedDocs[type]

            DocumentTypeDetail(
                type = type,
                tag = tagInfo.first,
                typeName = tagInfo.second,
                description = "Vehicle ${tagInfo.second}",
                isRequired = type in requiredTypes,
                isUploaded = doc != null,
                document = doc?.let { mapVehicleDocumentDtoToInfo(it) }
            )
        }

        val uploaded = documents.size
        val notUploaded = allTypes.size - uploaded
        val expiringSoon = documents.count { isExpiringSoon(it.expiryDate) }
        val expired = documents.count { isExpired(it.expiryDate) }
        val active = documents.count { it.status == "verified" || it.status == "active" }
        val pending = documents.count { it.status == "pending" }

        val alerts = mutableListOf<DocumentAlert>()

        // Add alerts for missing required documents
        allTypes.filter { it in requiredTypes && it !in uploadedDocs.keys }.forEach { type ->
            val tagInfo = documentTypeMap[type] ?: Pair(type.uppercase(), type)
            alerts.add(DocumentAlert(
                type = type,
                typeName = tagInfo.second,
                alertType = "not_uploaded",
                message = "Required but not uploaded",
                daysRemaining = null
            ))
        }

        // Add alerts for expiring documents
        documents.filter { isExpiringSoon(it.expiryDate) }.forEach { doc ->
            val tagInfo = documentTypeMap[doc.documentType] ?: Pair(doc.documentType.uppercase(), doc.documentType)
            val daysRemaining = calculateDaysRemaining(doc.expiryDate)
            alerts.add(DocumentAlert(
                type = doc.documentType,
                typeName = tagInfo.second,
                alertType = "expiring_soon",
                message = "Expiring in $daysRemaining days",
                daysRemaining = daysRemaining
            ))
        }

        return VehicleDocumentsData(
            summary = DocumentsSummary(
                total = allTypes.size,
                uploaded = uploaded,
                notUploaded = notUploaded,
                active = active,
                expiringSoon = expiringSoon,
                expired = expired,
                pending = pending,
                alerts = alerts
            ),
            documentTypes = documentTypes,
            alertDocs = alerts,
            otherDocuments = emptyList()
        )
    }

    private fun mapVehicleDocumentDtoToInfo(dto: VehicleDocumentDto): VehicleDocumentInfo {
        val daysRemaining = calculateDaysRemaining(dto.expiryDate)
        val statusLabel = when (dto.status) {
            "verified" -> "Verified"
            "pending" -> "Pending"
            "rejected" -> "Rejected"
            "expired" -> "Expired"
            else -> dto.status.replaceFirstChar { it.uppercase() }
        }

        return VehicleDocumentInfo(
            id = dto.id.toString(),
            name = dto.documentName,
            documentNumber = dto.documentNumber,
            expiryDate = dto.expiryDate,
            status = dto.status,
            statusLabel = statusLabel,
            daysRemaining = daysRemaining,
            fileUrl = dto.fileUrl,
            uploadedAt = dto.createdAt
        )
    }

    /**
     * Maps list of VehicleTripDto to VehicleTripsData.
     * Used when falling back to GET /trips?vehicle_id= API.
     */
    fun mapTripsToVehicleTripsData(trips: List<VehicleTripDto>, page: Int, perPage: Int): VehicleTripsData {
        val total = trips.size
        val planned = trips.count { it.state == "planned" }
        val inProgress = trips.count { it.state == "in_progress" }
        val completed = trips.count { it.state == "completed" }
        val cancelled = trips.count { it.state == "cancelled" }

        val tripItems = trips.map { mapVehicleTripDtoToItem(it) }

        return VehicleTripsData(
            summary = TripsSummary(
                total = total,
                planned = planned,
                inProgress = inProgress,
                completed = completed,
                cancelled = cancelled,
                recentTrips = tripItems.take(5)
            ),
            trips = tripItems,
            page = page,
            perPage = perPage,
            totalPages = 1, // API doesn't return total pages in fallback
            hasMore = trips.size >= perPage
        )
    }

    private fun mapVehicleTripDtoToItem(dto: VehicleTripDto): VehicleTripItem {
        val stateLabel = when (dto.state) {
            "planned" -> "Planned"
            "in_progress" -> "In Progress"
            "completed" -> "Completed"
            "cancelled" -> "Cancelled"
            else -> dto.state.replace("_", " ").replaceFirstChar { it.uppercase() }
        }

        val driverName = dto.driver?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() }

        return VehicleTripItem(
            id = dto.id.toString(),
            tripNumber = null, // Not available in existing API
            origin = dto.startLocation ?: "",
            destination = dto.endLocation ?: "",
            state = dto.state,
            stateLabel = stateLabel,
            scheduledDate = dto.scheduledDate,
            startTime = dto.startTime,
            endTime = dto.endTime,
            driverName = driverName,
            distance = dto.distance,
            duration = null // Calculate if needed
        )
    }

    // Helper functions for date calculations
    private fun isExpiringSoon(expiryDate: String?): Boolean {
        if (expiryDate == null) return false
        val daysRemaining = calculateDaysRemaining(expiryDate)
        return daysRemaining in 1..30
    }

    private fun isExpired(expiryDate: String?): Boolean {
        if (expiryDate == null) return false
        val daysRemaining = calculateDaysRemaining(expiryDate)
        return daysRemaining != null && daysRemaining <= 0
    }

    private fun calculateDaysRemaining(expiryDate: String?): Int? {
        if (expiryDate == null) return null
        return try {
            // Simple calculation - in production use kotlinx-datetime
            // For now return a placeholder based on string comparison
            // Format expected: YYYY-MM-DD
            30 // Placeholder
        } catch (e: Exception) {
            null
        }
    }
}

