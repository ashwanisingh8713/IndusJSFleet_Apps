package com.indusjs.fleet.data.datasource.vehicle

import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.data.datasource.RemoteDataSource
import com.indusjs.fleet.data.model.vehicle.CreateVehicleRequest
import com.indusjs.fleet.data.model.vehicle.CreateVehicleWithDocumentsRequest
import com.indusjs.fleet.data.model.vehicle.UpdateVehicleRequest
import com.indusjs.fleet.data.model.vehicle.VehicleApiResponse
import com.indusjs.fleet.data.model.vehicle.VehicleDetailDto
import com.indusjs.fleet.data.model.vehicle.VehicleDocumentDto
import com.indusjs.fleet.data.model.vehicle.VehicleDocumentsDetailDto
import com.indusjs.fleet.data.model.vehicle.VehicleDto
import com.indusjs.fleet.data.model.vehicle.VehicleRouteDto
import com.indusjs.fleet.data.model.vehicle.VehicleTripDto
import com.indusjs.fleet.data.model.vehicle.VehicleTripsDto
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readBytes
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

/**
 * Interface for remote vehicle data operations.
 */
interface VehicleRemoteDataSource : RemoteDataSource {
    suspend fun getVehicles(token: String): VehicleApiResponse<List<VehicleDto>>
    suspend fun getVehicleById(token: String, id: String): VehicleApiResponse<VehicleDto>
    suspend fun createVehicle(token: String, request: CreateVehicleRequest): VehicleApiResponse<VehicleDto>
    suspend fun createVehicleWithDocuments(token: String, request: CreateVehicleWithDocumentsRequest): VehicleApiResponse<VehicleDto>
    suspend fun updateVehicle(token: String, id: String, request: UpdateVehicleRequest): VehicleApiResponse<VehicleDto>
    suspend fun deleteVehicle(token: String, id: String): VehicleApiResponse<Unit>

    // Vehicle Detail APIs (new endpoints - may not be implemented in backend yet)
    suspend fun getVehicleDetail(token: String, id: String): VehicleApiResponse<VehicleDetailDto>
    suspend fun getVehicleTrips(token: String, id: String, page: Int, perPage: Int, state: String?): VehicleApiResponse<VehicleTripsDto>
    suspend fun getVehicleRoute(token: String, id: String): VehicleApiResponse<VehicleRouteDto>
    suspend fun getVehicleDocumentsDetail(token: String, id: String): VehicleApiResponse<VehicleDocumentsDetailDto>

    // Existing backend APIs (fallback)
    suspend fun getVehicleDocuments(token: String, id: String): VehicleApiResponse<List<VehicleDocumentDto>>
    suspend fun getTripsByVehicleId(token: String, vehicleId: String, page: Int, perPage: Int, state: String?): VehicleApiResponse<List<VehicleTripDto>>

    // Document Upload
    suspend fun uploadDocument(
        token: String,
        vehicleId: String,
        documentType: String,
        documentName: String,
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String,
        documentNumber: String? = null,
        expiryDate: String? = null
    ): VehicleApiResponse<VehicleDocumentDto>

    // Document Download
    suspend fun downloadDocument(token: String, documentId: String): Result<ByteArray>
}

/**
 * Implementation of VehicleRemoteDataSource using Ktor.
 */
@Inject
class VehicleRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : VehicleRemoteDataSource {

