package com.ijs.customer.data.repository

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.customer.TAG_CUSTOMER_REPO
import com.indusjs.error.exception.ApiException
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.auth.AuthTokenHelper
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.ijs.customer.data.datasource.CustomerLocalDataSource
import com.ijs.customer.data.datasource.CustomerRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.ijs.customer.data.mapper.CustomerMapper.toDomain
import com.ijs.customer.data.mapper.CustomerMapper.toDomainList
import com.ijs.customer.data.mapper.CustomerMapper.toSummaryList
import com.ijs.customer.data.mapper.CustomerMapper.entityListToDomainList
import com.ijs.customer.data.mapper.CustomerMapper.entityListToSummaryList
import com.ijs.customer.data.mapper.CustomerMapper.toTripsDomain
import com.ijs.customer.data.mapper.CustomerMapper.toPendingPaymentsDomain
import com.ijs.customer.data.mapper.CustomerMapper.toPaymentsDomain
import com.ijs.customer.data.model.CreateCustomerRequest
import com.ijs.customer.data.model.UpdateCustomerRequest
import com.ijs.customer.domain.entity.*
import com.ijs.customer.domain.repository.CustomerRepository
import com.ijs.customer.domain.repository.CustomerTripsResult
import com.ijs.customer.domain.repository.CustomerPendingPaymentsResult
import com.ijs.customer.domain.repository.CustomerPaymentsResult
import dev.zacsweers.metro.Inject

/**
 * Implementation of CustomerRepository.
 * Handles customer operations with offline-first caching.
 */
@Inject
class CustomerRepositoryImpl(
    private val remoteDataSource: CustomerRemoteDataSource,
    private val localDataSource: CustomerLocalDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    private val logger: FleetLogger
) : CustomerRepository {


    override suspend fun getCustomers(
        page: Int,
        perPage: Int,
        searchQuery: String?,
        isActive: Boolean?
    ): Result<List<Customer>> = try {
        val token = requireAuthToken()
        val response = remoteDataSource.getCustomers(
            token = token,
            page = page,
            perPage = perPage,
            search = searchQuery,
            isActive = isActive
        )

        if (response.success && response.customers.isNotEmpty()) {
            // Cache customers locally
            localDataSource.saveCustomers(response.customers)

            var customers = response.customers.toDomainList()

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
        } else if (response.success) {
            // Success but empty list
            Result.Success(emptyList())
        } else {
            Result.Error(ApiException(response.message ?: "Failed to fetch customers"))
        }
    } catch (e: Exception) {
        logger.e(TAG_CUSTOMER_REPO, "Error fetching customers: ${e.message}", e)
        // Fallback to local cache
        try {
            val localCustomers = if (searchQuery.isNullOrBlank()) {
                localDataSource.getAllCustomers()
            } else {
                localDataSource.searchCustomers(searchQuery, isActive ?: true)
            }

            if (localCustomers.isNotEmpty()) {
                logger.d(TAG_CUSTOMER_REPO, "Returning ${localCustomers.size} customers from cache")
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
        logger.e(TAG_CUSTOMER_REPO, "Error fetching customer $customerId: ${e.message}", e)
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
        logger.e(TAG_CUSTOMER_REPO, "Error creating customer: ${e.message}", e)
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
        logger.e(TAG_CUSTOMER_REPO, "Error updating customer $customerId: ${e.message}", e)
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
        logger.e(TAG_CUSTOMER_REPO, "Error toggling customer status $customerId: ${e.message}", e)
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
        logger.e(TAG_CUSTOMER_REPO, "Error fetching customer statistics $customerId: ${e.message}", e)
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    override suspend fun getCustomerSummaries(
        searchQuery: String?,
        activeOnly: Boolean
    ): Result<List<CustomerSummary>> = try {
        val token = requireAuthToken()
        val response = remoteDataSource.getCustomers(token, page = 1, perPage = 100)

        if (response.success && response.customers.isNotEmpty()) {
            localDataSource.saveCustomers(response.customers)

            var summaries = response.customers.toSummaryList()

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
        logger.e(TAG_CUSTOMER_REPO, "Error fetching customer summaries: ${e.message}", e)
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

        if (response.success && response.customers.isNotEmpty()) {
            // Clear and repopulate cache
            localDataSource.clearCache()
            localDataSource.saveCustomers(response.customers)
            logger.d(TAG_CUSTOMER_REPO, "Refreshed ${response.customers.size} customers")
            Result.Success(response.customers.toDomainList())
        } else if (response.success) {
            // Success but empty - clear cache
            localDataSource.clearCache()
            Result.Success(emptyList())
        } else {
            Result.Error(ApiException(response.message ?: "Failed to refresh customers"))
        }
    } catch (e: Exception) {
        logger.e(TAG_CUSTOMER_REPO, "Error refreshing customers: ${e.message}", e)
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
        logger.d(TAG_CUSTOMER_REPO, "Fetching customer trips for customer: $customerId, page: $page, state: $state")
        val response = remoteDataSource.getCustomerTrips(token, customerIdInt, page, perPage, state)
        logger.d(TAG_CUSTOMER_REPO, "Customer trips response success: ${response.success}, message: ${response.message}")

        if (response.success) {
            // Use the convenience accessor 'trips' which gets items from data.items
            val trips = response.trips.map { it.toDomain() }
            val summary = response.summary?.toDomain()
            val totalPages = response.totalPages ?: 1
            val total = response.total ?: trips.size
            logger.d(TAG_CUSTOMER_REPO, "Customer trips loaded: ${trips.size} trips, total: $total, totalPages: $totalPages")

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
            logger.e(TAG_CUSTOMER_REPO, "Customer trips API failed: ${response.message}")
            Result.Error(ApiException(response.message ?: "Failed to fetch customer trips"))
        }
    } catch (e: Exception) {
        logger.e(TAG_CUSTOMER_REPO, "Error fetching customer trips $customerId: ${e.message}", e)
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
            val payments = response.payments.toPendingPaymentsDomain()

            Result.Success(
                CustomerPendingPaymentsResult(
                    payments = payments,
                    totalPending = response.totalPending,
                    overdueCount = response.overdueCount,
                    page = response.page,
                    totalPages = response.totalPages,
                    total = response.total,
                    hasMore = response.hasMore
                )
            )
        } else {
            Result.Error(ApiException(response.message ?: "Failed to fetch pending payments"))
        }
    } catch (e: Exception) {
        logger.e(TAG_CUSTOMER_REPO, "Error fetching customer pending payments $customerId: ${e.message}", e)
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
            val payments = response.payments.toPaymentsDomain()

            Result.Success(
                CustomerPaymentsResult(
                    payments = payments,
                    totalReceived = response.totalReceived,
                    page = response.page,
                    totalPages = response.totalPages,
                    total = response.total,
                    hasMore = response.hasMore
                )
            )
        } else {
            Result.Error(ApiException(response.message ?: "Failed to fetch customer payments"))
        }
    } catch (e: Exception) {
        logger.e(TAG_CUSTOMER_REPO, "Error fetching customer payments $customerId: ${e.message}", e)
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
        logger.e(TAG_CUSTOMER_REPO, "Error fetching customer payment summary $customerId: ${e.message}", e)
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
        logger.e(TAG_CUSTOMER_REPO, "Error fetching customer financial report $customerId: ${e.message}", e)
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    private suspend fun requireAuthToken(): String {
        return AuthTokenHelper.requireAuthTokenOrRedirect {
            userLocalDataSource.getAuthToken()
        }
    }
}
