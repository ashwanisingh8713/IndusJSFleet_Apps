package com.indusjs.fleet.data.datasource.finance

import co.touchlab.kermit.Logger
import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.data.model.finance.*
import dev.zacsweers.metro.Inject
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

/**
 * Remote data source for Vehicle Finance APIs.
 */
interface VehicleFinanceRemoteDataSource {
    // Purchase APIs
    suspend fun getPurchase(token: String, vehicleId: Int): VehiclePurchaseResponse
    suspend fun createPurchase(token: String, vehicleId: Int, request: CreatePurchaseRequest): VehiclePurchaseResponse
    suspend fun updatePurchase(token: String, vehicleId: Int, request: UpdatePurchaseRequest): VehiclePurchaseResponse

    // Loan Summary
    suspend fun getLoanSummary(token: String, vehicleId: Int): LoanSummaryResponse

    // Loan Payments
    suspend fun getLoanPayments(token: String, vehicleId: Int, page: Int, perPage: Int, status: String?): LoanPaymentsListResponse
    suspend fun getAllLoanPayments(token: String, page: Int, perPage: Int, vehicleId: Int?, status: String?): LoanPaymentsListResponse
    suspend fun getPaymentById(token: String, paymentId: Int): LoanPaymentResponse
    suspend fun recordPayment(token: String, request: RecordPaymentRequest): LoanPaymentResponse
    suspend fun updatePayment(token: String, paymentId: Int, request: UpdatePaymentRequest): LoanPaymentResponse
    suspend fun markEmiPaid(token: String, paymentId: Int, request: MarkEmiPaidRequest): LoanPaymentResponse
    suspend fun deletePayment(token: String, paymentId: Int): DeleteResponse

    // Alerts
    suspend fun getUpcomingEmis(token: String, days: Int): EmiAlertsResponse
    suspend fun getOverdueEmis(token: String): EmiAlertsResponse
}

