package com.indusjs.fleet.data.mapper.trip

import com.indusjs.fleet.domain.entity.trip.Trip
import com.indusjs.fleet.domain.entity.trip.TripDisplayInfo
import com.indusjs.fleet.domain.entity.trip.TripLocation
import com.indusjs.fleet.domain.entity.trip.TripStatus
import com.indusjs.fleet.data.model.trip.TripDto
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
        // Build driver name from embedded driver object or fallback
        val driverName = dto.driver?.let {
            "${it.firstName ?: ""} ${it.lastName ?: ""}".trim()
        }?.takeIf { it.isNotBlank() }

        // Build vehicle number from embedded vehicle object
        val vehicleNumber = dto.vehicle?.registrationNumber

        // Build start location
        val startLocation = if (dto.startLat != null && dto.startLng != null) {
            TripLocation(
                latitude = dto.startLat,
                longitude = dto.startLng,
                address = dto.startLocation ?: ""
            )
        } else null

        // Build end location
        val endLocation = if (dto.endLat != null && dto.endLng != null) {
            TripLocation(
                latitude = dto.endLat,
                longitude = dto.endLng,
                address = dto.endLocation ?: ""
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
            scheduledStartTime = dto.plannedStart,
            actualStartTime = dto.actualStart,
            actualEndTime = dto.actualEnd,
            cargoType = dto.cargoType,
            cargoDescription = dto.cargoDescription,
            customerName = dto.customerName,
            priority = dto.priority,
            notes = dto.notes,
            createdAt = dto.createdAt,
            // Map cost summary - use totalCost from embedded cost_summary
            totalCost = dto.costSummary?.totalCost?.takeIf { it > 0 },
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
        val apiDistanceDisplay = dto.distanceDisplay?.takeIf { it.isNotBlank() }
        val apiDurationDisplay = dto.durationDisplay?.takeIf { it.isNotBlank() }

        // Build distance display based on state (fallback when API display not available)
        val (distanceValue, distanceLabel, estimatedDist, coveredDist, totalDist) = when (status) {
            TripStatus.PLANNED -> {
                // Planned: Show estimated distance
                val estDist = distanceInfo?.estimatedDistance ?: dto.estimatedDistance
                val value = estDist?.let { if (it > 0) "${it.toInt()} km" else "NA" } ?: "NA"
                val label = "Est. Distance"
                DistanceResult(value, label, estDist, null, null)
            }
            TripStatus.IN_PROGRESS -> {
                // In Progress: Show covered distance or fallback to estimated
                val covered = distanceInfo?.coveredDistance ?: dto.actualDistance
                val total = distanceInfo?.totalDistance ?: dto.estimatedDistance
                val value = covered?.let { if (it > 0) "${it.toInt()} km" else null }
                    ?: total?.let { if (it > 0) "${it.toInt()} km" else null }
                    ?: "NA"
                val label = if (covered != null && covered > 0) "Covered" else "Distance"
                DistanceResult(value, label, dto.estimatedDistance, covered, total)
            }
            TripStatus.COMPLETED, TripStatus.CANCELLED -> {
                // Completed: Show total/actual distance
                val total = distanceInfo?.totalDistance ?: dto.actualDistance ?: dto.estimatedDistance
                val value = total?.let { if (it > 0) "${it.toInt()} km" else "NA" } ?: "NA"
                val label = "Distance"
                DistanceResult(value, label, dto.estimatedDistance, null, total)
            }
        }

        // Use API display value first, then computed value
        val finalDistanceValue = apiDistanceDisplay
            ?: distanceInfo?.displayValue?.takeIf { it.isNotBlank() }
            ?: distanceValue
        val finalDistanceLabel = distanceInfo?.displayLabel?.takeIf { it.isNotBlank() }
            ?: distanceLabel

        // Build duration display based on state (fallback when API display not available)
        val (durationValue, durationLabel, plannedMins, actualMins) = when (status) {
            TripStatus.PLANNED -> {
                // Planned: Show 'NA' for duration (trip hasn't started)
                DurationResult("NA", "Duration", durationInfo?.plannedDurationMinutes, null)
            }
            TripStatus.IN_PROGRESS -> {
                // In Progress: Show actual/active duration
                val actualMins = durationInfo?.actualDurationMinutes
                val value = actualMins?.let { if (it > 0) formatDuration(it) else "NA" } ?: "NA"
                DurationResult(value, "Duration", durationInfo?.plannedDurationMinutes, actualMins)
            }
            TripStatus.COMPLETED, TripStatus.CANCELLED -> {
                // Completed: Show final duration
                val actualMins = durationInfo?.actualDurationMinutes
                val value = actualMins?.let { if (it > 0) formatDuration(it) else "NA" } ?: "NA"
                DurationResult(value, "Duration", durationInfo?.plannedDurationMinutes, actualMins)
            }
        }

        // Use API display value first, then computed value
        val finalDurationValue = apiDurationDisplay
            ?: durationInfo?.displayValue?.takeIf { it.isNotBlank() }
            ?: durationValue
        val finalDurationLabel = durationInfo?.displayLabel?.takeIf { it.isNotBlank() }
            ?: durationLabel

        // Cost info
        val costSummaryTotal = dto.costSummary?.totalCost ?: 0.0
        val hasCosts = dto.hasCosts
            ?: costInfo?.hasCosts
            ?: (costSummaryTotal > 0.0)
        val totalCost = costInfo?.totalCost ?: costSummaryTotal
        val totalCostLabel = costInfo?.totalCostLabel
            ?: dto.totalCostLabel
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

