package com.indusjs.fleet.data.datasource.customer

import com.indusjs.fleet.data.datasource.LocalDataSource
import com.indusjs.fleet.data.model.customer.CustomerDto

/**
 * Local data source interface for Customer caching.
 *
 * NOTE: Interface lives in ijs-network-lib (uses DTOs, no Room dependency).
 * Implementation (CustomerLocalDataSourceImpl) lives in sharedUI (uses Room DAOs).
 *
 * All methods use [CustomerDto] instead of Room entities to keep
 * this interface free of Room dependencies.
 */
interface CustomerLocalDataSource : LocalDataSource {

    /**
     * Get all cached customers.
     */
    suspend fun getAllCustomers(): List<CustomerDto>

    /**
     * Get active customers only.
     */
    suspend fun getActiveCustomers(): List<CustomerDto>

    /**
     * Get customer by ID.
     */
    suspend fun getCustomerById(customerId: Int): CustomerDto?

    /**
     * Search customers by query.
     */
    suspend fun searchCustomers(query: String, activeOnly: Boolean = true): List<CustomerDto>

    /**
     * Save a single customer.
     */
    suspend fun saveCustomer(customer: CustomerDto)

    /**
     * Save multiple customers.
     */
    suspend fun saveCustomers(customers: List<CustomerDto>)

    /**
     * Delete all cached customers.
     */
    suspend fun clearCache()

    /**
     * Get customer count.
     */
    suspend fun getCustomerCount(): Int
}

