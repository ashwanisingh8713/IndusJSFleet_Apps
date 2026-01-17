package com.indusjs.fleet.di

import com.indusjs.fleet.data.database.dao.CustomerDao
import com.indusjs.fleet.data.datasource.customer.CustomerLocalDataSource
import com.indusjs.fleet.data.datasource.customer.CustomerLocalDataSourceImpl
import com.indusjs.fleet.data.datasource.customer.CustomerRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.repository.customer.CustomerRepositoryImpl
import com.indusjs.fleet.domain.repository.customer.CustomerRepository
import com.indusjs.fleet.domain.usecase.customer.*
import com.indusjs.fleet.presentation.customers.create.CreateCustomerViewModel
import com.indusjs.fleet.presentation.customers.detail.CustomerDetailViewModel
import com.indusjs.fleet.presentation.customers.list.CustomersListViewModel
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

/**
 * DI graph for Customer feature.
 * Provides all dependencies for customer management screens.
 */
@SingleIn(FeatureScope::class)
@DependencyGraph
abstract class CustomerFeatureGraph {

    // ==================== Bindings ====================

    @Binds
    abstract fun bindCustomerRepository(impl: CustomerRepositoryImpl): CustomerRepository

    @Binds
    abstract fun bindCustomerLocalDataSource(impl: CustomerLocalDataSourceImpl): CustomerLocalDataSource

    // ==================== Exposed Dependencies ====================

    abstract val customersListViewModel: CustomersListViewModel
    abstract val customerDetailViewModel: CustomerDetailViewModel
    abstract val createCustomerViewModel: CreateCustomerViewModel

    // Use cases
    abstract val getCustomersUseCase: GetCustomersUseCase
    abstract val getCustomerUseCase: GetCustomerUseCase
    abstract val createCustomerUseCase: CreateCustomerUseCase
    abstract val updateCustomerUseCase: UpdateCustomerUseCase
    abstract val toggleCustomerStatusUseCase: ToggleCustomerStatusUseCase
    abstract val getCustomerStatisticsUseCase: GetCustomerStatisticsUseCase
    abstract val getCustomerSummariesUseCase: GetCustomerSummariesUseCase
    abstract val getLocalCustomersUseCase: GetLocalCustomersUseCase
    abstract val refreshCustomersUseCase: RefreshCustomersUseCase

    // ==================== Factory ====================

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
            @Provides json: Json,
            @Provides customerDao: CustomerDao,
            @Provides userLocalDataSource: UserLocalDataSource
        ): CustomerFeatureGraph
    }
}

