package com.indusjs.fleet.data.repository.customer

import co.touchlab.kermit.Logger
import com.indusjs.error.exception.ApiException
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.auth.AuthTokenHelper
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.fleet.data.datasource.customer.CustomerLocalDataSource
import com.indusjs.fleet.data.datasource.customer.CustomerRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.mapper.customer.CustomerMapper.toDomain
import com.indusjs.fleet.data.mapper.customer.CustomerMapper.toDomainList
import com.indusjs.fleet.data.mapper.customer.CustomerMapper.toSummaryList
import com.indusjs.fleet.data.mapper.customer.CustomerMapper.entityListToDomainList
import com.indusjs.fleet.data.mapper.customer.CustomerMapper.entityListToSummaryList
import com.indusjs.fleet.data.mapper.customer.CustomerMapper.toTripsDomain
import com.indusjs.fleet.data.mapper.customer.CustomerMapper.toPendingPaymentsDomain
import com.indusjs.fleet.data.mapper.customer.CustomerMapper.toPaymentsDomain
import com.indusjs.fleet.data.model.customer.CreateCustomerRequest
import com.indusjs.fleet.data.model.customer.UpdateCustomerRequest
import com.indusjs.fleet.domain.entity.customer.*
import com.indusjs.fleet.domain.repository.customer.CustomerRepository
import com.indusjs.fleet.domain.repository.customer.CustomerTripsResult
import com.indusjs.fleet.domain.repository.customer.CustomerPendingPaymentsResult
import com.indusjs.fleet.domain.repository.customer.CustomerPaymentsResult
import dev.zacsweers.metro.Inject

/**
 * Implementation of CustomerRepository.
 * Handles customer operations with offline-first caching.
 */
