package com.indusjs.fleet.data.datasource.customer

import co.touchlab.kermit.Logger
import com.indusjs.fleet.core.util.currentTimeMillis
import com.indusjs.fleet.data.database.dao.CustomerDao
import com.indusjs.fleet.data.database.entity.CustomerEntity
import com.indusjs.fleet.data.model.customer.CustomerDto
import dev.zacsweers.metro.Inject

/**
 * Implementation of CustomerLocalDataSource using Room.
 *
 * The interface [CustomerLocalDataSource] is defined in ijs-network-lib.
 * This implementation converts between DTOs and Room entities internally.
 */
@Inject
class CustomerLocalDataSourceImpl(
    private val customerDao: CustomerDao
) : CustomerLocalDataSource {

    private val log = Logger.withTag("CustomerLocalDataSource")

    override suspend fun getAllCustomers(): List<CustomerDto> {
        return customerDao.getAllCustomers().map { it.toDto() }
    }

    override suspend fun getActiveCustomers(): List<CustomerDto> {
        return customerDao.getActiveCustomers().map { it.toDto() }
    }

    override suspend fun getCustomerById(customerId: Int): CustomerDto? {
        return customerDao.getCustomerById(customerId)?.toDto()
    }

    override suspend fun searchCustomers(query: String, activeOnly: Boolean): List<CustomerDto> {
        return customerDao.searchCustomers(query, activeOnly).map { it.toDto() }
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

    private fun CustomerEntity.toDto(): CustomerDto = CustomerDto(
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
        updatedAt = updatedAt
    )
}
