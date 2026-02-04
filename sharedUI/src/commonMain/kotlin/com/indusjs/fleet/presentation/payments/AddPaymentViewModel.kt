package com.indusjs.fleet.presentation.payments

import co.touchlab.kermit.Logger
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.entity.payment.*
import com.indusjs.fleet.domain.repository.payment.TripPaymentRepository
import com.indusjs.fleet.domain.repository.trip.TripRepository
import dev.zacsweers.metro.Inject

/**
 * ViewModel for Add/Edit Payment Screen.
 */
@Inject
class AddPaymentViewModel(
    private val paymentRepository: TripPaymentRepository,
    private val tripRepository: TripRepository
) : MviViewModel<AddPaymentContract.State, AddPaymentContract.Intent, AddPaymentContract.Effect>(AddPaymentContract.State()) {

    private val log = Logger.withTag("AddPaymentViewModel")

    override suspend fun handleIntent(intent: AddPaymentContract.Intent) {
        when (intent) {
            is AddPaymentContract.Intent.Initialize -> initialize(intent.tripId, intent.paymentId)

            // Trip selection
            is AddPaymentContract.Intent.LoadTrips -> loadTrips()
            is AddPaymentContract.Intent.SearchTrips -> searchTrips(intent.query)
            is AddPaymentContract.Intent.SelectTrip -> selectTrip(intent.trip)
            is AddPaymentContract.Intent.ShowTripSelector -> updateState { copy(showTripSelector = true) }
            is AddPaymentContract.Intent.HideTripSelector -> updateState { copy(showTripSelector = false) }

            // Amount
            is AddPaymentContract.Intent.UpdateAmount -> updateState {
                copy(amount = intent.amount, amountError = null)
            }
            is AddPaymentContract.Intent.UpdateTdsAmount -> updateState { copy(tdsAmount = intent.tds) }
            is AddPaymentContract.Intent.UpdateDiscountAmount -> updateState { copy(discountAmount = intent.discount) }
            is AddPaymentContract.Intent.QuickFillFull -> quickFillFull()
            is AddPaymentContract.Intent.QuickFillHalf -> quickFillHalf()

            // Payment details
            is AddPaymentContract.Intent.UpdatePaymentType -> {
                // Clear dueDate when switching to Final/Refund
                val clearDueDate = intent.type == PaymentType.FINAL || intent.type == PaymentType.REFUND
                val currentDueDate = state.value.dueDate
                updateState {
                    copy(
                        paymentType = intent.type,
                        dueDate = if (clearDueDate) "" else currentDueDate
                    )
                }
            }
            is AddPaymentContract.Intent.UpdatePaymentMode -> updateState { copy(paymentMode = intent.mode) }
            is AddPaymentContract.Intent.UpdatePaymentDateTime -> updateState {
                copy(paymentDate = intent.date, paymentTime = intent.time, dateError = null)
            }
            is AddPaymentContract.Intent.UpdateDueDate -> updateState { copy(dueDate = intent.date) }

            // Transaction details
            is AddPaymentContract.Intent.UpdateTransactionId -> updateState { copy(transactionId = intent.id) }
            is AddPaymentContract.Intent.UpdateBankName -> updateState { copy(bankName = intent.name) }
            is AddPaymentContract.Intent.UpdatePaymentSource -> updateState { copy(paymentSource = intent.source) }

            // Additional
            is AddPaymentContract.Intent.UpdateNotes -> updateState { copy(notes = intent.notes) }
            is AddPaymentContract.Intent.UpdateReceivedBy -> updateState { copy(receivedBy = intent.name) }
            is AddPaymentContract.Intent.UpdateReceivedAtLocation -> updateState { copy(receivedAtLocation = intent.location) }

            // Actions
            is AddPaymentContract.Intent.Save -> save()
            is AddPaymentContract.Intent.Cancel -> sendEffect(AddPaymentContract.Effect.NavigateBack)
        }
    }

    private suspend fun initialize(tripId: String?, paymentId: String?) {
        updateState {
            copy(
                prefilledTripId = tripId,
                paymentId = paymentId,
                isEditMode = paymentId != null
            )
        }

        // Set default date/time to now
        val dateStr = FleetDateTime.today()
        val timeStr = FleetDateTime.currentTime()

        updateState {
            copy(
                paymentDate = dateStr,
                paymentTime = timeStr
            )
        }

        if (paymentId != null) {
            loadPaymentForEdit(paymentId)
        } else {
            loadTrips()
            if (tripId != null) {
                loadTripAndSelect(tripId)
            }
        }
    }

    private suspend fun loadPaymentForEdit(paymentId: String) {
        updateState { copy(isLoading = true) }

        when (val result = paymentRepository.getPayment(paymentId)) {
            is Result.Success -> {
                val payment = result.data
                updateState {
                    copy(
                        isLoading = false,
                        amount = payment.amount.toString(),
                        tdsAmount = if (payment.tdsAmount > 0) payment.tdsAmount.toString() else "",
                        discountAmount = if (payment.discountAmount > 0) payment.discountAmount.toString() else "",
                        paymentType = payment.paymentType,
                        paymentMode = payment.paymentMode,
                        paymentDate = formatDateForInput(payment.paymentDate),
                        paymentTime = formatTimeForInput(payment.paymentDate),
                        transactionId = payment.transactionId ?: "",
                        bankName = payment.bankName ?: "",
                        paymentSource = payment.paymentSource ?: "",
                        notes = payment.notes ?: "",
                        receivedBy = payment.receivedBy ?: "",
                        receivedAtLocation = payment.receivedAtLocation ?: "",
                        customerName = payment.customerName ?: "",
                        customerContact = payment.customerContact ?: "",
                        customerCompany = payment.customerCompany ?: "",
                        customerGst = payment.customerGst ?: "",
                        customerId = payment.customerId
                    )
                }
                // Load trip info
                loadTripAndSelect(payment.tripId)
            }
            is Result.Error -> {
                log.e(result.exception) { "Failed to load payment for edit" }
                updateState { copy(isLoading = false, error = result.message) }
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }

    private suspend fun loadTrips() {
        updateState { copy(isLoadingTrips = true) }

        tripRepository.getTrips().collect { result ->
            when (result) {
                is Result.Success -> {
                    val tripSummaries = result.data.map { trip ->
                        // Use the FleetDateTime utility for proper date extraction (same as TripCostEntryScreen)
                        val startDateSource = trip.plannedStart ?: trip.scheduledDate
                        val endDateSource = trip.plannedEnd ?: trip.deliveryDate

                        TripSummaryForPayment(
                            id = trip.id,
                            vehicleId = trip.vehicleId,
                            driverId = trip.driverId,
                            vehicleRegistration = trip.vehicleNumber ?: "N/A",
                            driverName = trip.driverName,
                            startLocation = trip.startLocation?.address ?: "Unknown",
                            endLocation = trip.endLocation?.address ?: "Unknown",
                            tripPrice = trip.tripPrice ?: 0.0,
                            paidAmount = trip.paidTripPrice ?: 0.0,
                            pendingAmount = (trip.tripPrice ?: 0.0) - (trip.paidTripPrice ?: 0.0),
                            customerId = trip.customerId,
                            customerName = trip.customerName,
                            customerContact = trip.customerContact,
                            state = trip.status.name,
                            scheduledDate = startDateSource,  // Pass the source for date calculation in screen
                            paymentStatus = trip.paymentStatus,
                            tripStartDate = FleetDateTime.getMinDateForTripCost(startDateSource),
                            tripEndDate = FleetDateTime.getMinDateForTripCost(endDateSource),
                            tripStartTime = extractTimeFromIso(trip.plannedStart),
                            tripEndTime = extractTimeFromIso(trip.plannedEnd)
                        )
                    }
                    updateState { copy(isLoadingTrips = false, trips = tripSummaries) }
                }
                is Result.Error -> {
                    log.e(result.exception) { "Failed to load trips" }
                    updateState { copy(isLoadingTrips = false) }
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    private suspend fun loadTripAndSelect(tripId: String) {
        when (val result = tripRepository.getTripById(tripId)) {
            is Result.Success -> {
                val trip = result.data
                // Use the FleetDateTime utility for proper date extraction (same as TripCostEntryScreen)
                val startDateSource = trip.plannedStart ?: trip.scheduledDate
                val endDateSource = trip.plannedEnd ?: trip.deliveryDate

                val summary = TripSummaryForPayment(
                    id = trip.id,
                    vehicleId = trip.vehicleId,
                    driverId = trip.driverId,
                    vehicleRegistration = trip.vehicleNumber ?: "N/A",
                    driverName = trip.driverName,
                    startLocation = trip.startLocation?.address ?: "Unknown",
                    endLocation = trip.endLocation?.address ?: "Unknown",
                    tripPrice = trip.tripPrice ?: 0.0,
                    paidAmount = trip.paidTripPrice ?: 0.0,
                    pendingAmount = (trip.tripPrice ?: 0.0) - (trip.paidTripPrice ?: 0.0),
                    customerId = trip.customerId,
                    customerName = trip.customerName,
                    customerContact = trip.customerContact,
                    state = trip.status.name,
                    scheduledDate = startDateSource,  // Pass the source for date calculation in screen
                    paymentStatus = trip.paymentStatus,
                    tripStartDate = FleetDateTime.getMinDateForTripCost(startDateSource),
                    tripEndDate = FleetDateTime.getMinDateForTripCost(endDateSource),
                    tripStartTime = extractTimeFromIso(trip.plannedStart),
                    tripEndTime = extractTimeFromIso(trip.plannedEnd)
                )
                selectTrip(summary)
            }
            is Result.Error -> {
                log.e(result.exception) { "Failed to load trip $tripId" }
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }

    private fun searchTrips(query: String) {
        updateState { copy(tripSearchQuery = query) }
        // Filter trips based on query
        // The actual filtering happens in the UI based on tripSearchQuery
    }

    private fun selectTrip(trip: TripSummaryForPayment) {
        updateState {
            copy(
                selectedTrip = trip,
                showTripSelector = false,
                tripError = null,
                customerName = trip.customerName ?: "",
                customerContact = trip.customerContact ?: "",
                customerId = trip.customerId
            )
        }
    }

    private fun quickFillFull() {
        val pending = state.value.selectedTrip?.pendingAmount ?: return
        updateState { copy(amount = pending.toString()) }
    }

    private fun quickFillHalf() {
        val pending = state.value.selectedTrip?.pendingAmount ?: return
        updateState { copy(amount = (pending / 2).toString()) }
    }

    private suspend fun save() {
        // Validate
        var hasError = false
        val currentState = state.value

        if (currentState.selectedTrip == null) {
            updateState { copy(tripError = "Please select a trip") }
            hasError = true
        }

        if (currentState.amount.isBlank() || currentState.amount.toDoubleOrNull()?.let { it <= 0 } == true) {
            updateState { copy(amountError = "Enter a valid amount") }
            hasError = true
        }

        if (currentState.paymentDate.isBlank()) {
            updateState { copy(dateError = "Select payment date") }
            hasError = true
        }

        if (hasError) return

        updateState { copy(isSaving = true, error = null) }

        val tripId = currentState.selectedTrip!!.id.toIntOrNull() ?: return
        val vehicleId = currentState.selectedTrip!!.vehicleId.toIntOrNull() ?: return
        val driverId = currentState.selectedTrip!!.driverId.toIntOrNull() ?: return
        val customerId = currentState.selectedTrip!!.customerId?.toIntOrNull()
        val amount = currentState.amount.toDoubleOrNull() ?: return
        val tds = currentState.tdsAmount.toDoubleOrNull() ?: 0.0
        val discount = currentState.discountAmount.toDoubleOrNull() ?: 0.0
        val paymentDateTime = formatDateTimeForApi(currentState.paymentDate, currentState.paymentTime)

        val result = if (currentState.isEditMode && currentState.paymentId != null) {
            paymentRepository.updatePayment(
                paymentId = currentState.paymentId,
                amount = amount,
                tdsAmount = tds,
                discountAmount = discount,
                paymentType = currentState.paymentType,
                paymentMode = currentState.paymentMode,
                paymentSource = currentState.paymentSource.ifBlank { null },
                paymentDate = paymentDateTime,
                transactionId = currentState.transactionId.ifBlank { null },
                bankName = currentState.bankName.ifBlank { null },
                notes = currentState.notes.ifBlank { null },
                receivedBy = currentState.receivedBy.ifBlank { null },
                receivedAtLocation = currentState.receivedAtLocation.ifBlank { null }
            )
        } else {
            paymentRepository.addPaymentToTrip(
                tripId = tripId,
                vehicleId = vehicleId,
                driverId = driverId,
                customerId = customerId,
                amount = amount,
                tdsAmount = tds,
                discountAmount = discount,
                paymentType = currentState.paymentType,
                paymentMode = currentState.paymentMode,
                paymentSource = currentState.paymentSource.ifBlank { null },
                paymentDate = paymentDateTime,
                transactionId = currentState.transactionId.ifBlank { null },
                bankName = currentState.bankName.ifBlank { null },
                notes = currentState.notes.ifBlank { null },
                receivedBy = currentState.receivedBy.ifBlank { null },
                receivedAtLocation = currentState.receivedAtLocation.ifBlank { null }
            )
        }

        when (result) {
            is Result.Success -> {
                updateState { copy(isSaving = false) }
                sendEffect(AddPaymentContract.Effect.ShowSnackbar(if (currentState.isEditMode) "Payment updated" else "Payment recorded"))
                sendEffect(AddPaymentContract.Effect.PaymentSaved)
                sendEffect(AddPaymentContract.Effect.NavigateBack)
            }
            is Result.Error -> {
                log.e(result.exception) { "Failed to save payment" }
                updateState { copy(isSaving = false, error = result.message) }
                sendEffect(AddPaymentContract.Effect.ShowError(result.message ?: "Failed to save payment"))
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }

    private fun formatDateForInput(isoDate: String?): String {
        if (isoDate == null) return ""
        return try {
            val parts = isoDate.take(10).split("-")
            if (parts.size == 3) {
                "${parts[2]}-${parts[1]}-${parts[0]}"
            } else {
                isoDate.take(10)
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun formatTimeForInput(isoDate: String?): String {
        if (isoDate == null) return ""
        return try {
            isoDate.substring(11, 16)
        } catch (e: Exception) {
            ""
        }
    }

    private fun formatDateTimeForApi(date: String, time: String): String {
        // Convert DD-MM-YYYY HH:MM to ISO 8601
        return try {
            val dateParts = date.split("-")
            if (dateParts.size == 3) {
                val isoDate = "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}"
                val timeStr = if (time.isNotBlank()) time else "00:00"
                "${isoDate}T${timeStr}:00Z"
            } else {
                "${date}T${time}:00Z"
            }
        } catch (e: Exception) {
            "${date}T${time}:00Z"
        }
    }

    /**
     * Extract date in DD-MM-YYYY format from ISO 8601 datetime string.
     * Falls back to the input if it's already in DD-MM-YYYY format.
     */
    private fun extractDateFromIso(isoOrDate: String?): String? {
        if (isoOrDate.isNullOrBlank()) return null

        return try {
            // Try parsing as ISO 8601 first
            val parsed = FleetDateTime.fromIso8601(isoOrDate)
            if (parsed != null) {
                FleetDateTime.formatDate(parsed)
            } else {
                // Might already be in DD-MM-YYYY format
                isoOrDate
            }
        } catch (e: Exception) {
            isoOrDate
        }
    }

    private fun extractTimeFromIso(isoDate: String?): String? {
        if (isoDate.isNullOrBlank()) return null

        return try {
            // Extract time portion (HH:mm) from ISO 8601 string
            isoDate.substring(11, 16)
        } catch (e: Exception) {
            null
        }
    }
}
