package com.indusjs.fleet.di

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.data.datasource.dashboard.DashboardRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.payment.TripPaymentRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.repository.payment.TripPaymentRepositoryImpl
import com.indusjs.fleet.domain.repository.payment.TripPaymentRepository
import com.indusjs.fleet.domain.repository.trip.TripRepository
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.fleet.presentation.payments.AddPaymentViewModel
import com.indusjs.fleet.presentation.payments.PaymentDetailViewModel
import com.indusjs.fleet.presentation.payments.PaymentsViewModel
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

/**
 * Metro DI Graph for Payment feature.
 */
@SingleIn(FeatureScope::class)
@DependencyGraph
abstract class PaymentFeatureGraph {

    @Binds
    abstract fun bindTripPaymentRepository(
        impl: TripPaymentRepositoryImpl
    ): TripPaymentRepository

    abstract val paymentsViewModel: PaymentsViewModel
    abstract val addPaymentViewModel: AddPaymentViewModel
    abstract val paymentDetailViewModel: PaymentDetailViewModel

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
            @Provides json: Json,
            @Provides dispatcherProvider: DispatcherProvider,
            @Provides userLocalDataSource: UserLocalDataSource,
            @Provides dashboardRemoteDataSource: DashboardRemoteDataSourceImpl,
            @Provides tripRepository: TripRepository,
            @Provides userRepository: UserRepository
        ): PaymentFeatureGraph
    }
}
