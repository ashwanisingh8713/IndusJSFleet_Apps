package com.ijs.payment.domain.repository

import com.ijs.payment.presentation.TripSummaryForPayment
import com.indusjs.error.result.Result
import kotlinx.coroutines.flow.Flow

/**
 * Provider interface for trip data needed by the payment feature.
 * Breaks the circular dependency between screen-payment and screen-trip.
 *
 * Implementation is provided by sharedUI's DefaultViewModelProvider,
 * which adapts the real TripRepository from screen-trip.
 */
interface TripProviderForPayment {

    /**
     * Load all trips as payment-ready summaries.
     */
    fun getTripsForPayment(): Flow<Result<List<TripSummaryForPayment>>>

    /**
     * Load a single trip by ID as a payment-ready summary.
     */
    suspend fun getTripForPayment(tripId: String): Result<TripSummaryForPayment>
}

