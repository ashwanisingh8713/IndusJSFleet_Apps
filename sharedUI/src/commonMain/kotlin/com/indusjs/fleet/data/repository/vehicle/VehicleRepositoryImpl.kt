package com.indusjs.fleet.data.repository.vehicle

import com.indusjs.error.exception.ApiException
import com.indusjs.error.exception.AuthException
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.error.result.Result
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.datasource.vehicle.VehicleRemoteDataSource
import com.indusjs.fleet.data.mapper.vehicle.VehicleMapper
import com.indusjs.fleet.data.model.history.VehicleHistoryDataDto
import com.indusjs.fleet.data.model.vehicle.CreateVehicleWithDocumentsRequest
import com.indusjs.fleet.data.model.vehicle.DocumentFileData
import com.indusjs.fleet.domain.entity.vehicle.DocumentsSummary
import com.indusjs.fleet.domain.entity.vehicle.DocumentType
import com.indusjs.fleet.domain.entity.vehicle.RouteInfo
import com.indusjs.fleet.domain.entity.vehicle.TripsSummary
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.entity.vehicle.VehicleDetail
import com.indusjs.fleet.domain.entity.vehicle.VehicleDocument
import com.indusjs.fleet.domain.entity.vehicle.VehicleDocumentsData
import com.indusjs.fleet.domain.entity.vehicle.VehicleStats
import com.indusjs.fleet.domain.entity.vehicle.VehicleTripsData
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
            emit(Result.Error(e, ApiErrorHandler.extractErrorMessage(e)))
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
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
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
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
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
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
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
            val request = mapper.mapToUpdateRequest(vehicle)
            val response = remoteDataSource.updateVehicle(token, vehicle.id, request)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to update vehicle"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
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
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    // ==================== Vehicle Detail APIs ====================

    override suspend fun getVehicleDetail(id: String): Result<VehicleDetail> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getVehicleDetail(token, id)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToVehicleDetail(response.data))
            } else {
                // API not implemented yet - build from existing APIs
                buildVehicleDetailFromExistingApis(id)
            }
        } catch (e: Exception) {
            // API not implemented yet - build from existing APIs
            buildVehicleDetailFromExistingApis(id)
        }
    }

    private suspend fun buildVehicleDetailFromExistingApis(id: String): Result<VehicleDetail> {
        return try {
            val vehicleResult = getVehicleById(id)
            val vehicle = (vehicleResult as? Result.Success)?.data
                ?: return Result.Error(Exception("Vehicle not found"), "Vehicle not found")

            // Try to get trips count
            val tripsResult = getVehicleTrips(id, 1, 100, null)
            val tripsData = (tripsResult as? Result.Success)?.data

            // Try to get documents
            val docsResult = getVehicleDocumentsDetail(id)
            val docsData = (docsResult as? Result.Success)?.data

            Result.Success(VehicleDetail(
                vehicle = vehicle,
                assignedDriver = vehicle.assignedDriver,
                currentLocation = vehicle.lastLocation,
                stats = VehicleStats(
                    totalTrips = tripsData?.summary?.total ?: 0,
                    completedTrips = tripsData?.summary?.completed ?: 0,
                    totalDistance = 0.0,
                    tripsThisMonth = 0,
                    distanceThisMonth = 0.0
                ),
                documents = docsData?.summary ?: DocumentsSummary(),
                trips = tripsData?.summary ?: TripsSummary(),
                route = null
            ))
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun getVehicleTrips(
        id: String,
        page: Int,
        perPage: Int,
        state: String?
    ): Result<VehicleTripsData> {
        return try {
            val token = requireAuthToken()

            // Try new endpoint first
            val response = remoteDataSource.getVehicleTrips(token, id, page, perPage, state)
            if (response.success && response.data != null) {
                return Result.Success(mapper.mapToVehicleTripsData(response.data))
            }

            // Fallback to existing /trips?vehicle_id= endpoint
            val fallbackResponse = remoteDataSource.getTripsByVehicleId(token, id, page, perPage, state)
            if (fallbackResponse.success && fallbackResponse.data != null) {
                Result.Success(mapper.mapTripsToVehicleTripsData(fallbackResponse.data, page, perPage))
            } else {
                // Return empty data if both APIs fail
                Result.Success(VehicleTripsData(
                    summary = TripsSummary(),
                    trips = emptyList(),
                    page = page,
                    perPage = perPage,
                    totalPages = 0,
                    hasMore = false
                ))
            }
        } catch (e: Exception) {
            // Return empty data on error
            Result.Success(VehicleTripsData(
                summary = TripsSummary(),
                trips = emptyList(),
                page = page,
                perPage = perPage,
                totalPages = 0,
                hasMore = false
            ))
        }
    }

    override suspend fun getVehicleRoute(id: String): Result<RouteInfo> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getVehicleRoute(token, id)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToRouteInfo(response.data))
            } else {
                // No active route - return empty info
                Result.Success(RouteInfo(hasActiveTrip = false))
            }
        } catch (e: Exception) {
            // No active route - return empty info
            Result.Success(RouteInfo(hasActiveTrip = false))
        }
    }

    override suspend fun getVehicleDocumentsDetail(id: String): Result<VehicleDocumentsData> {
        return try {
            val token = requireAuthToken()

            // Try new endpoint first
            val response = remoteDataSource.getVehicleDocumentsDetail(token, id)
            if (response.success && response.data != null) {
                return Result.Success(mapper.mapToVehicleDocumentsData(response.data))
            }

            // Fallback to existing /vehicles/:id/documents endpoint
            val fallbackResponse = remoteDataSource.getVehicleDocuments(token, id)
            if (fallbackResponse.success && fallbackResponse.data != null) {
                Result.Success(mapper.mapDocumentsToVehicleDocumentsData(fallbackResponse.data))
            } else {
                // Return empty documents data
                Result.Success(VehicleDocumentsData(
                    summary = DocumentsSummary(total = 6, notUploaded = 6),
                    documentTypes = emptyList(),
                    alertDocs = emptyList(),
                    otherDocuments = emptyList()
                ))
            }
        } catch (e: Exception) {
            // Return empty documents data on error
            Result.Success(VehicleDocumentsData(
                summary = DocumentsSummary(total = 6, notUploaded = 6),
                documentTypes = emptyList(),
                alertDocs = emptyList(),
                otherDocuments = emptyList()
            ))
        }
    }

    override suspend fun uploadDocument(
        vehicleId: String,
        documentType: String,
        documentName: String,
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String,
        documentNumber: String?,
        expiryDate: String?
    ): Result<Unit> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.uploadDocument(
                token = token,
                vehicleId = vehicleId,
                documentType = documentType,
                documentName = documentName,
                fileBytes = fileBytes,
                fileName = fileName,
                mimeType = mimeType,
                documentNumber = documentNumber,
                expiryDate = expiryDate
            )

            if (response.success) {
                Result.Success(Unit)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to upload document"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun downloadDocument(documentId: String): Result<ByteArray> {
        return try {
            val token = requireAuthToken()
            remoteDataSource.downloadDocument(token, documentId)
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun getVehicleHistory(
        id: String,
        page: Int,
        perPage: Int
    ): Result<VehicleHistoryDataDto> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getVehicleHistory(token, id, page, perPage)

            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(
                    ApiException(response.message ?: "Failed to get vehicle history"),
                    response.message
                )
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    /**
     * Retrieves auth token or throws AuthException.
     */
    private suspend fun requireAuthToken(): String {
        return userLocalDataSource.getAuthToken()
            ?: throw AuthException.unauthenticated()
    }
}

