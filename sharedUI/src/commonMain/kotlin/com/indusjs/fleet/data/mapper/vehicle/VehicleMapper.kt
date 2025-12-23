package com.indusjs.fleet.data.mapper.vehicle

import com.indusjs.fleet.domain.entity.vehicle.Location
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.entity.vehicle.VehicleStatus
import com.indusjs.fleet.domain.entity.vehicle.VehicleType
import com.indusjs.fleet.data.model.vehicle.CreateVehicleRequest
import com.indusjs.fleet.data.model.vehicle.LocationDto
import com.indusjs.fleet.data.model.vehicle.VehicleDto
import dev.zacsweers.metro.Inject

/**
 * Mapper for converting between Vehicle DTOs and domain entities.
 */
@Inject
class VehicleMapper {

    /**
     * Maps VehicleDto to Vehicle domain entity.
     */
    fun mapToDomain(dto: VehicleDto): Vehicle = Vehicle(
        id = dto.id,
        registrationNumber = dto.registrationNumber,
        make = dto.make,
        model = dto.model,
        year = dto.year,
        type = parseVehicleType(dto.type),
        status = parseVehicleStatus(dto.status),
        fuelLevel = dto.fuelLevel,
        mileage = dto.mileage,
        lastLocation = dto.lastLocation?.let { mapLocationToDomain(it) },
        assignedDriverId = dto.assignedDriverId,
        assignedDriverName = dto.assignedDriverName,
        lastServiceDate = dto.lastServiceDate,
        nextServiceDate = dto.nextServiceDate
    )

    /**
     * Maps a list of VehicleDto to a list of Vehicle domain entities.
     */
    fun mapToDomainList(dtos: List<VehicleDto>): List<Vehicle> = dtos.map { mapToDomain(it) }

    /**
     * Maps Vehicle domain entity to VehicleDto.
     */
    fun mapToData(vehicle: Vehicle): VehicleDto = VehicleDto(
        id = vehicle.id,
        registrationNumber = vehicle.registrationNumber,
        make = vehicle.make,
        model = vehicle.model,
        year = vehicle.year,
        type = vehicle.type.name,
        status = vehicle.status.name,
        fuelLevel = vehicle.fuelLevel,
        mileage = vehicle.mileage,
        lastLocation = vehicle.lastLocation?.let { mapLocationToData(it) },
        assignedDriverId = vehicle.assignedDriverId,
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
        type = vehicle.type.name,
        status = vehicle.status.name,
        assignedDriverId = vehicle.assignedDriverId
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

    private fun parseVehicleType(type: String): VehicleType = try {
        VehicleType.valueOf(type.uppercase())
    } catch (e: IllegalArgumentException) {
        VehicleType.TRUCK
    }

    private fun parseVehicleStatus(status: String): VehicleStatus = try {
        VehicleStatus.valueOf(status.uppercase())
    } catch (e: IllegalArgumentException) {
        VehicleStatus.ACTIVE
    }
}

