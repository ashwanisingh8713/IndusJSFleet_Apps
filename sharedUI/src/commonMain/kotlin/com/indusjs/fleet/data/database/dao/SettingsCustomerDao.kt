package com.indusjs.fleet.data.database.dao

import com.indusjs.fleet.data.database.entity.CustomerEntity
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * DAO interface for Customer table operations.
 */
interface CustomerDao {
    suspend fun getAllCustomers(): List<CustomerEntity>
    suspend fun getActiveCustomers(): List<CustomerEntity>
    suspend fun getCustomerById(customerId: Int): CustomerEntity?
    suspend fun searchCustomers(query: String, activeOnly: Boolean = true): List<CustomerEntity>
    suspend fun insertCustomer(customer: CustomerEntity)
    suspend fun insertCustomers(customers: List<CustomerEntity>)
    suspend fun updateCustomer(customer: CustomerEntity)
    suspend fun deleteCustomer(customerId: Int)
    suspend fun deleteAllCustomers()
    suspend fun getCustomerCount(): Int
    fun observeCustomers(): Flow<List<CustomerEntity>>
}

/**
 * Settings-based implementation of CustomerDao for cross-platform support.
 */
class SettingsCustomerDao(
    private val settings: Settings,
    private val json: Json
) : CustomerDao {

    companion object {
        private const val KEY_CUSTOMERS = "customers_cache"
    }

    private val _customersFlow = MutableStateFlow<List<CustomerEntity>>(emptyList())

    init {
        // Load cached data on init
        val cached = loadCustomers()
        _customersFlow.value = cached
    }

    override suspend fun getAllCustomers(): List<CustomerEntity> {
        return loadCustomers().sortedBy { it.companyName }
    }

    override suspend fun getActiveCustomers(): List<CustomerEntity> {
        return loadCustomers()
            .filter { it.isActive }
            .sortedBy { it.companyName }
    }

    override suspend fun getCustomerById(customerId: Int): CustomerEntity? {
        return loadCustomers().find { it.id == customerId }
    }

    override suspend fun searchCustomers(query: String, activeOnly: Boolean): List<CustomerEntity> {
        val lowerQuery = query.lowercase()
        return loadCustomers()
            .filter { customer ->
                val matchesQuery = customer.companyName.lowercase().contains(lowerQuery) ||
                    customer.personName.lowercase().contains(lowerQuery) ||
                    customer.primaryContact.contains(query)
                val matchesActive = !activeOnly || customer.isActive
                matchesQuery && matchesActive
            }
            .sortedBy { it.companyName }
    }

    override suspend fun insertCustomer(customer: CustomerEntity) {
        val customers = loadCustomers().toMutableList()
        val index = customers.indexOfFirst { it.id == customer.id }
        if (index >= 0) {
            customers[index] = customer
        } else {
            customers.add(customer)
        }
        saveCustomers(customers)
    }

    override suspend fun insertCustomers(customers: List<CustomerEntity>) {
        val existing = loadCustomers().toMutableList()
        customers.forEach { customer ->
            val index = existing.indexOfFirst { it.id == customer.id }
            if (index >= 0) {
                existing[index] = customer
            } else {
                existing.add(customer)
            }
        }
        saveCustomers(existing)
    }

    override suspend fun updateCustomer(customer: CustomerEntity) {
        insertCustomer(customer)
    }

    override suspend fun deleteCustomer(customerId: Int) {
        val customers = loadCustomers().filterNot { it.id == customerId }
        saveCustomers(customers)
    }

    override suspend fun deleteAllCustomers() {
        settings.remove(KEY_CUSTOMERS)
        _customersFlow.value = emptyList()
    }

    override suspend fun getCustomerCount(): Int {
        return loadCustomers().size
    }

    override fun observeCustomers(): Flow<List<CustomerEntity>> {
        return _customersFlow.map { it.sortedBy { customer -> customer.companyName } }
    }

    private fun loadCustomers(): List<CustomerEntity> {
        val jsonString = settings.getStringOrNull(KEY_CUSTOMERS) ?: return emptyList()
        return try {
            json.decodeFromString(ListSerializer(CustomerEntity.serializer()), jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCustomers(customers: List<CustomerEntity>) {
        val jsonString = json.encodeToString(ListSerializer(CustomerEntity.serializer()), customers)
        settings.putString(KEY_CUSTOMERS, jsonString)
        _customersFlow.value = customers
    }
}

