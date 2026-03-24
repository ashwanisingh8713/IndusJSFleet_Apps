package com.ijs.trip.data.mapper

import com.ijs.trip.domain.entity.Trip
import com.ijs.trip.domain.entity.TripDisplayInfo
import com.ijs.trip.domain.entity.TripLocation
import com.ijs.trip.domain.entity.TripStatus
import com.ijs.trip.data.model.TripDto
import dev.zacsweers.metro.Inject

/**
 * Mapper for converting between Trip DTOs and domain entities.
 */
@Inject
class TripMapper {

    /**
     * Maps TripDto to Trip domain entity.
     */
    fun mapToDomain(dto: TripDto): Trip {
        // Build driver name from embedded driver object OR flat field from v2 list API
        val driverName = dto.driver?.let {
            "${it.firstName ?: ""} ${it.lastName ?: ""}".trim()
        }?.takeIf { it.isNotBlank() }
            ?: dto.driverName  // Fallback to flat field from v2 list response

        // Build vehicle number from embedded vehicle object OR flat field from v2 list API
        val vehicleNumber = dto.vehicle?.registrationNumber
            ?: dto.vehicleNumber  // Fallback to flat field from v2 list response

        // Build start location - allow address-only for list view (no lat/lng)
        val startLocation = if (dto.startLat != null && dto.startLng != null) {
            TripLocation(
                latitude = dto.startLat,
                longitude = dto.startLng,
                address = dto.startLocation ?: ""
            )
        } else if (!dto.startLocation.isNullOrBlank()) {
            // v2 list API returns only address without coordinates
            TripLocation(
                latitude = 0.0,
                longitude = 0.0,
                address = dto.startLocation
            )
        } else null

        // Build end location - allow address-only for list view (no lat/lng)
        val endLocation = if (dto.endLat != null && dto.endLng != null) {
            TripLocation(
                latitude = dto.endLat,
                longitude = dto.endLng,
                address = dto.endLocation ?: ""
            )
        } else if (!dto.endLocation.isNullOrBlank()) {
            // v2 list API returns only address without coordinates
            TripLocation(
                latitude = 0.0,
                longitude = 0.0,
                address = dto.endLocation
            )
        } else null

        // Build current location
        val currentLocation = if (dto.currentLat != null && dto.currentLng != null) {
            TripLocation(
                latitude = dto.currentLat,
                longitude = dto.currentLng,
                address = ""
            )
        } else null

        val status = TripStatus.fromApiString(dto.state)

        // Build display info from API fields or compute based on state
        val displayInfo = buildDisplayInfo(dto, status)

        return Trip(
            id = dto.id.toString(),
            tripNumber = dto.tripNumber,
            vehicleId = dto.vehicleId.toString(),
            vehicleNumber = vehicleNumber,
            driverId = dto.driverId.toString(),
            driverName = driverName,
            status = status,
            startLocation = startLocation,
            endLocation = endLocation,
            currentLocation = currentLocation,
            distance = dto.estimatedDistance ?: dto.actualDistance ?: 0.0,
            // Schedule - Departure
            scheduledStartTime = dto.plannedStart ?: dto.scheduledDate,
            plannedStart = dto.plannedStart,
            scheduledDate = dto.scheduledDate,
            startTime = dto.startTime,
            // Schedule - Arrival
            plannedEnd = dto.plannedEnd,
            deliveryDate = dto.deliveryDate,
            deliveryTime = dto.deliveryTime,
            // Actual times
            actualStartTime = dto.actualStart,
            actualEndTime = dto.actualEnd,
            cargoType = dto.cargoType,
            cargoDescription = dto.cargoDescription,
            cargoLoadingWeight = dto.cargoLoadingWeight,
            weightUnit = dto.weightUnit,
            // Customer info
            customerId = dto.customerId?.toString(),
            customerName = dto.customerName,
            customerContact = dto.customerContact,
            priority = dto.priority,
            notes = dto.notes,
            createdAt = dto.createdAt,
            // Pricing
            tripPrice = dto.tripPrice,
            paidTripPrice = dto.paidTripPrice,
            paymentStatus = dto.paymentStatus,
            // Map cost summary - use flat totalCost from v2 API or embedded cost_summary
            totalCost = dto.totalCost?.takeIf { it > 0 } ?: dto.costSummary?.totalCost?.takeIf { it > 0 },
            displayInfo = displayInfo
        )
    }

