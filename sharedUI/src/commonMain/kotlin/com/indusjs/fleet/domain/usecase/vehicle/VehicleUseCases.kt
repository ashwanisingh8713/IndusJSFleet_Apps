package com.indusjs.fleet.domain.usecase.vehicle

import com.indusjs.error.result.Result
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.entity.vehicle.VehicleDocument
import com.indusjs.fleet.domain.repository.vehicle.VehicleRepository
import com.indusjs.fleet.domain.usecase.UseCase
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting the list of vehicles.
 */
@Inject
class GetVehiclesUseCase(
    private val vehicleRepository: VehicleRepository
) : UseCase<List<Vehicle>> {

    override operator fun invoke(): Flow<Result<List<Vehicle>>> {
        return vehicleRepository.getVehicles()
    }
}

/**
 * Use case for getting a vehicle by ID.
 */
@Inject
class GetVehicleByIdUseCase(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(id: String): Result<Vehicle> {
        return vehicleRepository.getVehicleById(id)
    }
}

/**
 * Use case for creating a new vehicle.
 */
@Inject
class CreateVehicleUseCase(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(vehicle: Vehicle): Result<Vehicle> {
        return vehicleRepository.createVehicle(vehicle)
    }
}

/**
 * Use case for creating a new vehicle with optional documents.
 * Uses multipart form upload to /vehicles/with-documents endpoint.
 */
@Inject
class CreateVehicleWithDocumentsUseCase(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(vehicle: Vehicle, documents: List<VehicleDocument>): Result<Vehicle> {
        return vehicleRepository.createVehicleWithDocuments(vehicle, documents)
    }
}

/**
 * Use case for updating a vehicle.
 */
@Inject
class UpdateVehicleUseCase(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(vehicle: Vehicle): Result<Vehicle> {
        return vehicleRepository.updateVehicle(vehicle)
    }
}

/**
 * Use case for deleting a vehicle.
 */
@Inject
class DeleteVehicleUseCase(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return vehicleRepository.deleteVehicle(id)
    }
}