@Inject
class CustomerRepositoryImpl(
    private val remoteDataSource: CustomerRemoteDataSource,
    private val localDataSource: CustomerLocalDataSource,
    private val userLocalDataSource: UserLocalDataSource
) : CustomerRepository {

    private val log = Logger.withTag("CustomerRepositoryImpl")

    override suspend fun getCustomers(
        page: Int,
        perPage: Int,
        searchQuery: String?,
        isActive: Boolean?
    ): Result<List<Customer>> = try {
        val token = requireAuthToken()
        val response = remoteDataSource.getCustomers(token, page, perPage)

        if (response.success && response.data != null) {
            // Cache customers locally
            localDataSource.saveCustomers(response.data)

            var customers = response.data.toDomainList()

            // Apply local filtering if needed
            if (!searchQuery.isNullOrBlank()) {
                val query = searchQuery.lowercase()
                customers = customers.filter { customer ->
                    customer.companyName.lowercase().contains(query) ||
                    customer.personName.lowercase().contains(query) ||
                    customer.primaryContact.contains(query)
                }
            }

            if (isActive != null) {
                customers = customers.filter { it.isActive == isActive }
            }

            Result.Success(customers)
        } else {
            Result.Error(ApiException(response.message ?: "Failed to fetch customers"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error fetching customers: ${e.message}" }
        // Fallback to local cache
        try {
            val localCustomers = if (searchQuery.isNullOrBlank()) {
                localDataSource.getAllCustomers()
            } else {
                localDataSource.searchCustomers(searchQuery, isActive ?: true)
            }

            if (localCustomers.isNotEmpty()) {
                log.d { "Returning ${localCustomers.size} customers from cache" }
                Result.Success(localCustomers.entityListToDomainList())
            } else {
                Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
            }
        } catch (_: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun getCustomer(customerId: String): Result<Customer> = try {
        val token = requireAuthToken()
        val response = remoteDataSource.getCustomer(token, customerId.toInt())

        if (response.success && response.data != null) {
            // Update local cache
            localDataSource.saveCustomer(response.data)
            Result.Success(response.data.toDomain())
        } else {
            // Try local cache
            val local = localDataSource.getCustomerById(customerId.toInt())
            if (local != null) {
                Result.Success(local.toDomain())
            } else {
                Result.Error(ApiException(response.message ?: "Customer not found"))
            }
        }
    } catch (e: Exception) {
        log.e(e) { "Error fetching customer $customerId: ${e.message}" }
        // Try local cache
        val local = localDataSource.getCustomerById(customerId.toInt())
        if (local != null) {
            Result.Success(local.toDomain())
        } else {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun createCustomer(
        companyName: String,
        personName: String,
        primaryContact: String,
        secondaryContact: String?,
        companyAddress: String?,
        email: String?,
        gstNumber: String?,
        notes: String?
    ): Result<Customer> = try {
        val token = requireAuthToken()
        val request = CreateCustomerRequest(
            companyName = companyName,
            personName = personName,
            primaryContact = primaryContact,
            secondaryContact = secondaryContact,
            companyAddress = companyAddress,
            email = email,
            gstNumber = gstNumber,
            notes = notes
        )

        val response = remoteDataSource.createCustomer(token, request)

        if (response.success && response.data != null) {
            // Cache the new customer
            localDataSource.saveCustomer(response.data)
            Result.Success(response.data.toDomain())
        } else {
            Result.Error(ApiException(response.message ?: "Failed to create customer"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error creating customer: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    override suspend fun updateCustomer(
        customerId: String,
        companyName: String?,
        personName: String?,
        primaryContact: String?,
        secondaryContact: String?,
        companyAddress: String?,
        email: String?,
        gstNumber: String?,
        notes: String?
    ): Result<Customer> = try {
        val token = requireAuthToken()
        val request = UpdateCustomerRequest(
            companyName = companyName,
            personName = personName,
            primaryContact = primaryContact,
            secondaryContact = secondaryContact,
            companyAddress = companyAddress,
            email = email,
            gstNumber = gstNumber,
            notes = notes
        )

        val response = remoteDataSource.updateCustomer(token, customerId.toInt(), request)

        if (response.success && response.data != null) {
            // Update local cache
            localDataSource.saveCustomer(response.data)
            Result.Success(response.data.toDomain())
        } else {
            Result.Error(ApiException(response.message ?: "Failed to update customer"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error updating customer $customerId: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    override suspend fun toggleCustomerStatus(customerId: String): Result<Customer> = try {
        val token = requireAuthToken()
        val response = remoteDataSource.toggleCustomerStatus(token, customerId.toInt())

        if (response.success && response.data != null) {
            // Update local cache
            localDataSource.saveCustomer(response.data)
            Result.Success(response.data.toDomain())
        } else {
            Result.Error(ApiException(response.message ?: "Failed to toggle customer status"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error toggling customer status $customerId: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    override suspend fun getCustomerStatistics(
        customerId: String,
        startDate: String?,
        endDate: String?
    ): Result<CustomerStatistics> = try {
        val token = requireAuthToken()
        val response = remoteDataSource.getCustomerStatistics(token, customerId.toInt(), startDate, endDate)

        if (response.success && response.data != null) {
            Result.Success(response.data.toDomain())
        } else {
            Result.Error(ApiException(response.message ?: "Failed to fetch customer statistics"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error fetching customer statistics $customerId: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    override suspend fun getCustomerSummaries(
        searchQuery: String?,
        activeOnly: Boolean
    ): Result<List<CustomerSummary>> = try {
        val token = requireAuthToken()
        val response = remoteDataSource.getCustomers(token, page = 1, perPage = 100)

        if (response.success && response.data != null) {
            localDataSource.saveCustomers(response.data)

            var summaries = response.data.toSummaryList()

            if (!searchQuery.isNullOrBlank()) {
                val query = searchQuery.lowercase()
                summaries = summaries.filter { summary ->
                    summary.companyName.lowercase().contains(query) ||
                    summary.personName.lowercase().contains(query) ||
                    summary.primaryContact.contains(query)
                }
            }

            if (activeOnly) {
                summaries = summaries.filter { it.isActive }
            }

            Result.Success(summaries)
        } else {
            // Fallback to local cache
            val local = if (searchQuery.isNullOrBlank()) {
                localDataSource.getActiveCustomers()
            } else {
                localDataSource.searchCustomers(searchQuery, activeOnly)
            }
            Result.Success(local.entityListToSummaryList())
        }
    } catch (e: Exception) {
        log.e(e) { "Error fetching customer summaries: ${e.message}" }
        // Fallback to local cache
        val local = if (searchQuery.isNullOrBlank()) {
            localDataSource.getActiveCustomers()
        } else {
            localDataSource.searchCustomers(searchQuery, activeOnly)
        }
        if (local.isNotEmpty()) {
            Result.Success(local.entityListToSummaryList())
        } else {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun getLocalCustomers(): List<CustomerSummary> {
        return localDataSource.getActiveCustomers().entityListToSummaryList()
    }

    override suspend fun refreshCustomers(): Result<List<Customer>> = try {
        val token = requireAuthToken()
        val response = remoteDataSource.getCustomers(token, page = 1, perPage = 100)

        if (response.success && response.data != null) {
            // Clear and repopulate cache
            localDataSource.clearCache()
            localDataSource.saveCustomers(response.data)
            log.d { "Refreshed ${response.data.size} customers" }
            Result.Success(response.data.toDomainList())
        } else {
            Result.Error(ApiException(response.message ?: "Failed to refresh customers"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error refreshing customers: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    // ============= Customer Trips =============

    override suspend fun getCustomerTrips(
        customerId: String,
        page: Int,
        perPage: Int,
        state: String?
    ): Result<CustomerTripsResult> = try {
        val token = requireAuthToken()
        val customerIdInt = customerId.toIntOrNull()
            ?: return Result.Error(ApiException("Invalid customer ID: $customerId"))
        log.d { "Fetching customer trips for customer: $customerId, page: $page, state: $state" }
        val response = remoteDataSource.getCustomerTrips(token, customerIdInt, page, perPage, state)
        log.d { "Customer trips response success: ${response.success}, message: ${response.message}" }

        if (response.success) {
            // Use the convenience accessor 'trips' which gets items from data.items
            val trips = response.trips.map { it.toDomain() }
            val summary = response.summary?.toDomain()
            val totalPages = response.totalPages ?: 1
            val total = response.total ?: trips.size
            log.d { "Customer trips loaded: ${trips.size} trips, total: $total, totalPages: $totalPages" }

            Result.Success(
                CustomerTripsResult(
                    trips = trips,
                    summary = summary,
                    page = page,
                    totalPages = totalPages,
                    total = total,
                    hasMore = response.hasMore
                )
            )
        } else {
            log.e { "Customer trips API failed: ${response.message}" }
            Result.Error(ApiException(response.message ?: "Failed to fetch customer trips"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error fetching customer trips $customerId: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    // ============= Customer Pending Payments =============

    override suspend fun getCustomerPendingPayments(
        customerId: String,
        page: Int,
        perPage: Int
    ): Result<CustomerPendingPaymentsResult> = try {
        val token = requireAuthToken()
        val response = remoteDataSource.getCustomerPendingPayments(token, customerId.toInt(), page, perPage)

        if (response.success) {
            val payments = response.data?.toPendingPaymentsDomain() ?: emptyList()
            val totalPages = response.totalPages ?: 1
            val total = response.total ?: payments.size

            Result.Success(
                CustomerPendingPaymentsResult(
                    payments = payments,
                    totalPending = response.totalPending ?: 0.0,
                    overdueCount = response.overdueCount ?: 0,
                    page = page,
                    totalPages = totalPages,
                    total = total,
                    hasMore = page < totalPages
                )
            )
        } else {
            Result.Error(ApiException(response.message ?: "Failed to fetch pending payments"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error fetching customer pending payments $customerId: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    // ============= Customer Payments =============

    override suspend fun getCustomerPayments(
        customerId: String,
        page: Int,
        perPage: Int,
        status: String?,
        mode: String?
    ): Result<CustomerPaymentsResult> = try {
        val token = requireAuthToken()
        val response = remoteDataSource.getCustomerPayments(token, customerId.toInt(), page, perPage, status, mode)

        if (response.success) {
            val payments = response.data?.toPaymentsDomain() ?: emptyList()
            val totalPages = response.totalPages ?: 1
            val total = response.total ?: payments.size

            Result.Success(
                CustomerPaymentsResult(
                    payments = payments,
                    totalReceived = response.totalReceived ?: 0.0,
                    page = page,
                    totalPages = totalPages,
                    total = total,
                    hasMore = page < totalPages
                )
            )
        } else {
            Result.Error(ApiException(response.message ?: "Failed to fetch customer payments"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error fetching customer payments $customerId: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    // ============= Customer Payment Summary =============

    override suspend fun getCustomerPaymentSummary(
        customerId: String,
        startDate: String?,
        endDate: String?
    ): Result<CustomerPaymentSummary> = try {
        val token = requireAuthToken()
        val response = remoteDataSource.getCustomerPaymentSummary(token, customerId.toInt(), startDate, endDate)

        if (response.success && response.data != null) {
            Result.Success(response.data.toDomain())
        } else {
            Result.Error(ApiException(response.message ?: "Failed to fetch payment summary"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error fetching customer payment summary $customerId: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    // ============= Customer Financial Report =============

    override suspend fun getCustomerFinancialReport(
        customerId: String,
        period: String,
        startDate: String?,
        endDate: String?
    ): Result<CustomerFinancialReport> = try {
        val token = requireAuthToken()
        val response = remoteDataSource.getCustomerFinancialReport(token, customerId.toInt(), period, startDate, endDate)

        if (response.success && response.data != null) {
            Result.Success(response.data.toDomain())
        } else {
            Result.Error(ApiException(response.message ?: "Failed to fetch financial report"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error fetching customer financial report $customerId: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    private suspend fun requireAuthToken(): String {
        return AuthTokenHelper.requireAuthTokenOrRedirect {
            userLocalDataSource.getAuthToken()
        }
    }
}
