package com.indusjs.fleet.di.adapter

import com.indusjs.error.result.Result
import com.indusjs.fleet.domain.entity.maps.MapLocation
import com.indusjs.fleet.domain.entity.maps.MapVehicle
import com.indusjs.fleet.domain.entity.maps.MapVehicleStatus
import com.ijs.map.domain.MapVehicleProvider
import com.ijs.vehicle.domain.entity.Vehicle
import com.ijs.vehicle.domain.entity.VehicleStatus
import com.ijs.vehicle.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.first

/**
 * Adapter that bridges [VehicleRepository] (screen-vehicle) to [MapVehicleProvider]
 * (screen-map), so the map can seed markers from the tenant's real vehicles
 * without screen-map depending on the vehicle feature.
 *
 * Mirrors the existing [TripProviderAdapter] cross-feature pattern.
 */
class MapVehicleProviderAdapter(
    private val vehicleRepository: VehicleRepository
) : MapVehicleProvider {

    override suspend fun getMapVehicles(): Result<List<MapVehicle>> {
        // getVehicles() is a Flow<Result<...>> that emits Loading then a terminal
        // Success/Error. Take the first non-Loading emission and map it.
        return try {
            when (val result = vehicleRepository.getVehicles().first { it !is Result.Loading }) {
                is Result.Success -> Result.Success(result.data.map { it.toMapVehicle() })
                is Result.Error -> result
                is Result.Loading -> Result.Success(emptyList()) // unreachable given predicate
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    /**
     * Maps a fleet [Vehicle] into a [MapVehicle] seed marker.
     * Vehicles without a known location seed at (0,0)/OFFLINE; the live feed then
     * supplies real coordinates and flips the status.
     */
    private fun Vehicle.toMapVehicle(): MapVehicle {
        val loc = lastLocation
        return MapVehicle(
            id = id,
            vehicleNumber = registrationNumber,
            driverName = assignedDriverName ?: assignedDriver?.fullName(),
            location = MapLocation(
                latitude = loc?.latitude ?: 0.0,
                longitude = loc?.longitude ?: 0.0,
                address = loc?.address
            ),
            status = seedStatus(loc != null),
            speed = 0.0,
            heading = 0.0,
            lastUpdated = 0L
        )
    }

    /**
     * Initial marker status before any live frame arrives.
     * ON_ROUTE vehicles are shown IDLE (awaiting first live position); a vehicle
     * with no known location is OFFLINE; everything else is STOPPED.
     */
    private fun Vehicle.seedStatus(hasLocation: Boolean): MapVehicleStatus = when {
        !hasLocation -> MapVehicleStatus.OFFLINE
        status == VehicleStatus.ON_ROUTE -> MapVehicleStatus.IDLE
        else -> MapVehicleStatus.STOPPED
    }
}
