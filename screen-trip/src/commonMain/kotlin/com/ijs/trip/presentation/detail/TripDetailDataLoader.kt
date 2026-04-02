package com.ijs.trip.presentation.detail

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.trip.TAG_TRIP_DETAIL_LOADER
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.error.result.Result
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.ijs.trip.presentation.detail.TripDetailContract.Effect
import com.ijs.customer.domain.entity.Customer
import com.ijs.customer.domain.repository.CustomerRepository
import com.ijs.driver.domain.entity.DriverStatus
import com.ijs.driver.domain.usecase.GetDriversUseCase
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
    private val userLocalDataSource: UserLocalDataSource,
    private val customerRepository: CustomerRepository?,
    private val tripPaymentRepository: TripPaymentRepository?,
    private val logger: FleetLogger
) {
suspend fun loadTrip(tripId: String) {
        stateManager.updateTripState { copy(isLoading = true, error = null, tripId = tripId) }

        withContext(dispatcherProvider.io) {
            // Load user role for permission check
            val userRole = try {
                userLocalDataSource.getUserRole() ?: ""
            } catch (e: Exception) {
                logger.e(TAG_TRIP_DETAIL_LOADER, "Failed to get user role: ${e.message}")
                ""
            }
            val normalizedRole = userRole.lowercase().replace("_", "")
            logger.d(TAG_TRIP_DETAIL_LOADER, "TripDetailDataLoader - userRole: '$userRole', normalized: '$normalizedRole'")
            stateManager.updateTripState { copy(userRole = userRole) }

            when (val result = getTripByIdUseCase(tripId)) {
                is Result.Success -> {
                    val trip = result.data

                    // Parse departure date/time from ISO or separate fields
                    val (depDate, depTime) = parseScheduleDateTime(
                        isoDateTime = trip.plannedStart,
                        date = trip.scheduledDate,
                        time = trip.startTime
                    )

                    // Parse arrival date/time from ISO or separate fields
                    val (arrDate, arrTime) = parseScheduleDateTime(
                        isoDateTime = trip.plannedEnd,
                        date = trip.deliveryDate,
                        time = trip.deliveryTime
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
                            tripPrice = trip.tripPrice?.toString() ?: ""
                        )
                    }
                    // Load trip costs
                    loadTripCosts(tripId)
                    // Load trip payments (Owner/GM only can see pricing)
                    loadTripPayments(tripId)
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
        stateManager.updateTripState { copy(isLoadingCosts = true) }

        when (val result = costsRepository.getTripCosts(tripId)) {
            is Result.Success -> {
                val costs = result.data
                val total = costs.sumOf { it.amount }
                val byType = costs.groupBy { it.costType }
                stateManager.updateTripState {
                    copy(costs = costs, totalCost = total, costsByType = byType, isLoadingCosts = false)
                }
            }
            is Result.Error -> {
                stateManager.updateTripState { copy(isLoadingCosts = false) }
            }
            is Result.Loading -> { /* Not applicable */ }
        }
    }

    /**
     * Load payments for a specific trip.
     * Only loads if the user has permission to view pricing (Owner/GM).
     */
    suspend fun loadTripPayments(tripId: String) {
        if (tripPaymentRepository == null) {
            logger.d(TAG_TRIP_DETAIL_LOADER, "TripPaymentRepository not available, skipping payment load")
            return
        }

        // Only load payments for users who can view pricing
        val state = stateManager.currentTripState
        if (!state.canViewTripPrice) {
            logger.d(TAG_TRIP_DETAIL_LOADER, "User role '${state.userRole}' cannot view payments, skipping")
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
                    val tripPrice = state.trip?.tripPrice ?: 0.0
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
     * Parses schedule date/time from ISO format or separate date/time fields.
     * Returns a Pair of (date in DD-MM-YYYY format, time in HH:MM format).
     * FleetDateTimePicker expects DD-MM-YYYY and HH:MM with delimiters.
     */
    fun parseScheduleDateTime(
        isoDateTime: String?,
        date: String?,
        time: String?
    ): Pair<String, String> {
        if (!isoDateTime.isNullOrBlank()) {
            try {
                val parts = isoDateTime.replace("Z", "").split("T")
                if (parts.size == 2) {
                    val datePart = parts[0]
                    val timePart = parts[1]

                    val dateComponents = datePart.split("-")
                    if (dateComponents.size == 3) {
                        // Return DD-MM-YYYY (with hyphens)
                        val formattedDate = "${dateComponents[2]}-${dateComponents[1]}-${dateComponents[0]}"
                        val timeComponents = timePart.split(":")
                        if (timeComponents.size >= 2) {
                            // Return HH:MM (with colon)
                            val formattedTime = "${timeComponents[0]}:${timeComponents[1]}"
                            return Pair(formattedDate, formattedTime)
                        }
                    }
                }
            } catch (_: Exception) {
                logger.e(TAG_TRIP_DETAIL_LOADER, "Failed to parse ISO datetime: $isoDateTime")
            }
        }

        // Fallback: preserve delimiters from the original date/time fields
        val fallbackDate = date?.trim() ?: ""
        val fallbackTime = time?.trim() ?: ""
        return Pair(fallbackDate, fallbackTime)
    }
}

