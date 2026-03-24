package com.ijs.customer.data.datasource

import co.touchlab.kermit.Logger
import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.data.datasource.RemoteDataSource
import com.ijs.customer.data.model.*
import dev.zacsweers.metro.Inject
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

/**
 * Remote data source for Customer API operations.
 */
@Inject
class CustomerRemoteDataSource(
    private val httpClient: HttpClient,
    private val json: Json
) : RemoteDataSource {

    private val log = Logger.withTag("CustomerRemoteDataSource")

    /**
     * Get all customers with pagination.
     * GET /customers
     */
    suspend fun getCustomers(
        token: String,
        page: Int = 1,
        perPage: Int = 20
    ): CustomerListResponse {
        log.d { "Fetching customers, page: $page" }

        val response = httpClient.get("${ApiConfig.BASE_URL}/customers") {
            header("Authorization", "Bearer $token")
            parameter("page", page)
            parameter("per_page", perPage)
        }

        val responseText = response.bodyAsText()
        log.d { "Response: $responseText" }

        return json.decodeFromString(CustomerListResponse.serializer(), responseText)
    }

    /**
     * Get a single customer by ID.
     * GET /customers/{id}
     */
    suspend fun getCustomer(token: String, customerId: Int): CustomerResponse {
        log.d { "Fetching customer: $customerId" }

        val response = httpClient.get("${ApiConfig.BASE_URL}/customers/$customerId") {
            header("Authorization", "Bearer $token")
        }

        val responseText = response.bodyAsText()
        log.d { "Response: $responseText" }

        return json.decodeFromString(CustomerResponse.serializer(), responseText)
    }

    /**
     * Create a new customer.
     * POST /customers
     */
    suspend fun createCustomer(token: String, request: CreateCustomerRequest): CustomerResponse {
        log.d { "Creating customer: ${request.companyName}" }

        val response = httpClient.post("${ApiConfig.BASE_URL}/customers") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(CreateCustomerRequest.serializer(), request))
        }

        val responseText = response.bodyAsText()
        log.d { "Response: $responseText" }

        return json.decodeFromString(CustomerResponse.serializer(), responseText)
    }

    /**
     * Update an existing customer.
     * PUT /customers/{id}
     */
    suspend fun updateCustomer(
        token: String,
        customerId: Int,
        request: UpdateCustomerRequest
    ): CustomerResponse {
        log.d { "Updating customer: $customerId" }

        val response = httpClient.put("${ApiConfig.BASE_URL}/customers/$customerId") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(UpdateCustomerRequest.serializer(), request))
        }

        val responseText = response.bodyAsText()
        log.d { "Response: $responseText" }

        return json.decodeFromString(CustomerResponse.serializer(), responseText)
    }

    /**
     * Toggle customer active status.
     * PATCH /customers/{id}/toggle-status
     */
    suspend fun toggleCustomerStatus(token: String, customerId: Int): CustomerResponse {
        log.d { "Toggling customer status: $customerId" }

        val response = httpClient.patch("${ApiConfig.BASE_URL}/customers/$customerId/toggle-status") {
            header("Authorization", "Bearer $token")
        }

        val responseText = response.bodyAsText()
        log.d { "Response: $responseText" }

        return json.decodeFromString(CustomerResponse.serializer(), responseText)
    }

    /**
     * Get customer statistics.
     * GET /customers/{id}/statistics
     * Only available to Owner and General Manager.
     */
    suspend fun getCustomerStatistics(
        token: String,
        customerId: Int,
        startDate: String? = null,
        endDate: String? = null
    ): CustomerStatisticsResponse {
        log.d { "Fetching customer statistics: $customerId" }

        val response = httpClient.get("${ApiConfig.BASE_URL}/customers/$customerId/statistics") {
            header("Authorization", "Bearer $token")
            startDate?.let { parameter("start_date", it) }
            endDate?.let { parameter("end_date", it) }
        }

        val responseText = response.bodyAsText()
        log.d { "Response: $responseText" }

        return json.decodeFromString(CustomerStatisticsResponse.serializer(), responseText)
    }

    // ============= Customer Trips API =============

    /**
     * Get trips for a customer with pagination and optional state filter.
     * GET /customers/{id}/trips
     */
    suspend fun getCustomerTrips(
        token: String,
        customerId: Int,
        page: Int = 1,
        perPage: Int = 20,
        state: String? = null
    ): CustomerTripsResponse {
        log.d { "Fetching customer trips: $customerId, page: $page, state: $state" }

        val response = httpClient.get("${ApiConfig.BASE_URL}/customers/$customerId/trips") {
            header("Authorization", "Bearer $token")
            parameter("page", page)
            parameter("per_page", perPage)
            state?.let { parameter("state", it) }
        }

        val responseText = response.bodyAsText()
        log.d { "Response: ${responseText.take(500)}..." }

        return json.decodeFromString(CustomerTripsResponse.serializer(), responseText)
    }

    // ============= Customer Pending Payments API =============

    /**
     * Get pending payments for a customer.
     * GET /customers/{id}/pending-payments
     * Only available to Owner and General Manager.
     */
    suspend fun getCustomerPendingPayments(
        token: String,
        customerId: Int,
        page: Int = 1,
        perPage: Int = 20
    ): CustomerPendingPaymentsResponse {
        log.d { "Fetching customer pending payments: $customerId, page: $page" }

        val response = httpClient.get("${ApiConfig.BASE_URL}/customers/$customerId/pending-payments") {
            header("Authorization", "Bearer $token")
            parameter("page", page)
            parameter("per_page", perPage)
        }

        val responseText = response.bodyAsText()
        log.d { "Response: ${responseText.take(500)}..." }

        return json.decodeFromString(CustomerPendingPaymentsResponse.serializer(), responseText)
    }

    // ============= Customer Payments API =============

    /**
     * Get payments received from a customer.
     * GET /customers/{id}/payments
     * Only available to Owner and General Manager.
     */
    suspend fun getCustomerPayments(
        token: String,
        customerId: Int,
        page: Int = 1,
        perPage: Int = 20,
        status: String? = null,
        mode: String? = null
    ): CustomerPaymentsResponse {
        log.d { "Fetching customer payments: $customerId, page: $page, mode: $mode" }

        val response = httpClient.get("${ApiConfig.BASE_URL}/customers/$customerId/payments") {
            header("Authorization", "Bearer $token")
            parameter("page", page)
            parameter("per_page", perPage)
            status?.let { parameter("status", it) }
            mode?.let { parameter("mode", it) }
        }

        val responseText = response.bodyAsText()
        log.d { "Response: ${responseText.take(500)}..." }

        return json.decodeFromString(CustomerPaymentsResponse.serializer(), responseText)
    }

    // ============= Customer Payment Summary API =============

    /**
     * Get payment summary for a customer (breakdown by mode/month).
     * GET /customers/{id}/payment-summary
     * Only available to Owner and General Manager.
     */
    suspend fun getCustomerPaymentSummary(
        token: String,
        customerId: Int,
        startDate: String? = null,
        endDate: String? = null
    ): CustomerPaymentSummaryResponse {
        log.d { "Fetching customer payment summary: $customerId" }

        val response = httpClient.get("${ApiConfig.BASE_URL}/customers/$customerId/payment-summary") {
            header("Authorization", "Bearer $token")
            startDate?.let { parameter("start_date", it) }
            endDate?.let { parameter("end_date", it) }
        }

        val responseText = response.bodyAsText()
        log.d { "Response: $responseText" }

        return json.decodeFromString(CustomerPaymentSummaryResponse.serializer(), responseText)
    }

    // ============= Customer Financial Report API =============

    /**
     * Get comprehensive P&L report for a customer.
     * GET /customers/{id}/financial-report
     * Only available to Owner and General Manager.
     */
    suspend fun getCustomerFinancialReport(
        token: String,
        customerId: Int,
        period: String = "monthly",
        startDate: String? = null,
        endDate: String? = null
    ): CustomerFinancialReportResponse {
        log.d { "Fetching customer financial report: $customerId, period: $period" }

        val response = httpClient.get("${ApiConfig.BASE_URL}/customers/$customerId/financial-report") {
            header("Authorization", "Bearer $token")
            parameter("period", period)
            startDate?.let { parameter("start_date", it) }
            endDate?.let { parameter("end_date", it) }
        }

        val responseText = response.bodyAsText()
        log.d { "Response: ${responseText.take(500)}..." }

        return json.decodeFromString(CustomerFinancialReportResponse.serializer(), responseText)
    }
}
