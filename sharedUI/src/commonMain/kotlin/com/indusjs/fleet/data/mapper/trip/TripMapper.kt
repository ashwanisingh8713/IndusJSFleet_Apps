package com.indusjs.fleet.data.mapper.trip

import com.indusjs.fleet.domain.entity.trip.Trip
import com.indusjs.fleet.domain.entity.trip.TripLocation
import com.indusjs.fleet.domain.entity.trip.TripStatus
import com.indusjs.fleet.data.model.trip.CreateTripRequest
import com.indusjs.fleet.data.model.trip.TripDto
import com.indusjs.fleet.data.model.trip.TripLocationDto
import dev.zacsweers.metro.Inject

/**
 * Mapper for converting between Trip DTOs and domain entities.
 */
@Inject
class TripMapper {

    /**
     * Maps TripDto to Trip domain entity.
     */
    fun mapToDomain(dto: TripDto): Trip = Trip(
        id = dto.id,
        tripNumber = dto.tripNumber,
        vehicleId = dto.vehicleId,
        vehicleNumber = dto.vehicleNumber,
        driverId = dto.driverId,
        driverName = dto.driverName,
        status = parseTripStatus(dto.status),
        startLocation = mapLocationToDomain(dto.startLocation),
        endLocation = mapLocationToDomain(dto.endLocation),
        currentLocation = dto.currentLocation?.let { mapLocationToDomain(it) },
        distance = dto.distance,
        estimatedDuration = dto.estimatedDuration,
        actualDuration = dto.actualDuration,
        scheduledStartTime = dto.scheduledStartTime,
        actualStartTime = dto.actualStartTime,
        actualEndTime = dto.actualEndTime,
        cargo = dto.cargo,
        notes = dto.notes
    )

    /**
     * Maps a list of TripDto to a list of Trip domain entities.
     */
    fun mapToDomainList(dtos: List<TripDto>): List<Trip> = dtos.map { mapToDomain(it) }

    /**
     * Maps Trip domain entity to TripDto.
     */
    fun mapToData(trip: Trip): TripDto = TripDto(
        id = trip.id,
        tripNumber = trip.tripNumber,
        vehicleId = trip.vehicleId,
        vehicleNumber = trip.vehicleNumber,
        driverId = trip.driverId,
        driverName = trip.driverName,
        status = trip.status.name,
        startLocation = mapLocationToData(trip.startLocation),
        endLocation = mapLocationToData(trip.endLocation),
        currentLocation = trip.currentLocation?.let { mapLocationToData(it) },
        distance = trip.distance,
        estimatedDuration = trip.estimatedDuration,
        actualDuration = trip.actualDuration,
        scheduledStartTime = trip.scheduledStartTime,
        actualStartTime = trip.actualStartTime,
        actualEndTime = trip.actualEndTime,
        cargo = trip.cargo,
        notes = trip.notes
    )

    /**
     * Maps Trip domain entity to CreateTripRequest.
     */
    fun mapToCreateRequest(trip: Trip): CreateTripRequest = CreateTripRequest(
        vehicleId = trip.vehicleId,
        driverId = trip.driverId,
        startLocation = mapLocationToData(trip.startLocation),
        endLocation = mapLocationToData(trip.endLocation),
        scheduledStartTime = trip.scheduledStartTime,
        cargo = trip.cargo,
        notes = trip.notes
    )

    private fun mapLocationToDomain(dto: TripLocationDto): TripLocation = TripLocation(
        latitude = dto.latitude,
        longitude = dto.longitude,
        address = dto.address
    )

    private fun mapLocationToData(location: TripLocation): TripLocationDto = TripLocationDto(
        latitude = location.latitude,
        longitude = location.longitude,
        address = location.address
    )

    private fun parseTripStatus(status: String): TripStatus = try {
        TripStatus.valueOf(status.uppercase())
    } catch (e: IllegalArgumentException) {
        TripStatus.SCHEDULED
    }
}
