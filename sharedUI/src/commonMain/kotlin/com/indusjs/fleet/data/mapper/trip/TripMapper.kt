package com.indusjs.fleet.data.mapper.trip

import com.indusjs.fleet.domain.entity.trip.Trip
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

        return Trip(
            id = dto.id.toString(),
            tripNumber = dto.tripNumber,
            vehicleId = dto.vehicleId.toString(),
            vehicleNumber = vehicleNumber,
            driverId = dto.driverId.toString(),
            driverName = driverName,
            status = TripStatus.fromApiString(dto.state),
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
            createdAt = dto.createdAt
        )
    }

    /**
     * Maps a list of TripDto to a list of Trip domain entities.
     */
    fun mapToDomainList(dtos: List<TripDto>): List<Trip> = dtos.map { mapToDomain(it) }
}
