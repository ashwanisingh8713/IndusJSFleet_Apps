package com.ijs.trip.presentation.detail

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.trip.TAG_TRIP_DETAIL_LOADER
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.permission.PermissionChecker
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.ijs.trip.presentation.detail.TripDetailContract.Effect
import com.ijs.customer.domain.entity.Customer
import com.ijs.customer.domain.repository.CustomerRepository
import com.ijs.driver.domain.entity.DriverStatus
import com.ijs.driver.domain.usecase.GetDriversUseCase
import com.ijs.trip.domain.entity.Trip
import com.ijs.trip.payment.domain.entity.TripPaymentSummary
import com.ijs.trip.payment.domain.repository.TripPaymentRepository
import com.ijs.trip.domain.usecase.GetTripByIdUseCase
import com.ijs.vehicle.domain.entity.VehicleStatus
import com.ijs.vehicle.domain.usecase.GetVehiclesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Handles data loading operations for the Trip Detail screen.
 * Responsible for loading trip details, costs, vehicles, drivers, and customers.
 */
internal class TripDetailDataLoader(
    private val stateManager: TripDetailStateManager,
    private val dispatcherProvider: DispatcherProvider,
    private val scope: CoroutineScope,
    private val getTripByIdUseCase: GetTripByIdUseCase,
    private val costsRepository: CostsRepository,
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val getDriversUseCase: GetDriversUseCase,
    private val permissionChecker: PermissionChecker,
    private val customerRepository: CustomerRepository?,
    private val tripPaymentRepository: TripPaymentRepository?,
    private val logger: FleetLogger
) {
    suspend fun loadTrip(tripId: String, isSilent: Boolean = false) {
        if (!isSilent) {
            stateManager.updateTripState { copy(isLoading = true, error = null, tripId = tripId) }
        } else {
            stateManager.updateTripState { copy(error = null, tripId = tripId) }
        }

        withContext(dispatcherProvider.io) {
            // Compute permission flags from the user's actual permission set
            stateManager.updateTripState {
                copy(
                    canEditTripPermission = permissionChecker.canEditTrip(),
                    canViewTripPricePermission = permissionChecker.canViewTripPrice()
                )
            }

            when (val result = getTripByIdUseCase(tripId)) {
                is Result.Success -> {
                    val trip = result.data

                    // Derive picker date/time from epoch-millis schedule fields
                    val (depDate, depTime) = parseScheduleDateTime(
                        timestampMs = trip.plannedStart ?: trip.scheduledDate,
                        fallbackTimeMs = trip.startTime
                    )

                    val (arrDate, arrTime) = parseScheduleDateTime(
                        timestampMs = trip.plannedEnd ?: trip.deliveryDate,
                        fallbackTimeMs = trip.deliveryTime
                    )

                    stateManager.updateTripState {
                        copy(
                            isLoading = false,
                            trip = trip,
                            startLocationAddress = trip.startLocation?.address ?: "",
                            startLat = trip.startLocation?.latitude?.toString() ?: "",
                            startLng = trip.startLocation?.longitude?.toString() ?: "",
                            endLocationAddress = trip.endLocation?.address ?: "",
                            endLat = trip.endLocation?.latitude?.toString() ?: "",
                            endLng = trip.endLocation?.longitude?.toString() ?: "",
                            estimatedDistance = trip.distance.takeIf { it > 0 }?.toString() ?: "",
                            estimatedDuration = trip.displayInfo.durationValue.takeIf { it != "NA" } ?: "",
                            departureDate = depDate,
                            departureTime = depTime,
                            arrivalDate = arrDate,
                            arrivalTime = arrTime,
                            cargoType = trip.cargoType ?: "",
                            cargoDescription = trip.cargoDescription ?: "",
                            cargoWeight = trip.cargoLoadingWeight?.toString() ?: "",
                            weightUnit = trip.weightUnit ?: "KG",
                            customerName = trip.customerName ?: "",
                            priority = trip.priority ?: "",
                            notes = trip.notes ?: "",
                            tripPrice = trip.tripPrice?.toString() ?: "",
                            // Actual price (revenue) defaults to the quote when unset; COGS.
                            actualPrice = (trip.sellingValue ?: trip.tripPrice)?.toString() ?: "",
                            purchasePrice = trip.purchasePrice?.toString() ?: ""
                        )
                    }
                    // Load trip costs
                    loadTripCosts(tripId)
                    // Load trip payments (Owner/GM only can see pricing)
                    loadTripPayments(tripId, trip)
                }
                is Result.Error -> {
                    stateManager.updateTripState {
                        copy(isLoading = false, error = result.message ?: "Failed to load trip")
                    }
                    stateManager.emitEffect(Effect.ShowError(result.message ?: "Failed to load trip"))
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    suspend fun loadTripCosts(tripId: String) {
        stateManager.updateTripState { copy(isLoadingCosts = true, costsError = null) }

        when (val result = costsRepository.getTripCosts(tripId)) {
            is Result.Success -> {
                val costs = result.data
                val total = costs.sumOf { it.amount }
                val byType = costs.groupBy { it.costType }
                stateManager.updateTripState {
                    copy(costs = costs, totalCost = total, costsByType = byType, isLoadingCosts = false, costsError = null)
                }
            }
            is Result.Error -> {
                // Surface the failure instead of falling through to the empty state, which
                // would hide a real backend error (e.g. trip-costs 500) and make a just-added
                // cost look like it silently vanished.
                logger.e(TAG_TRIP_DETAIL_LOADER, "Failed to load trip costs: ${result.message}")
                stateManager.updateTripState {
                    copy(isLoadingCosts = false, costsError = result.message ?: "Failed to load costs")
                }
            }
            is Result.Loading -> { /* Not applicable */ }
        }
    }

    /**
     * Load payments for a specific trip.
     * Only loads if the user has permission to view pricing (Owner/GM).
     */
    suspend fun loadTripPayments(tripId: String, trip: Trip) {
        if (tripPaymentRepository == null) {
            logger.d(TAG_TRIP_DETAIL_LOADER, "TripPaymentRepository not available, skipping payment load")
            return
        }

        // Only load payments for users who can view pricing
        if (!permissionChecker.canViewTripPrice()) {
            logger.d(TAG_TRIP_DETAIL_LOADER, "User lacks financials:read permission, skipping payment load")
            return
        }

        stateManager.updateTripState { copy(isLoadingPayments = true) }

        withContext(dispatcherProvider.io) {
            when (val result = tripPaymentRepository.getTripPayments(tripId)) {
                is Result.Success -> {
                    val paymentResult = result.data
                    val payments = paymentResult.payments
                    val totalPaid = payments
                        .filter { it.isReceived }
                        .sumOf { it.netAmount }
                    val tripPrice = trip.tripPrice ?: 0.0
                    val pending = (tripPrice - totalPaid).coerceAtLeast(0.0)

                    stateManager.updateTripState {
                        copy(
                            payments = payments,
                            paymentSummary = paymentResult.summary ?: TripPaymentSummary(
                                totalReceived = totalPaid,
                                totalPending = pending,
                                paymentCount = payments.size,
                                receivedCount = payments.count { it.isReceived }
                            ),
                            totalPaid = totalPaid,
                            pendingAmount = pending,
                            isLoadingPayments = false
                        )
                    }
                    logger.d(TAG_TRIP_DETAIL_LOADER, "Loaded ${payments.size} payments for trip $tripId")
                }
                is Result.Error -> {
                    stateManager.updateTripState { copy(isLoadingPayments = false) }
                    logger.e(TAG_TRIP_DETAIL_LOADER, "Failed to load trip payments: ${result.message}")
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    suspend fun loadVehiclesAndDrivers() {
        val trip = stateManager.currentTripState.trip
        val currentVehicleId = trip?.vehicleId
        val currentDriverId = trip?.driverId

        withContext(dispatcherProvider.io) {
            getVehiclesUseCase().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val availableVehicles = result.data.filter { vehicle ->
                            VehicleStatus.isAvailableForAssignment(vehicle.status) || vehicle.id == currentVehicleId
                        }
                        val currentVehicle = availableVehicles.find { it.id == currentVehicleId }
                        stateManager.updateTripState {
                            copy(vehicles = availableVehicles, selectedVehicle = currentVehicle)
                        }
                    }
                    is Result.Error -> logger.e(TAG_TRIP_DETAIL_LOADER, "Failed to load vehicles: ${result.message}")
                    is Result.Loading -> { /* Already handled */ }
                }
            }
        }

        withContext(dispatcherProvider.io) {
            getDriversUseCase().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val availableDrivers = result.data.filter { driver ->
                            (DriverStatus.isAvailableForAssignment(driver.status) && driver.isActive) || driver.id == currentDriverId
                        }
                        val currentDriver = availableDrivers.find { it.id == currentDriverId }
                        stateManager.updateTripState {
                            copy(
                                drivers = availableDrivers,
                                selectedDriver = currentDriver,
                                isLoadingVehiclesDrivers = false
                            )
                        }
                    }
                    is Result.Error -> {
                        stateManager.updateTripState { copy(isLoadingVehiclesDrivers = false) }
                        logger.e(TAG_TRIP_DETAIL_LOADER, "Failed to load drivers: ${result.message}")
                    }
                    is Result.Loading -> { /* Already handled */ }
                }
            }
        }
    }

    /**
     * Load customers from local database for customer selection.
     */
    suspend fun loadCustomers() {
        if (customerRepository == null) {
            logger.w(TAG_TRIP_DETAIL_LOADER, "CustomerRepository not available")
            return
        }

        stateManager.updateTripState { copy(isLoadingCustomers = true) }

        withContext(dispatcherProvider.io) {
            try {
                val localCustomers = customerRepository.getLocalCustomers()
                val activeCustomers = localCustomers.filter { it.isActive }
                    .map { summary ->
                        Customer(
                            id = summary.id,
                            companyName = summary.companyName,
                            personName = summary.personName,
                            primaryContact = summary.primaryContact,
                            secondaryContact = null,
                            companyAddress = null,
                            email = null,
                            gstNumber = null,
                            notes = null,
                            isActive = summary.isActive,
                            createdAt = null,
                            updatedAt = null
                        )
                    }
                stateManager.updateTripState {
                    copy(customers = activeCustomers, isLoadingCustomers = false)
                }
                logger.d(TAG_TRIP_DETAIL_LOADER, "Loaded ${activeCustomers.size} customers from local cache")
            } catch (e: Exception) {
                stateManager.updateTripState { copy(isLoadingCustomers = false) }
                logger.e(TAG_TRIP_DETAIL_LOADER, "Failed to load customers: ${e.message}")
            }
        }
    }

    /**
     * Refresh customers from API and update local DB.
     */
    fun refreshCustomers() {
        val repository = customerRepository ?: run {
            logger.e(TAG_TRIP_DETAIL_LOADER, "CustomerRepository not available")
            stateManager.emitEffect(Effect.ShowError("Customer feature not available"))
            return
        }

        scope.launch {
            stateManager.updateTripState { copy(isRefreshingCustomers = true) }
            try {
                when (val result = repository.refreshCustomers()) {
                    is Result.Success -> {
                        loadCustomers()
                        stateManager.emitEffect(Effect.ShowSnackbar("Customers refreshed"))
                    }
                    is Result.Error -> {
                        stateManager.emitEffect(
                            Effect.ShowError("Failed to refresh customers: ${result.message}")
                        )
                    }
                    is Result.Loading -> { /* Already handled */ }
                }
            } catch (e: Exception) {
                logger.e(TAG_TRIP_DETAIL_LOADER, "Failed to refresh customers: ${e.message}")
                stateManager.emitEffect(Effect.ShowError("Failed to refresh customers"))
            } finally {
                stateManager.updateTripState { copy(isRefreshingCustomers = false) }
            }
        }
    }

    /**
     * Derives the picker's date (DD-MM-YYYY) and time (HH:MM, 24-hour) strings
     * from a UTC epoch-millis timestamp (device-zone wall clock). Treats null/0
     * as unset. [fallbackTimeMs] supplies the time-of-day if [timestampMs] is a
     * date-only value (its own clock is 00:00).
     */
    fun parseScheduleDateTime(
        timestampMs: Long?,
        fallbackTimeMs: Long? = null
    ): Pair<String, String> {
        val ms = timestampMs?.takeIf { it > 0L } ?: return Pair("", "")
        val value = com.indusjs.datetimeutils.FleetEpoch.toValue(ms)
            ?: return Pair("", "")

        val day = value.day.toString().padStart(2, '0')
        val month = value.month.toString().padStart(2, '0')
        val formattedDate = "$day-$month-${value.year}"

        // Use the timestamp's own time-of-day; if it's exactly midnight and a
        // separate time field exists, fall back to that.
        val timeSource = if (value.hour == 0 && value.minute == 0) {
            com.indusjs.datetimeutils.FleetEpoch.toValue(fallbackTimeMs?.takeIf { it > 0L }) ?: value
        } else {
            value
        }
        val hour = timeSource.hour.toString().padStart(2, '0')
        val minute = timeSource.minute.toString().padStart(2, '0')
        val formattedTime = "$hour:$minute"

        return Pair(formattedDate, formattedTime)
    }
}

