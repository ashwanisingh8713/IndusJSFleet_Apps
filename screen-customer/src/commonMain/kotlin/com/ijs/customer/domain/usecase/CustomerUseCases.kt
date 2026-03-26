package com.ijs.customer.domain.usecase

import com.ijs.customer.domain.entity.Customer
import com.ijs.customer.domain.entity.CustomerStatistics
import com.ijs.customer.domain.entity.CustomerSummary
import com.ijs.customer.domain.repository.CustomerRepository
import com.indusjs.error.result.Result
import dev.zacsweers.metro.Inject

/**
 * Use case to get all customers with pagination and filtering.
 */
@Inject
class GetCustomersUseCase(
    private val repository: CustomerRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        perPage: Int = 20,
        searchQuery: String? = null,
        isActive: Boolean? = null
    ): Result<List<Customer>> = repository.getCustomers(page, perPage, searchQuery, isActive)
}

/**
 * Use case to get a single customer by ID.
 */
@Inject
class GetCustomerUseCase(
    private val repository: CustomerRepository
) {
    suspend operator fun invoke(customerId: String): Result<Customer> =
        repository.getCustomer(customerId)
}

/**
 * Use case to create a new customer.
 */
@Inject
class CreateCustomerUseCase(
    private val repository: CustomerRepository
) {
    suspend operator fun invoke(
        companyName: String,
        personName: String,
        primaryContact: String,
        secondaryContact: String? = null,
        companyAddress: String? = null,
        email: String? = null,
        gstNumber: String? = null,
        notes: String? = null
    ): Result<Customer> = repository.createCustomer(
        companyName = companyName,
        personName = personName,
        primaryContact = primaryContact,
        secondaryContact = secondaryContact,
        companyAddress = companyAddress,
        email = email,
        gstNumber = gstNumber,
        notes = notes
    )
}

/**
 * Use case to update an existing customer.
 */
@Inject
class UpdateCustomerUseCase(
    private val repository: CustomerRepository
) {
    suspend operator fun invoke(
        customerId: String,
        companyName: String? = null,
        personName: String? = null,
        primaryContact: String? = null,
        secondaryContact: String? = null,
        companyAddress: String? = null,
        email: String? = null,
        gstNumber: String? = null,
        notes: String? = null
    ): Result<Customer> = repository.updateCustomer(
        customerId = customerId,
        companyName = companyName,
        personName = personName,
        primaryContact = primaryContact,
        secondaryContact = secondaryContact,
        companyAddress = companyAddress,
        email = email,
        gstNumber = gstNumber,
        notes = notes
    )
}

/**
 * Use case to toggle customer status (active/inactive).
 */
@Inject
class ToggleCustomerStatusUseCase(
    private val repository: CustomerRepository
) {
    suspend operator fun invoke(customerId: String): Result<Customer> =
        repository.toggleCustomerStatus(customerId)
}

/**
 * Use case to get customer statistics.
 * Only for Owner and General Manager.
 */
@Inject
class GetCustomerStatisticsUseCase(
    private val repository: CustomerRepository
) {
    suspend operator fun invoke(customerId: String): Result<CustomerStatistics> =
        repository.getCustomerStatistics(customerId)
}

/**
 * Use case to get customer summaries for selection.
 * Used in dropdowns and autocomplete in CreateTrip screen.
 */
@Inject
class GetCustomerSummariesUseCase(
    private val repository: CustomerRepository
) {
    suspend operator fun invoke(
        searchQuery: String? = null,
        activeOnly: Boolean = true
    ): Result<List<CustomerSummary>> = repository.getCustomerSummaries(searchQuery, activeOnly)
}

/**
 * Use case to get local customers from cache.
 */
@Inject
class GetLocalCustomersUseCase(
    private val repository: CustomerRepository
) {
    suspend operator fun invoke(): List<CustomerSummary> = repository.getLocalCustomers()
}

/**
 * Use case to refresh customers from remote.
 */
@Inject
class RefreshCustomersUseCase(
    private val repository: CustomerRepository
) {
    suspend operator fun invoke(): Result<List<Customer>> = repository.refreshCustomers()
}

