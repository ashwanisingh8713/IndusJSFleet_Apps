package com.indusjs.fleet.domain.repository.customer

import com.indusjs.fleet.domain.entity.customer.*
import com.indusjs.fleet.domain.repository.Repository
import com.indusjs.error.result.Result

/**
 * Repository interface for Customer operations.
 *
 * Permissions:
 * - Create/Edit: Owner, General Manager, Manager
 * - View: All roles
 * - Statistics/Financials: Owner, General Manager only
 */
interface CustomerRepository : Repository {

    /**
     * Get all customers with pagination.
     */
    suspend fun getCustomers(
        page: Int = 1,
        perPage: Int = 20,
        searchQuery: String? = null,
        isActive: Boolean? = null
    ): Result<List<Customer>>

    /**
     * Get a single customer by ID.
     */
    suspend fun getCustomer(customerId: String): Result<Customer>

    /**
     * Create a new customer.
     */
    suspend fun createCustomer(
        companyName: String,
        personName: String,
        primaryContact: String,
        secondaryContact: String? = null,
        companyAddress: String? = null,
        email: String? = null,
        gstNumber: String? = null,
        notes: String? = null
    ): Result<Customer>

    /**
     * Update an existing customer.
     */
    suspend fun updateCustomer(
        customerId: String,
        companyName: String? = null,
        personName: String? = null,
        primaryContact: String? = null,
        secondaryContact: String? = null,
        companyAddress: String? = null,
        email: String? = null,
        gstNumber: String? = null,
        notes: String? = null
    ): Result<Customer>

    /**
     * Toggle customer active status.
     */
    suspend fun toggleCustomerStatus(customerId: String): Result<Customer>

    /**
     * Get customer statistics.
     * Only available to Owner and General Manager.
     */
    suspend fun getCustomerStatistics(
        customerId: String,
        startDate: String? = null,
        endDate: String? = null
    ): Result<CustomerStatistics>

    /**
     * Get all customers as summary list for selection.
     * Used in dropdowns and autocomplete.
     */
    suspend fun getCustomerSummaries(
        searchQuery: String? = null,
        activeOnly: Boolean = true
    ): Result<List<CustomerSummary>>

    /**
     * Get customers from local cache.
     */
    suspend fun getLocalCustomers(): List<CustomerSummary>

    /**
     * Refresh customers from remote and update local cache.
     */
    suspend fun refreshCustomers(): Result<List<Customer>>

    // ============= Customer Trips =============

    /**
     * Get trips for a customer with pagination.
     * All roles can access.
     */
    suspend fun getCustomerTrips(
        customerId: String,
        page: Int = 1,
        perPage: Int = 20,
        state: String? = null
    ): Result<CustomerTripsResult>

    // ============= Customer Pending Payments =============

    /**
     * Get pending payments for a customer.
     * Only available to Owner and General Manager.
     */
    suspend fun getCustomerPendingPayments(
        customerId: String,
        page: Int = 1,
        perPage: Int = 20
    ): Result<CustomerPendingPaymentsResult>

    // ============= Customer Payments =============

    /**
     * Get payments received from a customer.
     * Only available to Owner and General Manager.
     */
    suspend fun getCustomerPayments(
        customerId: String,
        page: Int = 1,
        perPage: Int = 20,
        status: String? = null,
        mode: String? = null
    ): Result<CustomerPaymentsResult>

    // ============= Customer Payment Summary =============

    /**
     * Get payment summary for a customer.
     * Only available to Owner and General Manager.
     */
    suspend fun getCustomerPaymentSummary(
        customerId: String,
        startDate: String? = null,
        endDate: String? = null
    ): Result<CustomerPaymentSummary>

    // ============= Customer Financial Report =============

    /**
     * Get comprehensive P&L report for a customer.
     * Only available to Owner and General Manager.
     */
    suspend fun getCustomerFinancialReport(
        customerId: String,
        period: String = "monthly",
        startDate: String? = null,
        endDate: String? = null
    ): Result<CustomerFinancialReport>
}

/**
 * Result wrapper for customer trips with pagination info.
 */
data class CustomerTripsResult(
    val trips: List<CustomerTrip>,
    val summary: CustomerTripsSummary? = null,
    val page: Int = 1,
    val totalPages: Int = 1,
    val total: Int = 0,
    val hasMore: Boolean = false
)

/**
 * Result wrapper for customer pending payments with pagination info.
 */
data class CustomerPendingPaymentsResult(
    val payments: List<CustomerPendingPayment>,
    val totalPending: Double = 0.0,
    val overdueCount: Int = 0,
    val page: Int = 1,
    val totalPages: Int = 1,
    val total: Int = 0,
    val hasMore: Boolean = false
)

/**
 * Result wrapper for customer payments with pagination info.
 */
data class CustomerPaymentsResult(
    val payments: List<CustomerPayment>,
    val totalReceived: Double = 0.0,
    val page: Int = 1,
    val totalPages: Int = 1,
    val total: Int = 0,
    val hasMore: Boolean = false
)
