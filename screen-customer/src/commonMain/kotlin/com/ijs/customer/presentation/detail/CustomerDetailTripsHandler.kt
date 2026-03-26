package com.ijs.customer.presentation.detail

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.customer.TAG_CUSTOMER_TRIPS
import com.ijs.customer.domain.entity.CustomerTrip
import com.ijs.customer.domain.entity.CustomerTripsSummary
import com.ijs.customer.domain.repository.CustomerRepository
import com.ijs.customer.presentation.detail.CustomerDetailContract.State
import com.ijs.customer.presentation.detail.CustomerDetailContract.TripStateFilter
import com.indusjs.error.result.Result

/**
 * Handles trips tab data loading, pagination, and filtering
 * for CustomerDetailViewModel.
 */
class CustomerDetailTripsHandler(
    private val customerRepository: CustomerRepository,
    private val getState: () -> State,
    private val setState: (State.() -> State) -> Unit,
    private val logger: FleetLogger
) {

    suspend fun loadTrips(customerId: String) {
        if (customerId.isBlank()) {
            logger.w(TAG_CUSTOMER_TRIPS, "loadTrips: customerId is blank, skipping")
            return
        }

        logger.d(TAG_CUSTOMER_TRIPS, "loadTrips: Starting load for customer $customerId, filter: ${getState().tripStateFilter}")
        setState { copy(isLoadingTrips = true, tripsError = null, tripsPage = 1) }

        when (val result = customerRepository.getCustomerTrips(
            customerId = customerId,
            page = 1,
            state = getState().tripStateFilter.apiValue
        )) {
            is Result.Success -> {
                logger.d(TAG_CUSTOMER_TRIPS, "loadTrips: Success - ${result.data.trips.size} trips loaded")
                setState {
                    copy(
                        isLoadingTrips = false,
                        trips = result.data.trips,
                        tripsSummary = result.data.summary,
                        hasMoreTrips = result.data.hasMore,
                        tripsDataLoaded = true
                    )
                }
            }
            is Result.Error -> {
                logger.e(TAG_CUSTOMER_TRIPS, "loadTrips: Error - ${result.message}")
                setState {
                    copy(
                        isLoadingTrips = false,
                        tripsError = result.message ?: "Failed to load trips"
                    )
                }
            }
            is Result.Loading -> { }
        }
    }

    suspend fun loadMoreTrips(customerId: String) {
        val state = getState()
        if (state.isLoadingTrips || !state.hasMoreTrips) return

        val nextPage = state.tripsPage + 1
        setState { copy(isLoadingTrips = true) }

        when (val result = customerRepository.getCustomerTrips(
            customerId = customerId,
            page = nextPage,
            state = state.tripStateFilter.apiValue
        )) {
            is Result.Success -> {
                setState {
                    copy(
                        isLoadingTrips = false,
                        trips = trips + result.data.trips,
                        tripsPage = nextPage,
                        hasMoreTrips = result.data.hasMore
                    )
                }
            }
            is Result.Error -> {
                setState { copy(isLoadingTrips = false) }
            }
            is Result.Loading -> { }
        }
    }

    suspend fun setTripStateFilter(customerId: String, filter: TripStateFilter) {
        setState { copy(tripStateFilter = filter, tripsDataLoaded = false) }
        loadTrips(customerId)
    }

    suspend fun refreshTrips(customerId: String) {
        setState { copy(tripsDataLoaded = false) }
        loadTrips(customerId)
    }
}