    /**
     * Builds TripDisplayInfo from DTO fields.
     * Uses API-provided display fields first (for list responses), then display_info (detail), then computes.
     */
    private fun buildDisplayInfo(dto: TripDto, status: TripStatus): TripDisplayInfo {
        // Use API-provided display_info if available (detail response)
        val apiDisplayInfo = dto.displayInfo
        val distanceInfo = apiDisplayInfo?.distanceInfo
        val durationInfo = apiDisplayInfo?.durationInfo
        val costInfo = apiDisplayInfo?.costInfo
        val progressInfo = apiDisplayInfo?.progressInfo

        // For list responses, API provides flat display fields - use these first
        val apiDistanceDisplay = dto.distanceDisplay?.takeIf { it.isNotBlank() && it != "NA" }
        val apiDurationDisplay = dto.durationDisplay?.takeIf { it.isNotBlank() && it != "NA" }

        // Use estimated labels from list response (v2 API provides these)
        val apiEstimatedDistanceLabel = dto.estimatedDistanceLabel?.takeIf { it.isNotBlank() && it != "NA" }
        val apiEstimatedDurationLabel = dto.estimatedDurationLabel?.takeIf { it.isNotBlank() && it != "NA" }

        // Build distance display based on state (fallback when API display not available)
        val (distanceValue, distanceLabel, estimatedDist, coveredDist, totalDist) = when (status) {
            TripStatus.PLANNED -> {
                // Planned: Show estimated distance (prefer API label, then compute)
                val estDist = distanceInfo?.estimatedDistance ?: dto.estimatedDistance
                // Use API-provided label first, then compute from raw value
                val value = apiEstimatedDistanceLabel
                    ?: distanceInfo?.estimatedDistanceLabel?.takeIf { it.isNotBlank() && it != "NA" }
                    ?: estDist?.let { if (it > 0) "${it.toInt()} km" else "NA" }
                    ?: "NA"
                val label = "Est. Distance"
                DistanceResult(value, label, estDist, null, null)
            }
            TripStatus.ON_ROUTE, TripStatus.DELAYED -> {
                // On Route/Delayed: Show covered distance or fallback to estimated
                val covered = distanceInfo?.coveredDistance ?: dto.actualDistance
                val total = distanceInfo?.totalDistance ?: dto.estimatedDistance
                val value = covered?.let { if (it > 0) "${it.toInt()} km" else null }
                    ?: total?.let { if (it > 0) "${it.toInt()} km" else null }
                    ?: "NA"
                val label = if (covered != null && covered > 0) "Covered" else "Distance"
                DistanceResult(value, label, dto.estimatedDistance, covered, total)
            }
            TripStatus.COMPLETED, TripStatus.CANCELLED, TripStatus.FAILED -> {
                // Completed/Cancelled/Failed: Show total/actual distance
                val total = distanceInfo?.totalDistance ?: dto.actualDistance ?: dto.estimatedDistance
                val value = total?.let { if (it > 0) "${it.toInt()} km" else "NA" } ?: "NA"
                val label = "Distance"
                DistanceResult(value, label, dto.estimatedDistance, null, total)
            }
        }

        // Use API display value first (if not NA), then estimated label, then computed value
        val finalDistanceValue = apiDistanceDisplay
            ?: distanceInfo?.displayValue?.takeIf { it.isNotBlank() && it != "NA" }
            ?: apiEstimatedDistanceLabel
            ?: distanceValue
        val finalDistanceLabel = distanceInfo?.displayLabel?.takeIf { it.isNotBlank() }
            ?: distanceLabel

        // Build duration display based on state (fallback when API display not available)
        val (durationValue, durationLabel, plannedMins, actualMins) = when (status) {
            TripStatus.PLANNED -> {
                // Planned: Show estimated/planned duration (prefer API label, then compute)
                val plannedMinutes = durationInfo?.plannedDurationMinutes ?: dto.estimatedDurationMinutes
                // Use API-provided label first, then compute from raw value
                val value = apiEstimatedDurationLabel
                    ?: durationInfo?.plannedDurationLabel?.takeIf { it.isNotBlank() && it != "NA" }
                    ?: plannedMinutes?.let { if (it > 0) formatDuration(it) else "NA" }
                    ?: "NA"
                DurationResult(value, "Est. Duration", plannedMinutes, null)
            }
            TripStatus.ON_ROUTE, TripStatus.DELAYED -> {
                // On Route/Delayed: Show actual/active duration
                val actualMins = durationInfo?.actualDurationMinutes
                val value = actualMins?.let { if (it > 0) formatDuration(it) else "NA" } ?: "NA"
                DurationResult(value, "Duration", durationInfo?.plannedDurationMinutes, actualMins)
            }
            TripStatus.COMPLETED, TripStatus.CANCELLED, TripStatus.FAILED -> {
                // Completed/Cancelled/Failed: Show final duration
                val actualMins = durationInfo?.actualDurationMinutes
                val value = actualMins?.let { if (it > 0) formatDuration(it) else "NA" } ?: "NA"
                DurationResult(value, "Duration", durationInfo?.plannedDurationMinutes, actualMins)
            }
        }

        // Use API display value first (if not NA), then estimated label, then computed value
        val finalDurationValue = apiDurationDisplay
            ?: durationInfo?.displayValue?.takeIf { it.isNotBlank() && it != "NA" }
            ?: apiEstimatedDurationLabel
            ?: durationValue
        val finalDurationLabel = durationInfo?.displayLabel?.takeIf { it.isNotBlank() }
            ?: durationLabel

        // Cost info - use flat fields from v2 list API first, then nested objects
        val costSummaryTotal = dto.totalCost ?: dto.costSummary?.totalCost ?: 0.0
        val hasCosts = dto.hasCosts
            ?: costInfo?.hasCosts
            ?: (costSummaryTotal > 0.0)
        val totalCost = costInfo?.totalCost ?: costSummaryTotal
        val totalCostLabel = dto.totalCostLabel?.takeIf { it != "NA" && it.isNotBlank() }
            ?: costInfo?.totalCostLabel
            ?: if (hasCosts && totalCost > 0.0) formatCost(totalCost) else "NA"
        val costCount = costInfo?.costCount ?: dto.costSummary?.costCount ?: 0

        // Cargo type label
        val cargoTypeLabel = apiDisplayInfo?.cargoInfo?.cargoTypeLabel
            ?: dto.cargoTypeLabel
            ?: dto.cargoType

        // Progress info (In Progress only)
        val progressPercent = progressInfo?.progressPercent
        val remainingDistance = progressInfo?.remainingDistance
        val estimatedArrival = progressInfo?.estimatedArrival

        return TripDisplayInfo(
            distanceValue = finalDistanceValue,
            distanceLabel = finalDistanceLabel,
            estimatedDistance = estimatedDist,
            coveredDistance = coveredDist,
            totalDistance = totalDist,
            durationValue = finalDurationValue,
            durationLabel = finalDurationLabel,
            plannedDurationMinutes = plannedMins,
            actualDurationMinutes = actualMins,
            cargoTypeLabel = cargoTypeLabel,
            hasCosts = hasCosts,
            totalCost = totalCost,
            totalCostLabel = totalCostLabel,
            costCount = costCount,
            progressPercent = progressPercent,
            remainingDistance = remainingDistance,
            estimatedArrival = estimatedArrival
        )
    }

