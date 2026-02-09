package com.indusjs.fleet.domain.repository.vehicle

import com.indusjs.error.result.Result
import com.indusjs.fleet.data.model.history.VehicleHistoryDataDto
import com.indusjs.fleet.data.model.state.StateHistoryResponseDto
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.entity.vehicle.VehicleDetail
import com.indusjs.fleet.domain.entity.vehicle.VehicleDocument
import com.indusjs.fleet.domain.entity.vehicle.VehicleDocumentsData
import com.indusjs.fleet.domain.entity.vehicle.VehicleTripsData
import com.indusjs.fleet.domain.entity.vehicle.RouteInfo
import com.indusjs.fleet.domain.repository.Repository
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Vehicle operations.
 * Defined in the domain layer to be implemented by the data layer.
 */
interface VehicleRepository : Repository {

    /**
     * Get all vehicles as a Flow.
     */
    fun getVehicles(): Flow<Result<List<Vehicle>>>

    /**
     * Get available vehicles for trip assignment.
     * Returns only vehicles with status=active that are not currently occupied.
     */
    fun getAvailableVehicles(): Flow<Result<List<Vehicle>>>

    /**
     * Get a specific vehicle by ID.
     */
    suspend fun getVehicleById(id: String): Result<Vehicle>

    /**
     * Create a new vehicle.
     */
    suspend fun createVehicle(vehicle: Vehicle): Result<Vehicle>

    /**
     * Create a new vehicle with optional documents.
     * Uses multipart form upload to /vehicles/with-documents endpoint.
     */
    suspend fun createVehicleWithDocuments(
        vehicle: Vehicle,
        documents: List<VehicleDocument>
    ): Result<Vehicle>

    /**
     * Update an existing vehicle.
     */
    suspend fun updateVehicle(vehicle: Vehicle): Result<Vehicle>

    /**
     * Delete a vehicle by ID.
     */
    suspend fun deleteVehicle(id: String): Result<Unit>

    // ==================== State Management APIs ====================

    /**
     * Update vehicle state.
     * PATCH /vehicles/{id}/state
     *
     * @param id Vehicle ID
     * @param newState New state value (from StatusConstants.VehicleState)
     * @param reason Optional reason for state change
     * @param notes Optional notes for state change
     */
    suspend fun updateVehicleState(
        id: String,
        newState: String,
        reason: String? = null,
        notes: String? = null
    ): Result<Vehicle>

    /**
     * Get vehicle state change history.
     * GET /vehicles/{id}/state-history
     */
    suspend fun getVehicleStateHistory(
        id: String,
        page: Int = 1,
        perPage: Int = 20
    ): Result<StateHistoryResponseDto>

    // ==================== Vehicle Detail APIs ====================

    /**
     * Get complete vehicle detail for all tabs.
     * GET /vehicles/{id}/detail
     */
    suspend fun getVehicleDetail(id: String): Result<VehicleDetail>

    /**
     * Get vehicle trips with pagination.
     * GET /vehicles/{id}/trips?page=1&per_page=10&state=completed
     */
    suspend fun getVehicleTrips(
        id: String,
        page: Int = 1,
        perPage: Int = 10,
        state: String? = null
    ): Result<VehicleTripsData>

    /**
     * Get vehicle route and stops for active trip.
     * GET /vehicles/{id}/route
     */
    suspend fun getVehicleRoute(id: String): Result<RouteInfo>

    /**
     * Get vehicle documents detail.
     * GET /vehicles/{id}/documents/detail
     */
    suspend fun getVehicleDocumentsDetail(id: String): Result<VehicleDocumentsData>

    /**
     * Upload a document for a vehicle.
     * POST /vehicles/{id}/documents
     */
    suspend fun uploadDocument(
        vehicleId: String,
        documentType: String,
        documentName: String,
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String,
        documentNumber: String? = null,
        expiryDate: String? = null
    ): Result<Unit>

    /**
     * Download a document file.
     * GET /documents/{id}/download
     */
    suspend fun downloadDocument(documentId: String): Result<ByteArray>

    // ==================== History APIs ====================

    /**
     * Get vehicle history with pagination.
     * GET /vehicles/{id}/history?page=1&per_page=20
     */
    suspend fun getVehicleHistory(
        id: String,
        page: Int = 1,
        perPage: Int = 20
    ): Result<VehicleHistoryDataDto>
}

