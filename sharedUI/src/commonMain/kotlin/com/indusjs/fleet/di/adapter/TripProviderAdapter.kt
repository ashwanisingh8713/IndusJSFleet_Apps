package com.indusjs.fleet.di.adapter

import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.datetimeutils.FleetEpoch
import com.indusjs.error.result.Result
import com.ijs.trip.payment.domain.repository.TripProviderForPayment
import com.ijs.trip.payment.presentation.TripSummaryForPayment
import com.ijs.trip.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Adapter that bridges [TripRepository] (screen-trip) to [TripProviderForPayment] (screen-trip-payment).
 * Breaks the circular dependency between screen-trip ↔ screen-trip-payment.
 */
class TripProviderAdapter(
    private val tripRepository: TripRepository
) : TripProviderForPayment {

    override fun getTripsForPayment(): Flow<Result<List<TripSummaryForPayment>>> {
        return tripRepository.getTrips().map { result ->
            when (result) {
                is Result.Success -> Result.Success(result.data.map { trip -> trip.toSummary() })
                is Result.Error -> result
                is Result.Loading -> result
            }
        }
    }

    override suspend fun getTripForPayment(tripId: String): Result<TripSummaryForPayment> {
        return when (val result = tripRepository.getTripById(tripId)) {
            is Result.Success -> Result.Success(result.data.toSummary())
            is Result.Error -> result
            is Result.Loading -> result
        }
    }

    private fun com.ijs.trip.domain.entity.Trip.toSummary(): TripSummaryForPayment {
        // Trip timestamps are UTC epoch-millis (Long?). TripSummaryForPayment
        // still expects display Strings (DD-MM-YYYY date, HH:mm time), so we
        // format at this boundary.
        val startDateSource: Long? = plannedStart ?: scheduledDate
        val endDateSource: Long? = plannedEnd ?: deliveryDate

        return TripSummaryForPayment(
            id = id,
            vehicleId = vehicleId,
            driverId = driverId,
            vehicleRegistration = vehicleNumber ?: "N/A",
            driverName = driverName,
            startLocation = startLocation?.address ?: "Unknown",
            endLocation = endLocation?.address ?: "Unknown",
            tripPrice = tripPrice ?: 0.0,
            paidAmount = paidTripPrice ?: 0.0,
            // Prefer the backend's authoritative pending (selling_value − paid); fall back
            // to quote − paid only when the API didn't send it.
            pendingAmount = this.pendingAmount ?: ((tripPrice ?: 0.0) - (paidTripPrice ?: 0.0)),
            customerId = customerId,
            customerName = customerName,
            customerContact = customerContact,
            state = status.name,
            scheduledDate = FleetDateTime.timestampToDateString(startDateSource),
            paymentStatus = paymentStatus,
            tripStartDate = FleetDateTime.timestampToDateString(startDateSource),
            tripEndDate = FleetDateTime.timestampToDateString(endDateSource),
            tripStartTime = extractTime(plannedStart),
            tripEndTime = extractTime(plannedEnd)
        )
    }

    /** Epoch-millis -> "HH:mm" (device zone), or null when unset. */
    private fun extractTime(timestampMillis: Long?): String? =
        FleetEpoch.toValue(timestampMillis)?.toTimeString()
}