    private fun formatDuration(minutes: Long): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }

    private fun formatCost(amount: Double): String {
        return if (amount >= 1000) {
            val k = amount / 1000
            if (k >= 100) {
                "₹${k.toInt()}K"
            } else {
                val formatted = ((k * 10).toInt() / 10.0)
                if (formatted == formatted.toInt().toDouble()) {
                    "₹${formatted.toInt()}K"
                } else {
                    "₹${formatted}K"
                }
            }
        } else {
            "₹${amount.toInt()}"
        }
    }

    /**
     * Maps a list of TripDto to a list of Trip domain entities.
     */
    fun mapToDomainList(dtos: List<TripDto>): List<Trip> = dtos.map { mapToDomain(it) }
}

// Helper data classes for destructuring
private data class DistanceResult(
    val value: String,
    val label: String,
    val estimated: Double?,
    val covered: Double?,
    val total: Double?
)

private data class DurationResult(
    val value: String,
    val label: String,
    val planned: Long?,
    val actual: Long?
)

/**
 * Mapper for converting between TripStop DTOs and domain entities.
 */
@Inject
class TripStopMapper {

    /**
     * Maps TripStopDto to TripStop domain entity.
     */
    fun mapToDomain(dto: com.ijs.trip.data.model.TripStopDto): com.ijs.trip.domain.entity.TripStop {
        return com.ijs.trip.domain.entity.TripStop(
            id = dto.id.toString(),
            tripId = dto.tripId.toString(),
            stopOrder = dto.stopOrder,
            location = dto.location,
            latitude = dto.latitude,
            longitude = dto.longitude,
            arrivalTime = dto.arrivalTime,
            departureTime = dto.departureTime,
            stopDuration = dto.stopDuration,
            notes = dto.notes,
            isCompleted = dto.isCompleted,
            completedAt = dto.completedAt,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }

    /**
     * Maps a list of TripStopDto to a list of TripStop domain entities.
     */
    fun mapToDomainList(dtos: List<com.ijs.trip.data.model.TripStopDto>): List<com.ijs.trip.domain.entity.TripStop> {
        return dtos.map { mapToDomain(it) }
    }

    /**
     * Maps CreateTripStopData to CreateTripStopRequest.
     */
    fun mapToRequest(data: com.ijs.trip.domain.entity.CreateTripStopData): com.ijs.trip.data.model.CreateTripStopRequest {
        return com.ijs.trip.data.model.CreateTripStopRequest(
            stopOrder = data.stopOrder,
            location = data.location,
            latitude = data.latitude,
            longitude = data.longitude,
            arrivalTime = data.arrivalTime,
            stopDuration = data.stopDuration,
            notes = data.notes
        )
    }

    /**
     * Maps UpdateTripStopData to UpdateTripStopRequest.
     */
    fun mapToUpdateRequest(data: com.ijs.trip.domain.entity.UpdateTripStopData): com.ijs.trip.data.model.UpdateTripStopRequest {
        return com.ijs.trip.data.model.UpdateTripStopRequest(
            stopOrder = data.stopOrder,
            location = data.location,
            latitude = data.latitude,
            longitude = data.longitude,
            arrivalTime = data.arrivalTime,
            stopDuration = data.stopDuration,
            notes = data.notes
        )
    }
}