@Inject
class VehicleFinanceRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val json: Json
) : VehicleFinanceRemoteDataSource {

    private val log = Logger.withTag("VehicleFinanceRemoteDataSource")

    // ==================== Purchase APIs ====================

    override suspend fun getPurchase(token: String, vehicleId: Int): VehiclePurchaseResponse {
        return try {
            log.d { "Fetching purchase info for vehicle: $vehicleId" }
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}/vehicles/$vehicleId/purchase") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
            }
            val body = response.bodyAsText()
            log.d { "Purchase response: ${body.take(500)}" }

            if (response.status == HttpStatusCode.NotFound) {
                return VehiclePurchaseResponse(success = true, message = "No purchase info", data = null)
            }

            json.decodeFromString<VehiclePurchaseResponse>(body)
        } catch (e: Exception) {
            log.e(e) { "Error fetching purchase: ${e.message}" }
            VehiclePurchaseResponse(success = false, message = e.message ?: "Failed to fetch purchase info")
        }
    }

    override suspend fun createPurchase(token: String, vehicleId: Int, request: CreatePurchaseRequest): VehiclePurchaseResponse {
        return try {
            log.d { "Creating purchase for vehicle: $vehicleId" }
            val response: HttpResponse = httpClient.post("${ApiConfig.BASE_URL}/vehicles/$vehicleId/purchase") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.bodyAsText()
            log.d { "Create purchase response: ${body.take(500)}" }
            json.decodeFromString<VehiclePurchaseResponse>(body)
        } catch (e: Exception) {
            log.e(e) { "Error creating purchase: ${e.message}" }
            VehiclePurchaseResponse(success = false, message = e.message ?: "Failed to create purchase")
        }
    }

    override suspend fun updatePurchase(token: String, vehicleId: Int, request: UpdatePurchaseRequest): VehiclePurchaseResponse {
        return try {
            log.d { "Updating purchase for vehicle: $vehicleId" }
            val response: HttpResponse = httpClient.put("${ApiConfig.BASE_URL}/vehicles/$vehicleId/purchase") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.bodyAsText()
            log.d { "Update purchase response: ${body.take(500)}" }
            json.decodeFromString<VehiclePurchaseResponse>(body)
        } catch (e: Exception) {
            log.e(e) { "Error updating purchase: ${e.message}" }
            VehiclePurchaseResponse(success = false, message = e.message ?: "Failed to update purchase")
        }
    }

    // ==================== Loan Summary ====================

    override suspend fun getLoanSummary(token: String, vehicleId: Int): LoanSummaryResponse {
        return try {
            log.d { "Fetching loan summary for vehicle: $vehicleId" }
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}/vehicles/$vehicleId/loan-summary") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
            }
            val body = response.bodyAsText()
            log.d { "Loan summary response: ${body.take(500)}" }
            json.decodeFromString<LoanSummaryResponse>(body)
        } catch (e: Exception) {
            log.e(e) { "Error fetching loan summary: ${e.message}" }
            LoanSummaryResponse(success = false, message = e.message ?: "Failed to fetch loan summary")
        }
    }

    // ==================== Loan Payments ====================

    override suspend fun getLoanPayments(
        token: String,
        vehicleId: Int,
        page: Int,
        perPage: Int,
        status: String?
    ): LoanPaymentsListResponse {
        return try {
            log.d { "Fetching loan payments for vehicle: $vehicleId, page: $page" }
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}/vehicles/$vehicleId/loan-payments") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
                parameter("page", page)
                parameter("per_page", perPage)
                status?.let { parameter("status", it) }
            }
            val body = response.bodyAsText()
            log.d { "Loan payments response: ${body.take(500)}" }
            json.decodeFromString<LoanPaymentsListResponse>(body)
        } catch (e: Exception) {
            log.e(e) { "Error fetching loan payments: ${e.message}" }
            LoanPaymentsListResponse(success = false, message = e.message ?: "Failed to fetch loan payments")
        }
    }

    override suspend fun getAllLoanPayments(
        token: String,
        page: Int,
        perPage: Int,
        vehicleId: Int?,
        status: String?
    ): LoanPaymentsListResponse {
        return try {
            log.d { "Fetching all loan payments, page: $page" }
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}/vehicle-loan-payments") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
                parameter("page", page)
                parameter("per_page", perPage)
                vehicleId?.let { parameter("vehicle_id", it) }
                status?.let { parameter("status", it) }
            }
            val body = response.bodyAsText()
            log.d { "All loan payments response: ${body.take(500)}" }
            json.decodeFromString<LoanPaymentsListResponse>(body)
        } catch (e: Exception) {
            log.e(e) { "Error fetching all loan payments: ${e.message}" }
            LoanPaymentsListResponse(success = false, message = e.message ?: "Failed to fetch all loan payments")
        }
    }

    override suspend fun getPaymentById(token: String, paymentId: Int): LoanPaymentResponse {
        return try {
            log.d { "Fetching payment details for payment ID: $paymentId" }
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}/vehicle-loan-payments/$paymentId") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
            }
            val body = response.bodyAsText()
            log.d { "Payment details response: ${body.take(500)}" }
            json.decodeFromString<LoanPaymentResponse>(body)
        } catch (e: Exception) {
            log.e(e) { "Error fetching payment details: ${e.message}" }
            LoanPaymentResponse(success = false, message = e.message ?: "Failed to fetch payment details")
        }
    }

    override suspend fun recordPayment(token: String, request: RecordPaymentRequest): LoanPaymentResponse {
        return try {
            log.d { "Recording payment for purchase: ${request.vehiclePurchaseId}, amount: ${request.amount}" }
            val response: HttpResponse = httpClient.post("${ApiConfig.BASE_URL}/vehicle-loan-payments") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.bodyAsText()
            log.d { "Record payment response: ${body.take(500)}" }
            json.decodeFromString<LoanPaymentResponse>(body)
        } catch (e: Exception) {
            log.e(e) { "Error recording payment: ${e.message}" }
            LoanPaymentResponse(success = false, message = e.message ?: "Failed to record payment")
        }
    }

    override suspend fun updatePayment(token: String, paymentId: Int, request: UpdatePaymentRequest): LoanPaymentResponse {
        return try {
            log.d { "Updating payment for payment ID: $paymentId" }
            val response: HttpResponse = httpClient.put("${ApiConfig.BASE_URL}/vehicle-loan-payments/$paymentId") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.bodyAsText()
            log.d { "Update payment response: ${body.take(500)}" }
            json.decodeFromString<LoanPaymentResponse>(body)
        } catch (e: Exception) {
            log.e(e) { "Error updating payment: ${e.message}" }
            LoanPaymentResponse(success = false, message = e.message ?: "Failed to update payment")
        }
    }

    override suspend fun markEmiPaid(token: String, paymentId: Int, request: MarkEmiPaidRequest): LoanPaymentResponse {
        return try {
            log.d { "Marking EMI $paymentId as paid" }
            val response: HttpResponse = httpClient.post("${ApiConfig.BASE_URL}/vehicle-loan-payments/$paymentId/pay") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.bodyAsText()
            log.d { "Mark EMI paid response: ${body.take(500)}" }
            json.decodeFromString<LoanPaymentResponse>(body)
        } catch (e: Exception) {
            log.e(e) { "Error marking EMI paid: ${e.message}" }
            LoanPaymentResponse(success = false, message = e.message ?: "Failed to mark EMI paid")
        }
    }

    override suspend fun deletePayment(token: String, paymentId: Int): DeleteResponse {
        return try {
            log.d { "Deleting payment: $paymentId" }
            val response: HttpResponse = httpClient.delete("${ApiConfig.BASE_URL}/vehicle-loan-payments/$paymentId") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
            }
            val body = response.bodyAsText()
            log.d { "Delete payment response: $body" }
            json.decodeFromString<DeleteResponse>(body)
        } catch (e: Exception) {
            log.e(e) { "Error deleting payment: ${e.message}" }
            DeleteResponse(success = false, message = e.message ?: "Failed to delete payment")
        }
    }

    // ==================== Alerts ====================

    override suspend fun getUpcomingEmis(token: String, days: Int): EmiAlertsResponse {
        return try {
            log.d { "Fetching upcoming EMIs for next $days days" }
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}/vehicle-loan-payments/upcoming") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
                parameter("days", days)
            }
            val body = response.bodyAsText()
            log.d { "Upcoming EMIs response: ${body.take(500)}" }
            json.decodeFromString<EmiAlertsResponse>(body)
        } catch (e: Exception) {
            log.e(e) { "Error fetching upcoming EMIs: ${e.message}" }
            EmiAlertsResponse(success = false, message = e.message ?: "Failed to fetch upcoming EMIs")
        }
    }

    override suspend fun getOverdueEmis(token: String): EmiAlertsResponse {
        return try {
            log.d { "Fetching overdue EMIs" }
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}/vehicle-loan-payments/overdue") {
                headers { append(HttpHeaders.Authorization, "Bearer $token") }
            }
            val body = response.bodyAsText()
            log.d { "Overdue EMIs response: ${body.take(500)}" }
            json.decodeFromString<EmiAlertsResponse>(body)
        } catch (e: Exception) {
            log.e(e) { "Error fetching overdue EMIs: ${e.message}" }
            EmiAlertsResponse(success = false, message = e.message ?: "Failed to fetch overdue EMIs")
        }
    }
}
