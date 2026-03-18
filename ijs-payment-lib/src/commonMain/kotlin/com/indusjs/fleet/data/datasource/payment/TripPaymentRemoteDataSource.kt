package com.indusjs.fleet.data.datasource.payment

import co.touchlab.kermit.Logger
import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.data.model.payment.*
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

/**
 * Remote data source for trip payment operations.
 */
@Inject
class TripPaymentRemoteDataSource(
    private val httpClient: HttpClient,
    private val json: Json
) {
    private val log = Logger.withTag("TripPaymentRemoteDataSource")

    // ==================== Create Payment ====================

    /**
     * Create a new trip payment.
     * POST /trip-payments
     */
    suspend fun createPayment(
        token: String,
        request: CreateTripPaymentRequest
    ): TripPaymentResponse {
        log.d { "Creating trip payment for trip: ${request.tripId}" }

        val response = httpClient.post("${ApiConfig.BASE_URL}/trip-payments") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        val responseText = response.bodyAsText()
        log.d { "Response: ${responseText.take(500)}" }

        // Check HTTP status before parsing
        if (!response.status.isSuccess()) {
            log.e { "Failed to create payment: ${response.status}" }
            return TripPaymentResponse(
                success = false,
                message = "Failed to create payment: ${response.status.value}"
            )
        }

        return try {
            json.decodeFromString(TripPaymentResponse.serializer(), responseText)
        } catch (e: Exception) {
            log.e(e) { "Error parsing create payment response" }
            TripPaymentResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    /**
     * Add payment to a specific trip.
     * POST /trip-payments
     */
    suspend fun addPaymentToTrip(
        token: String,
        tripId: Int,
        request: AddPaymentToTripRequest
    ): TripPaymentResponse {
        log.d { "Adding payment to trip: $tripId" }

        val response = httpClient.post("${ApiConfig.BASE_URL}/trip-payments") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        val responseText = response.bodyAsText()
        log.d { "Response: ${responseText.take(500)}" }

        // Check HTTP status before parsing
        if (!response.status.isSuccess()) {
            log.e { "Failed to add payment to trip: ${response.status}" }
            return TripPaymentResponse(
                success = false,
                message = "Failed to add payment: ${response.status.value}"
            )
        }

        return try {
            json.decodeFromString(TripPaymentResponse.serializer(), responseText)
        } catch (e: Exception) {
            log.e(e) { "Error parsing add payment response" }
            TripPaymentResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    // ==================== List Payments ====================

    /**
     * List all trip payments with filters.
     * GET /trip-payments
     */
    suspend fun listPayments(
        token: String,
        page: Int = 1,
        perPage: Int = 20,
        tripId: Int? = null,
        customerId: Int? = null,
        paymentType: String? = null,
        paymentMode: String? = null,
        paymentStatus: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): TripPaymentListResponse {
        log.d { "Fetching trip payments: page=$page, tripId=$tripId, customerId=$customerId" }

        val response = httpClient.get("${ApiConfig.BASE_URL}/trip-payments") {
            header("Authorization", "Bearer $token")
            parameter("page", page)
            parameter("per_page", perPage)
            tripId?.let { parameter("trip_id", it) }
            customerId?.let { parameter("customer_id", it) }
            paymentType?.let { parameter("payment_type", it) }
            paymentMode?.let { parameter("payment_mode", it) }
            paymentStatus?.let { parameter("payment_status", it) }
            startDate?.let { parameter("start_date", it) }
            endDate?.let { parameter("end_date", it) }
        }

        val responseText = response.bodyAsText()
        log.d { "Response: ${responseText.take(500)}" }

        // Check HTTP status before parsing
        if (!response.status.isSuccess()) {
            log.e { "Failed to fetch payments: ${response.status}" }
            return TripPaymentListResponse(
                success = false,
                message = "Failed to fetch payments: ${response.status.value}"
            )
        }

        return try {
            json.decodeFromString(TripPaymentListResponse.serializer(), responseText)
        } catch (e: Exception) {
            log.e(e) { "Error parsing payments response" }
            TripPaymentListResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    /**
     * Get payments for a specific trip.
     * GET /trips/{trip_id}/payments
     */
    suspend fun getTripPayments(
        token: String,
        tripId: Int,
        page: Int = 1,
        perPage: Int = 20
    ): TripPaymentsHistoryResponse {
        log.d { "Fetching payments for trip: $tripId" }

        val response = httpClient.get("${ApiConfig.BASE_URL}/trips/$tripId/payments") {
            header("Authorization", "Bearer $token")
            parameter("page", page)
            parameter("per_page", perPage)
        }

        val responseText = response.bodyAsText()
        log.d { "Response: ${responseText.take(500)}" }

        // Check HTTP status before parsing
        if (!response.status.isSuccess()) {
            log.e { "Failed to fetch trip payments: ${response.status}" }
            return TripPaymentsHistoryResponse(
                success = false,
                message = "Failed to fetch trip payments: ${response.status.value}"
            )
        }

        return try {
            json.decodeFromString(TripPaymentsHistoryResponse.serializer(), responseText)
        } catch (e: Exception) {
            log.e(e) { "Error parsing trip payments response" }
            TripPaymentsHistoryResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    // ==================== Get Payment ====================

    /**
     * Get payment by ID.
     * GET /trip-payments/{id}
     */
    suspend fun getPayment(
        token: String,
        paymentId: Int
    ): TripPaymentResponse {
        log.d { "Fetching payment: $paymentId" }

        val response = httpClient.get("${ApiConfig.BASE_URL}/trip-payments/$paymentId") {
            header("Authorization", "Bearer $token")
        }

        val responseText = response.bodyAsText()
        log.d { "Response: ${responseText.take(500)}" }

        // Check HTTP status before parsing
        if (!response.status.isSuccess()) {
            log.e { "Failed to fetch payment: ${response.status}" }
            return TripPaymentResponse(
                success = false,
                message = "Failed to fetch payment: ${response.status.value}"
            )
        }

        return try {
            json.decodeFromString(TripPaymentResponse.serializer(), responseText)
        } catch (e: Exception) {
            log.e(e) { "Error parsing payment response" }
            TripPaymentResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    // ==================== Update Payment ====================

    /**
     * Update a trip payment.
     * PUT /trip-payments/{id}
     */
    suspend fun updatePayment(
        token: String,
        paymentId: Int,
        request: UpdateTripPaymentRequest
    ): TripPaymentResponse {
        log.d { "Updating payment: $paymentId" }

        val response = httpClient.put("${ApiConfig.BASE_URL}/trip-payments/$paymentId") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        val responseText = response.bodyAsText()
        log.d { "Response: ${responseText.take(500)}" }

        // Check HTTP status before parsing
        if (!response.status.isSuccess()) {
            log.e { "Failed to update payment: ${response.status}" }
            return TripPaymentResponse(
                success = false,
                message = "Failed to update payment: ${response.status.value}"
            )
        }

        return try {
            json.decodeFromString(TripPaymentResponse.serializer(), responseText)
        } catch (e: Exception) {
            log.e(e) { "Error parsing update payment response" }
            TripPaymentResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    // ==================== Delete Payment ====================

    /**
     * Delete a trip payment.
     * DELETE /trip-payments/{id}
     */
    suspend fun deletePayment(
        token: String,
        paymentId: Int
    ): TripPaymentResponse {
        log.d { "Deleting payment: $paymentId" }

        val response = httpClient.delete("${ApiConfig.BASE_URL}/trip-payments/$paymentId") {
            header("Authorization", "Bearer $token")
        }

        val responseText = response.bodyAsText()
        log.d { "Response: ${responseText.take(500)}" }

        // Check HTTP status before parsing
        if (!response.status.isSuccess()) {
            log.e { "Failed to delete payment: ${response.status}" }
            return TripPaymentResponse(
                success = false,
                message = "Failed to delete payment: ${response.status.value}"
            )
        }

        return try {
            json.decodeFromString(TripPaymentResponse.serializer(), responseText)
        } catch (e: Exception) {
            log.e(e) { "Error parsing delete payment response" }
            TripPaymentResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    // ==================== Reports ====================

    /**
     * Get payment summary report.
     * GET /trip-payments/summary
     */
    suspend fun getPaymentSummary(
        token: String,
        startDate: String? = null,
        endDate: String? = null
    ): PaymentSummaryReportResponse {
        log.d { "Fetching payment summary: $startDate to $endDate" }

        val response = httpClient.get("${ApiConfig.BASE_URL}/trip-payments/summary") {
            header("Authorization", "Bearer $token")
            startDate?.let { parameter("start_date", it) }
            endDate?.let { parameter("end_date", it) }
        }

        val responseText = response.bodyAsText()
        log.d { "Response: ${responseText.take(500)}" }

        return json.decodeFromString(PaymentSummaryReportResponse.serializer(), responseText)
    }

    /**
     * Get TDS report.
     * GET /trip-payments/tds-report
     */
    suspend fun getTdsReport(
        token: String,
        financialYear: String
    ): TdsReportResponse {
        log.d { "Fetching TDS report for FY: $financialYear" }

        val response = httpClient.get("${ApiConfig.BASE_URL}/trip-payments/tds-report") {
            header("Authorization", "Bearer $token")
            parameter("financial_year", financialYear)
        }

        val responseText = response.bodyAsText()
        log.d { "Response: ${responseText.take(500)}" }

        return json.decodeFromString(TdsReportResponse.serializer(), responseText)
    }
}
