package com.indusjs.fleet.data.datasource.vehicle

import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.data.datasource.RemoteDataSource
import com.indusjs.fleet.data.model.vehicle.CreateVehicleRequest
import com.indusjs.fleet.data.model.vehicle.CreateVehicleWithDocumentsRequest
import com.indusjs.fleet.data.model.vehicle.VehicleApiResponse
import com.indusjs.fleet.data.model.vehicle.VehicleDto
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
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
    suspend fun updateVehicle(token: String, id: String, request: CreateVehicleRequest): VehicleApiResponse<VehicleDto>
    suspend fun deleteVehicle(token: String, id: String): VehicleApiResponse<Unit>
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
            VehicleApiResponse(success = false, message = e.message ?: "Network error occurred")
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
            VehicleApiResponse(success = false, message = e.message ?: "Network error occurred")
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
            VehicleApiResponse(success = false, message = e.message ?: "Network error occurred")
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
            VehicleApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun updateVehicle(token: String, id: String, request: CreateVehicleRequest): VehicleApiResponse<VehicleDto> {
        return try {
            log.d { "Updating vehicle: $id" }
            val response: HttpResponse = httpClient.put("$baseUrl/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to update vehicle: ${e.message}" }
            VehicleApiResponse(success = false, message = e.message ?: "Network error occurred")
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
            VehicleApiResponse(success = false, message = e.message ?: "Network error occurred")
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
}

