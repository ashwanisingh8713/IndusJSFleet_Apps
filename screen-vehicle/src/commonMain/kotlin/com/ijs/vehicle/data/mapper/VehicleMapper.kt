package com.ijs.vehicle.data.mapper

import com.ijs.vehicle.domain.entity.AssignedDriver
import com.ijs.vehicle.domain.entity.DocumentAlert
import com.ijs.vehicle.domain.entity.DocumentsSummary
import com.ijs.vehicle.domain.entity.DocumentTypeDetail
import com.ijs.vehicle.domain.entity.Location
import com.ijs.vehicle.domain.entity.RouteInfo
import com.ijs.vehicle.domain.entity.RouteProgress
import com.ijs.vehicle.domain.entity.RouteStop
import com.ijs.vehicle.domain.entity.TripsSummary
import com.ijs.vehicle.domain.entity.Vehicle
import com.ijs.vehicle.domain.entity.VehicleDetail
import com.ijs.vehicle.domain.entity.VehicleDocumentInfo
import com.ijs.vehicle.domain.entity.VehicleDocumentsData
import com.ijs.vehicle.domain.entity.VehicleStats
import com.ijs.vehicle.domain.entity.VehicleStatus
import com.ijs.vehicle.domain.entity.VehicleTripAssignment
import com.ijs.vehicle.domain.entity.VehicleTripItem
import com.ijs.vehicle.domain.entity.VehicleTripsData
import com.ijs.vehicle.domain.entity.VehicleType
import com.ijs.vehicle.data.model.AssignedDriverDto
import com.ijs.vehicle.data.model.CreateVehicleRequest
import com.ijs.vehicle.data.model.UpdateVehicleRequest
import com.ijs.vehicle.data.model.DocumentAlertDto
import com.ijs.vehicle.data.model.DocumentsSummaryDto
import com.ijs.vehicle.data.model.DetailRouteDto
import com.ijs.vehicle.data.model.DocumentTypeDetailDto
import com.ijs.vehicle.data.model.GeoPointDto
import com.ijs.vehicle.data.model.LocationDto
import com.ijs.vehicle.data.model.RouteProgressDto
import com.ijs.vehicle.data.model.RouteStopDto
import com.ijs.vehicle.data.model.TripsSummaryDto
import com.ijs.vehicle.data.model.VehicleDetailDto
import com.ijs.vehicle.data.model.VehicleDocumentDto
import com.ijs.vehicle.data.model.VehicleDocumentInfoDto
import com.ijs.vehicle.data.model.VehicleDocumentsDetailDto
import com.ijs.vehicle.data.model.VehicleDto
import com.ijs.vehicle.data.model.VehicleRouteDto
import com.ijs.vehicle.data.model.VehicleStatsDto
import com.ijs.vehicle.data.model.VehicleTripAssignmentDto
import com.ijs.vehicle.data.model.VehicleTripDto
import com.ijs.vehicle.data.model.VehicleTripItemDto
import com.ijs.vehicle.data.model.VehicleTripsDto
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

        // Convert createdAt (epoch millis) to DD-MM-YYYY for date-picker constraints.
        val createdAtFormatted =
            com.indusjs.datetimeutils.FleetDateTime.timestampToDateString(dto.createdAt)

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
            capacity = dto.capacity?.toInt() ?: 4,
            fuelLevel = dto.fuelLevel,
            mileage = dto.mileage,
            lastLocation = dto.lastLocation?.let { mapLocationToDomain(it) },
            assignedDriverId = dto.assignedDriver?.id?.toString() ?: dto.assignedDriverId?.toString(),
            assignedDriverName = driverName,
            assignedDriver = assignedDriver,
            lastServiceDate = dto.lastServiceDate,
            nextServiceDate = dto.nextServiceDate,
            isOccupied = dto.isOccupied,
            tripAssignment = dto.tripAssignment?.let { mapTripAssignmentToDomain(it) },
            createdAt = createdAtFormatted
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
        lastServiceDate = vehicle.lastServiceDate,
        nextServiceDate = vehicle.nextServiceDate
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
     * API uses: inactive, active, on_route, maintenance, damaged, decommissioned
     */
    private fun parseVehicleStatus(status: String): VehicleStatus =
        VehicleStatus.fromApiString(status)

    /**
     * Convert VehicleStatus enum to API string.
     */
    private fun vehicleStatusToApiString(status: VehicleStatus): String =
        VehicleStatus.toApiString(status)

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
            // /detail returns the FLAT DetailRouteDto, not the nested VehicleRouteDto.
            route = dto.route?.let { mapDetailRouteToRouteInfo(it) }
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
    private fun mapDocumentAlertToDomain(dto: DocumentAlertDto): DocumentAlert {
        // Backend sends name/tag/expiry_date/days_remaining/status/is_expired.
        // Derive the app's alertType + message from those (backend has no such fields).
        val days = dto.daysRemaining
        // A required-but-not-uploaded doc has no expiry_date and is not expired — must not be labeled "Expired".
        val notUploaded = dto.expiryDate == null && !dto.isExpired
        val alertType = when {
            notUploaded -> "not_uploaded"
            dto.isExpired || (days != null && days <= 0) -> "expired"
            else -> "expiring_soon"
        }
        val message = when {
            notUploaded -> "Not uploaded"
            dto.isExpired || (days != null && days <= 0) -> "Expired"
            days != null -> "Expiring in $days days"
            else -> dto.status
        }
        return DocumentAlert(
            type = dto.type,
            typeName = dto.name,
            alertType = alertType,
            message = message,
            daysRemaining = days
        )
    }

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
        origin = dto.resolvedOrigin,
        destination = dto.resolvedDestination,
        state = dto.state,
        stateLabel = dto.stateLabel,
        scheduledDate = dto.scheduledDate,
        startTime = dto.startTime,
        endTime = dto.endTime,
        driverName = dto.resolvedDriverName,
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
        // backend TripsTabResponse has no has_more → derive from pagination
        hasMore = dto.page < dto.totalPages
    )

    /**
     * Maps VehicleRouteDto to RouteInfo domain entity.
     */
    fun mapToRouteInfo(dto: VehicleRouteDto): RouteInfo {
        // Backend nests the active trip under "trip", geo under "route", and the
        // current position inside route.current — flatten into the flat domain shape.
        val driverName = dto.trip?.driver?.let {
            "${it.firstName ?: ""} ${it.lastName ?: ""}".trim().takeIf { n -> n.isNotBlank() }
        }
        val current = dto.route?.current?.let { mapGeoPointToLocation(it) }
        return RouteInfo(
            hasActiveTrip = dto.hasActiveTrip,
            tripId = dto.trip?.id?.toString(),
            tripNumber = null, // backend route response has no trip_number
            driverName = driverName,
            origin = dto.route?.origin?.location,
            destination = dto.route?.destination?.location,
            currentPosition = current,
            progress = dto.progress?.let { mapRouteProgressToDomain(it) },
            stops = dto.stops.map { mapRouteStopToDomain(it) }
        )
    }

    /**
     * Maps the FLAT DetailRouteDto (GET /vehicles/{id}/detail → DetailRouteResponse)
     * to RouteInfo. The flat shape carries no nested trip/geo/stops blocks, so:
     *  - start_location/end_location become origin/destination
     *  - total_stops/completed_stops are folded into a RouteProgress (percentage)
     *  - currentPosition/stops/driverName/tripNumber are unavailable here and stay
     *    null/empty (use GET /vehicles/{id}/route for the full nested route).
     */
    fun mapDetailRouteToRouteInfo(dto: DetailRouteDto): RouteInfo {
        val progress = if (dto.totalStops > 0) {
            RouteProgress(
                percentage = (dto.completedStops * 100) / dto.totalStops,
                distanceCovered = 0.0,
                distanceRemaining = 0.0,
                timeElapsed = null,
                eta = null
            )
        } else {
            null
        }
        return RouteInfo(
            hasActiveTrip = dto.hasActiveTrip,
            tripId = dto.tripId.takeIf { it > 0 }?.toString(),
            tripNumber = null,
            driverName = null,
            origin = dto.startLocation.takeIf { it.isNotBlank() },
            destination = dto.endLocation.takeIf { it.isNotBlank() },
            currentPosition = null,
            progress = progress,
            stops = emptyList()
        )
    }

    private fun mapGeoPointToLocation(dto: GeoPointDto): Location = Location(
        latitude = dto.latitude,
        longitude = dto.longitude,
        address = dto.location
    )

    /**
     * Maps RouteProgressDto to RouteProgress domain entity.
     * Backend has no distance covered/remaining; those stay 0.
     */
    private fun mapRouteProgressToDomain(dto: RouteProgressDto): RouteProgress = RouteProgress(
        percentage = dto.percentage.toInt(),
        distanceCovered = 0.0,
        distanceRemaining = 0.0,
        timeElapsed = dto.timeElapsed,
        eta = dto.eta
    )

    /**
     * Maps RouteStopDto to RouteStop domain entity.
     * Backend has no type/address/scheduled_time/actual_time; those stay null/blank.
     */
    private fun mapRouteStopToDomain(dto: RouteStopDto): RouteStop = RouteStop(
        id = dto.id.toString(),
        sequence = dto.sequence,
        type = "",
        location = dto.location,
        address = dto.location.takeIf { it.isNotBlank() },
        status = dto.status,
        scheduledTime = null,
        actualTime = null,
        notes = dto.notes
    )

    /**
     * Maps VehicleDocumentsDetailDto to VehicleDocumentsData domain entity.
     */
    fun mapToVehicleDocumentsData(dto: VehicleDocumentsDetailDto): VehicleDocumentsData {
        // Alerts live inside summary.alert_docs (backend DocSummaryResponse), not top-level.
        val summaryDto = dto.summary
        return VehicleDocumentsData(
            summary = summaryDto?.let { mapDocumentsSummaryToDomain(it) } ?: DocumentsSummary(),
            documentTypes = dto.documentTypes.map { mapDocumentTypeDetailToDomain(it) },
            alertDocs = summaryDto?.alerts?.map { mapDocumentAlertToDomain(it) } ?: emptyList(),
            otherDocuments = dto.otherDocuments.map { mapVehicleDocumentInfoToDomain(it) }
        )
    }

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
        // Backend DocItemResponse now sends download_url (relative, includes /api/v1).
        fileUrl = dto.downloadUrl,
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
            // Backend DocumentResponse now sends download_url (relative, includes /api/v1).
            fileUrl = dto.downloadUrl,
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

    // Helper functions for date calculations (epoch-millis via FleetEpoch).
    private fun isExpiringSoon(expiryDate: Long?): Boolean {
        val daysRemaining = calculateDaysRemaining(expiryDate)
        return daysRemaining != null && daysRemaining in 1..30
    }

    private fun isExpired(expiryDate: Long?): Boolean {
        val daysRemaining = calculateDaysRemaining(expiryDate)
        return daysRemaining != null && daysRemaining <= 0
    }

    /** Whole UTC days from now until [expiryDate] (negative if past), or null when unset. */
    private fun calculateDaysRemaining(expiryDate: Long?): Int? {
        if (expiryDate == null || expiryDate <= 0L) return null
        return com.indusjs.datetimeutils.FleetEpoch.daysUntil(expiryDate)?.toInt()
    }
}

