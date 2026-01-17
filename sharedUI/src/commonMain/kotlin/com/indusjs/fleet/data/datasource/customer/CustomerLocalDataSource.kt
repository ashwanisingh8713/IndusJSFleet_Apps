package com.indusjs.fleet.data.datasource.customer

import co.touchlab.kermit.Logger
import com.indusjs.fleet.core.util.currentTimeMillis
import com.indusjs.fleet.data.database.dao.CustomerDao
import com.indusjs.fleet.data.database.entity.CustomerEntity
import com.indusjs.fleet.data.datasource.LocalDataSource
import com.indusjs.fleet.data.model.customer.CustomerDto
import dev.zacsweers.metro.Inject

/**
 * Local data source interface for Customer caching.
 */
interface CustomerLocalDataSource : LocalDataSource {

    /**
     * Get all cached customers.
     */
    suspend fun getAllCustomers(): List<CustomerEntity>

    /**
     * Get active customers only.
     */
    suspend fun getActiveCustomers(): List<CustomerEntity>

    /**
     * Get customer by ID.
     */
    suspend fun getCustomerById(customerId: Int): CustomerEntity?

    /**
     * Search customers by query.
     */
    suspend fun searchCustomers(query: String, activeOnly: Boolean = true): List<CustomerEntity>

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

/**
 * Implementation of CustomerLocalDataSource using Room.
 */
@Inject
class CustomerLocalDataSourceImpl(
    private val customerDao: CustomerDao
) : CustomerLocalDataSource {

    private val log = Logger.withTag("CustomerLocalDataSource")

    override suspend fun getAllCustomers(): List<CustomerEntity> {
        return customerDao.getAllCustomers()
    }

    override suspend fun getActiveCustomers(): List<CustomerEntity> {
        return customerDao.getActiveCustomers()
    }

    override suspend fun getCustomerById(customerId: Int): CustomerEntity? {
        return customerDao.getCustomerById(customerId)
    }

    override suspend fun searchCustomers(query: String, activeOnly: Boolean): List<CustomerEntity> {
        return customerDao.searchCustomers(query, activeOnly)
    }

    override suspend fun saveCustomer(customer: CustomerDto) {
        val entity = customer.toEntity()
        customerDao.insertCustomer(entity)
        log.d { "Saved customer: ${customer.companyName}" }
    }

    override suspend fun saveCustomers(customers: List<CustomerDto>) {
        val entities = customers.map { it.toEntity() }
        customerDao.insertCustomers(entities)
        log.d { "Saved ${customers.size} customers" }
    }

    override suspend fun clearCache() {
        customerDao.deleteAllCustomers()
        log.d { "Cleared customer cache" }
    }

    override suspend fun getCustomerCount(): Int {
        return customerDao.getCustomerCount()
    }

    private fun CustomerDto.toEntity(): CustomerEntity = CustomerEntity(
        id = id,
        companyName = companyName,
        personName = personName,
        primaryContact = primaryContact,
        secondaryContact = secondaryContact,
        companyAddress = companyAddress,
        email = email,
        gstNumber = gstNumber,
        notes = notes,
        isActive = isActive,
        ownerId = ownerId,
        createdById = createdById,
        createdAt = createdAt,
        updatedAt = updatedAt,
        cachedAt = currentTimeMillis()
    )
}