    private val baseUrl = "${ApiConfig.BASE_URL}/vehicles"
    private val log = Logger.withTag("VehicleRemoteDataSource")

    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    override suspend fun getVehicles(token: String): VehicleApiResponse<List<VehicleDto>> {
        return try {
            log.d { "Fetching vehicles" }
            val response: HttpResponse = httpClient.get(baseUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseListResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch vehicles: ${e.message}" }
            VehicleApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    override suspend fun getVehicleById(token: String, id: String): VehicleApiResponse<VehicleDto> {
        return try {
            log.d { "Fetching vehicle: $id" }
            val response: HttpResponse = httpClient.get("$baseUrl/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch vehicle: ${e.message}" }
            VehicleApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    override suspend fun createVehicle(token: String, request: CreateVehicleRequest): VehicleApiResponse<VehicleDto> {
        return try {
            log.d { "Creating vehicle: ${request.registrationNumber}" }
            val response: HttpResponse = httpClient.post(baseUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to create vehicle: ${e.message}" }
            VehicleApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    override suspend fun createVehicleWithDocuments(
        token: String,
        request: CreateVehicleWithDocumentsRequest
    ): VehicleApiResponse<VehicleDto> {
        return try {
            log.d { "Creating vehicle with documents: ${request.registrationNumber}" }

            val response: HttpResponse = httpClient.submitFormWithBinaryData(
                url = "$baseUrl/with-documents",
                formData = formData {
                    // Vehicle details (required fields)
                    append("registration_number", request.registrationNumber)
                    append("make", request.make)
                    append("model", request.model)
                    append("year", request.year.toString())

                    // Vehicle details (optional fields)
                    append("vehicle_type", request.vehicleType)
                    append("fuel_type", request.fuelType)
                    append("capacity", request.capacity.toString())
                    append("color", request.color)

                    // Document files (optional)
                    request.registrationCertificate?.let { doc ->
                        append("registration_certificate", doc.fileBytes, Headers.build {
                            append(HttpHeaders.ContentType, doc.mimeType)
                            append(HttpHeaders.ContentDisposition, "filename=\"${doc.fileName}\"")
                        })
                        doc.expiryDate?.let { append("registration_certificate_expiry", it) }
                    }

                    request.insurance?.let { doc ->
                        append("insurance", doc.fileBytes, Headers.build {
                            append(HttpHeaders.ContentType, doc.mimeType)
                            append(HttpHeaders.ContentDisposition, "filename=\"${doc.fileName}\"")
                        })
                        doc.expiryDate?.let { append("insurance_expiry", it) }
                    }

                    request.pucCertificate?.let { doc ->
                        append("puc_certificate", doc.fileBytes, Headers.build {
                            append(HttpHeaders.ContentType, doc.mimeType)
                            append(HttpHeaders.ContentDisposition, "filename=\"${doc.fileName}\"")
                        })
                        doc.expiryDate?.let { append("puc_certificate_expiry", it) }
                    }

                    request.fitnessCertificate?.let { doc ->
                        append("fitness_certificate", doc.fileBytes, Headers.build {
                            append(HttpHeaders.ContentType, doc.mimeType)
                            append(HttpHeaders.ContentDisposition, "filename=\"${doc.fileName}\"")
                        })
                        doc.expiryDate?.let { append("fitness_certificate_expiry", it) }
                    }

                    request.roadTax?.let { doc ->
                        append("road_tax", doc.fileBytes, Headers.build {
                            append(HttpHeaders.ContentType, doc.mimeType)
                            append(HttpHeaders.ContentDisposition, "filename=\"${doc.fileName}\"")
                        })
                        doc.expiryDate?.let { append("road_tax_expiry", it) }
                    }

                    request.permit?.let { doc ->
                        append("permit", doc.fileBytes, Headers.build {
                            append(HttpHeaders.ContentType, doc.mimeType)
                            append(HttpHeaders.ContentDisposition, "filename=\"${doc.fileName}\"")
                        })
                        doc.expiryDate?.let { append("permit_expiry", it) }
                    }
                }
            ) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            parseSingleResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to create vehicle with documents: ${e.message}" }
            VehicleApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    override suspend fun updateVehicle(token: String, id: String, request: UpdateVehicleRequest): VehicleApiResponse<VehicleDto> {
        return try {
            log.d { "Updating vehicle: $id with request: $request" }
            val response: HttpResponse = httpClient.put("$baseUrl/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to update vehicle: ${e.message}" }
            VehicleApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    override suspend fun deleteVehicle(token: String, id: String): VehicleApiResponse<Unit> {
        return try {
            log.d { "Deleting vehicle: $id" }
            val response: HttpResponse = httpClient.delete("$baseUrl/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.isSuccess()) {
                VehicleApiResponse(success = true, message = "Vehicle deleted successfully")
            } else {
                VehicleApiResponse(success = false, message = "Failed to delete vehicle")
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to delete vehicle: ${e.message}" }
            VehicleApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    // ==================== Vehicle Detail APIs ====================

    override suspend fun getVehicleDetail(token: String, id: String): VehicleApiResponse<VehicleDetailDto> {
        return try {
            log.d { "Fetching vehicle detail: $id" }
            val response: HttpResponse = httpClient.get("$baseUrl/$id/detail") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch vehicle detail: ${e.message}" }
            VehicleApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    override suspend fun getVehicleTrips(
        token: String,
        id: String,
        page: Int,
        perPage: Int,
        state: String?
    ): VehicleApiResponse<VehicleTripsDto> {
        return try {
            log.d { "Fetching vehicle trips: $id, page=$page" }
            val response: HttpResponse = httpClient.get("$baseUrl/$id/trips") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("page", page)
                parameter("per_page", perPage)
                state?.let { parameter("state", it) }
            }
            parseResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch vehicle trips: ${e.message}" }
            VehicleApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    override suspend fun getVehicleRoute(token: String, id: String): VehicleApiResponse<VehicleRouteDto> {
        return try {
            log.d { "Fetching vehicle route: $id" }
            val response: HttpResponse = httpClient.get("$baseUrl/$id/route") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch vehicle route: ${e.message}" }
            VehicleApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    override suspend fun getVehicleDocumentsDetail(token: String, id: String): VehicleApiResponse<VehicleDocumentsDetailDto> {
        return try {
            log.d { "Fetching vehicle documents detail: $id" }
            val response: HttpResponse = httpClient.get("$baseUrl/$id/documents/detail") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch vehicle documents: ${e.message}" }
            VehicleApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    private suspend inline fun <reified T> parseResponse(response: HttpResponse): VehicleApiResponse<T> {
        val bodyText = response.bodyAsText()
        log.d { "API Response: $bodyText" }
        return if (response.status.isSuccess()) {
            try {
                json.decodeFromString<VehicleApiResponse<T>>(bodyText)
            } catch (e: Exception) {
                log.e(e) { "Failed to parse response: $bodyText" }
                VehicleApiResponse(success = false, message = "Failed to parse response: ${e.message}")
            }
        } else {
            log.e { "Request failed with status: ${response.status}, body: $bodyText" }
            VehicleApiResponse(success = false, message = "Request failed with status: ${response.status}")
        }
    }

    private suspend fun parseSingleResponse(response: HttpResponse): VehicleApiResponse<VehicleDto> {
        val bodyText = response.bodyAsText()
        log.d { "API Response: $bodyText" }
        return if (response.status.isSuccess()) {
            try {
                val apiResponse = json.decodeFromString<VehicleApiResponse<VehicleDto>>(bodyText)
                // Handle both 'data' and 'vehicle' fields
                val vehicleData = apiResponse.data ?: apiResponse.vehicle
                apiResponse.copy(data = vehicleData)
            } catch (e: Exception) {
                log.e(e) { "Failed to parse response: $bodyText" }
                VehicleApiResponse(success = false, message = "Failed to parse response: ${e.message}")
            }
        } else {
            log.e { "Request failed with status: ${response.status}, body: $bodyText" }
            VehicleApiResponse(success = false, message = "Request failed with status: ${response.status}")
        }
    }

    private suspend fun parseListResponse(response: HttpResponse): VehicleApiResponse<List<VehicleDto>> {
        val bodyText = response.bodyAsText()
        log.d { "API Response: $bodyText" }
        return if (response.status.isSuccess()) {
            try {
                val apiResponse = json.decodeFromString<VehicleApiResponse<List<VehicleDto>>>(bodyText)
                // Handle both 'data' and 'vehicles' fields
                val vehiclesList = apiResponse.data ?: apiResponse.vehicles ?: emptyList()
                VehicleApiResponse(
                    success = apiResponse.success,
                    message = apiResponse.message,
                    data = vehiclesList
                )
            } catch (e: Exception) {
                log.e(e) { "Failed to parse response: $bodyText" }
                VehicleApiResponse(success = false, message = "Failed to parse response: ${e.message}")
            }
        } else {
            log.e { "Request failed with status: ${response.status}, body: $bodyText" }
            VehicleApiResponse(success = false, message = "Request failed with status: ${response.status}")
        }
    }

    // ==================== Existing Backend APIs (Fallback) ====================

    override suspend fun getVehicleDocuments(token: String, id: String): VehicleApiResponse<List<VehicleDocumentDto>> {
        return try {
            log.d { "Fetching vehicle documents: $id" }
            val response: HttpResponse = httpClient.get("$baseUrl/$id/documents") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseDocumentsResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch vehicle documents: ${e.message}" }
            VehicleApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    override suspend fun getTripsByVehicleId(
        token: String,
        vehicleId: String,
        page: Int,
        perPage: Int,
        state: String?
    ): VehicleApiResponse<List<VehicleTripDto>> {
        return try {
            log.d { "Fetching trips for vehicle: $vehicleId, page=$page" }
            val tripsUrl = "${ApiConfig.BASE_URL}/trips"
            val response: HttpResponse = httpClient.get(tripsUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("vehicle_id", vehicleId)
                parameter("page", page)
                parameter("per_page", perPage)
                state?.let { parameter("state", it) }
            }
            parseTripsResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch trips for vehicle: ${e.message}" }
            VehicleApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    private suspend fun parseDocumentsResponse(response: HttpResponse): VehicleApiResponse<List<VehicleDocumentDto>> {
        val bodyText = response.bodyAsText()
        log.d { "Documents API Response: $bodyText" }
        return if (response.status.isSuccess()) {
            try {
                json.decodeFromString<VehicleApiResponse<List<VehicleDocumentDto>>>(bodyText)
            } catch (e: Exception) {
                log.e(e) { "Failed to parse documents response: $bodyText" }
                VehicleApiResponse(success = false, message = "Failed to parse response: ${e.message}")
            }
        } else {
            log.e { "Request failed with status: ${response.status}, body: $bodyText" }
            VehicleApiResponse(success = false, message = ApiErrorHandler.extractErrorMessage(response.status, bodyText))
        }
    }

    private suspend fun parseTripsResponse(response: HttpResponse): VehicleApiResponse<List<VehicleTripDto>> {
        val bodyText = response.bodyAsText()
        log.d { "Trips API Response: $bodyText" }
        return if (response.status.isSuccess()) {
            try {
                json.decodeFromString<VehicleApiResponse<List<VehicleTripDto>>>(bodyText)
            } catch (e: Exception) {
                log.e(e) { "Failed to parse trips response: $bodyText" }
                VehicleApiResponse(success = false, message = "Failed to parse response: ${e.message}")
            }
        } else {
            log.e { "Request failed with status: ${response.status}, body: $bodyText" }
            VehicleApiResponse(success = false, message = ApiErrorHandler.extractErrorMessage(response.status, bodyText))
        }
    }

    // ==================== Document Upload ====================

    override suspend fun uploadDocument(
        token: String,
        vehicleId: String,
        documentType: String,
        documentName: String,
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String,
        documentNumber: String?,
        expiryDate: String?
    ): VehicleApiResponse<VehicleDocumentDto> {
        return try {
            log.d { "Uploading document for vehicle: $vehicleId, type: $documentType" }
            val response: HttpResponse = httpClient.submitFormWithBinaryData(
                url = "$baseUrl/$vehicleId/documents",
                formData = formData {
                    append("file", fileBytes, Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                    append("document_type", documentType)
                    append("document_name", documentName)
                    documentNumber?.let { append("document_number", it) }
                    expiryDate?.let { append("expiry_date", it) }
                }
            ) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseUploadResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to upload document: ${e.message}" }
            VehicleApiResponse(success = false, message = e.message ?: "Upload failed")
        }
    }

    private suspend fun parseUploadResponse(response: HttpResponse): VehicleApiResponse<VehicleDocumentDto> {
        val bodyText = response.bodyAsText()
        log.d { "Upload API Response: $bodyText" }
        return if (response.status.isSuccess()) {
            try {
                json.decodeFromString<VehicleApiResponse<VehicleDocumentDto>>(bodyText)
            } catch (e: Exception) {
                log.e(e) { "Failed to parse upload response: $bodyText" }
                VehicleApiResponse(success = false, message = "Failed to parse response: ${e.message}")
            }
        } else {
            log.e { "Upload failed with status: ${response.status}, body: $bodyText" }
            VehicleApiResponse(success = false, message = "Upload failed with status: ${response.status}")
        }
    }

    override suspend fun downloadDocument(token: String, documentId: String): Result<ByteArray> {
        return try {
            log.d { "Downloading document: $documentId" }
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}/documents/$documentId/download") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.isSuccess()) {
                @Suppress("DEPRECATION")
                val bytes = response.readBytes()
                log.d { "Downloaded document: ${bytes.size} bytes" }
                Result.Success(bytes)
            } else {
                val errorBody = response.bodyAsText()
                log.e { "Download failed with status: ${response.status}, body: $errorBody" }
                Result.Error(Exception("Download failed: ${response.status}"), "Download failed: ${response.status}")
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to download document: ${e.message}" }
            Result.Error(e, e.message ?: "Download failed")
        }
    }
}

