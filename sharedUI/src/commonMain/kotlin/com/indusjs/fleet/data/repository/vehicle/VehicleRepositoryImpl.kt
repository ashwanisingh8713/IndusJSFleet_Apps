package com.indusjs.fleet.data.repository.vehicle

import com.indusjs.fleet.core.error.ApiException
import com.indusjs.fleet.core.error.NotAuthenticatedException
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.datasource.vehicle.VehicleRemoteDataSource
import com.indusjs.fleet.data.mapper.vehicle.VehicleMapper
import com.indusjs.fleet.data.model.vehicle.CreateVehicleWithDocumentsRequest
import com.indusjs.fleet.data.model.vehicle.DocumentFileData
import com.indusjs.fleet.domain.entity.vehicle.DocumentType
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.entity.vehicle.VehicleDocument
import com.indusjs.fleet.domain.repository.vehicle.VehicleRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementation of VehicleRepository.
 * Handles vehicle CRUD operations via remote data source.
 */
@Inject
class VehicleRepositoryImpl(
    private val remoteDataSource: VehicleRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    private val mapper: VehicleMapper
) : VehicleRepository {

    override fun getVehicles(): Flow<Result<List<Vehicle>>> = flow {
        emit(Result.Loading)
        try {
            val token = requireAuthToken()
            val response = remoteDataSource.getVehicles(token)

            if (response.success && response.data != null) {
                emit(Result.Success(mapper.mapToDomainList(response.data)))
            } else {
                emit(Result.Error(ApiException(response.message ?: "Failed to get vehicles"), response.message))
            }
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    override suspend fun getVehicleById(id: String): Result<Vehicle> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getVehicleById(token, id)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Vehicle not found"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun createVehicle(vehicle: Vehicle): Result<Vehicle> {
        return try {
            val token = requireAuthToken()
            val request = mapper.mapToCreateRequest(vehicle)
            val response = remoteDataSource.createVehicle(token, request)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to create vehicle"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun createVehicleWithDocuments(
        vehicle: Vehicle,
        documents: List<VehicleDocument>
    ): Result<Vehicle> {
        return try {
            val token = requireAuthToken()

            // Build the request with vehicle details and documents
            val request = CreateVehicleWithDocumentsRequest(
                registrationNumber = vehicle.registrationNumber,
                make = vehicle.make,
                model = vehicle.model,
                year = vehicle.year,
                vehicleType = mapper.vehicleTypeToApiString(vehicle.type),
                fuelType = vehicle.fuelType.lowercase(),
                capacity = vehicle.capacity,
                color = vehicle.color.lowercase(),
                registrationCertificate = documents.find { it.type == DocumentType.REGISTRATION_CERTIFICATE }
                    ?.let { mapToDocumentFileData(it) },
                insurance = documents.find { it.type == DocumentType.INSURANCE }
                    ?.let { mapToDocumentFileData(it) },
                pucCertificate = documents.find { it.type == DocumentType.PUC_CERTIFICATE }
                    ?.let { mapToDocumentFileData(it) },
                fitnessCertificate = documents.find { it.type == DocumentType.FITNESS_CERTIFICATE }
                    ?.let { mapToDocumentFileData(it) },
                roadTax = documents.find { it.type == DocumentType.ROAD_TAX }
                    ?.let { mapToDocumentFileData(it) },
                permit = documents.find { it.type == DocumentType.PERMIT }
                    ?.let { mapToDocumentFileData(it) }
            )

            val response = remoteDataSource.createVehicleWithDocuments(token, request)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to create vehicle"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    private fun mapToDocumentFileData(doc: VehicleDocument): DocumentFileData? {
        // Note: VehicleDocument should contain fileBytes for upload
        // If fileBytes is not available, return null
        return doc.fileBytes?.let { bytes ->
            DocumentFileData(
                fileName = doc.fileName,
                fileBytes = bytes,
                mimeType = doc.mimeType,
                expiryDate = doc.expiryDate?.let { formatExpiryDate(it) }
            )
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun formatExpiryDate(timestamp: Long): String? {
        // For now, return null as expiry dates aren't being set from the form
        // In production, implement proper date formatting using kotlinx-datetime
        // Format should be: YYYY-MM-DD
        return null
    }

    override suspend fun updateVehicle(vehicle: Vehicle): Result<Vehicle> {
        return try {
            val token = requireAuthToken()
            val request = mapper.mapToCreateRequest(vehicle)
            val response = remoteDataSource.updateVehicle(token, vehicle.id, request)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to update vehicle"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun deleteVehicle(id: String): Result<Unit> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.deleteVehicle(token, id)

            if (response.success) {
                Result.Success(Unit)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to delete vehicle"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    /**
     * Retrieves auth token or throws NotAuthenticatedException.
     */
    private suspend fun requireAuthToken(): String {
        return userLocalDataSource.getAuthToken()
            ?: throw NotAuthenticatedException()
    }
}

