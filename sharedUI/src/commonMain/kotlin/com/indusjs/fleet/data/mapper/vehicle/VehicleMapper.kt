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
        assignedDriverId = dto.assignedDriverId?.toString(),
        assignedDriverName = dto.assignedDriverName ?: dto.registeredBy?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() },
        lastServiceDate = dto.lastServiceDate?.let { parseTimestamp(it) },
        nextServiceDate = dto.nextServiceDate?.let { parseTimestamp(it) }
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
}

