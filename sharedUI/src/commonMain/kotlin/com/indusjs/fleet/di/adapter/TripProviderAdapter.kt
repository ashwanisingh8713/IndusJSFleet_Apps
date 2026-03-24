package com.indusjs.fleet.di.adapter

import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.error.result.Result
import com.ijs.payment.domain.repository.TripProviderForPayment
import com.ijs.payment.presentation.TripSummaryForPayment
import com.ijs.trip.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Adapter that bridges [TripRepository] (feat-trip) to [TripProviderForPayment] (feat-payment).
 * Breaks the circular dependency between feat-trip ↔ feat-payment.
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
        val startDateSource = plannedStart ?: scheduledDate
        val endDateSource = plannedEnd ?: deliveryDate

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
            pendingAmount = (tripPrice ?: 0.0) - (paidTripPrice ?: 0.0),
            customerId = customerId,
            customerName = customerName,
            customerContact = customerContact,
            state = status.name,
            scheduledDate = startDateSource,
            paymentStatus = paymentStatus,
            tripStartDate = FleetDateTime.getMinDateForTripCost(startDateSource),
            tripEndDate = FleetDateTime.getMinDateForTripCost(endDateSource),
            tripStartTime = extractTimeFromIso(plannedStart),
            tripEndTime = extractTimeFromIso(plannedEnd)
        )
    }

    private fun extractTimeFromIso(isoDate: String?): String? {
        if (isoDate.isNullOrBlank()) return null
        return try {
            // Extract time portion (HH:mm) from ISO 8601 string
            isoDate.substring(11, 16)
        } catch (_: Exception) {
            null
        }
    }
}

