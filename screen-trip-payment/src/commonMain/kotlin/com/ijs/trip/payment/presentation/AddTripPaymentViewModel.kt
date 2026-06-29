package com.ijs.trip.payment.presentation

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.trip.payment.TAG_ADD_PAYMENT_VM
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.datetimeutils.FleetEpoch
import com.indusjs.fleet.core.util.convertToEpochMillis
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.repository.states.StatesRepository
import com.indusjs.uicomponents.components.UiText
import com.ijs.trip.payment.domain.entity.*
import com.ijs.trip.payment.domain.repository.TripPaymentRepository
import com.ijs.trip.payment.domain.repository.TripProviderForPayment
import dev.zacsweers.metro.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.*

/**
 * ViewModel for Add/Edit Payment Screen.
 */
@Inject
class AddTripPaymentViewModel(
    private val paymentRepository: TripPaymentRepository,
    private val tripProvider: TripProviderForPayment,
    private val logger: FleetLogger,
    private val statesRepository: StatesRepository? = null
) : MviViewModel<AddPaymentContract.State, AddPaymentContract.Intent, AddPaymentContract.Effect>(AddPaymentContract.State()) {

    init {
        viewModelScope.launch {
            try {
                val labels = statesRepository?.getPaymentStatesFlat()?.toMap() ?: emptyMap()
                if (labels.isNotEmpty()) updateState { copy(paymentStateLabels = labels) }
            } catch (_: Exception) { /* fallback */ }
        }
    }

override suspend fun handleIntent(intent: AddPaymentContract.Intent) {
        when (intent) {
            is AddPaymentContract.Intent.Initialize -> initialize(intent.tripId, intent.paymentId)

            // Trip selection
            is AddPaymentContract.Intent.LoadTrips -> loadTrips()
            is AddPaymentContract.Intent.SearchTrips -> searchTrips(intent.query)
            is AddPaymentContract.Intent.SelectTrip -> {
                // Select the picked list item immediately for responsiveness, then re-fetch
                // the trip DETAIL — the trips LIST (TripListItemResponse) has no payment
                // rollup, so its received/pending are stale (Received 0 / Pending = full
                // quote). The detail carries the server-derived paid/pending + customer.
                selectTrip(intent.trip)
                loadTripAndSelect(intent.trip.id)
            }
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
            // "Received by" starts BLANK by design — the user types who actually received the
            // payment; we no longer pre-fill it with the current user's name. (Optional on submit.)
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
                logger.e(TAG_ADD_PAYMENT_VM, "Failed to load payment for edit", result.exception)
                updateState {
                    copy(
                        isLoading = false,
                        error = result.message?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.error_generic)
                    )
                }
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }

    private suspend fun loadTrips() {
        updateState { copy(isLoadingTrips = true) }

        tripProvider.getTripsForPayment().collect { result ->
            when (result) {
                is Result.Success -> {
                    updateState { copy(isLoadingTrips = false, trips = result.data) }
                }
                is Result.Error -> {
                    logger.e(TAG_ADD_PAYMENT_VM, "Failed to load trips", result.exception)
                    updateState { copy(isLoadingTrips = false) }
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    private suspend fun loadTripAndSelect(tripId: String) {
        when (val result = tripProvider.getTripForPayment(tripId)) {
            is Result.Success -> {
                selectTrip(result.data)
            }
            is Result.Error -> {
                logger.e(TAG_ADD_PAYMENT_VM, "Failed to load trip $tripId", result.exception)
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
            updateState { copy(tripError = UiText.StringRes(Res.string.error_select_trip)) }
            hasError = true
        }

        if (currentState.amount.isBlank() || currentState.amount.toDoubleOrNull()?.let { it <= 0 } == true) {
            updateState { copy(amountError = UiText.StringRes(Res.string.error_valid_amount)) }
            hasError = true
        }

        if (currentState.paymentDate.isBlank()) {
            updateState { copy(dateError = UiText.StringRes(Res.string.error_payment_date_required)) }
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
        // Convert picked DD-MM-YYYY + HH:MM to UTC epoch millis for the request.
        val paymentDateTime = convertToEpochMillis(currentState.paymentDate, currentState.paymentTime)
        if (paymentDateTime == null) {
            updateState { copy(isSaving = false, dateError = UiText.StringRes(Res.string.error_payment_date_required)) }
            return
        }

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
                sendEffect(
                    AddPaymentContract.Effect.ShowSnackbar(
                        if (currentState.isEditMode) {
                            UiText.StringRes(Res.string.success_payment_updated)
                        } else {
                            UiText.StringRes(Res.string.success_payment_recorded)
                        }
                    )
                )
                sendEffect(AddPaymentContract.Effect.PaymentSaved)
                sendEffect(AddPaymentContract.Effect.NavigateBack)
            }
            is Result.Error -> {
                logger.e(TAG_ADD_PAYMENT_VM, "Failed to save payment", result.exception)
                updateState {
                    copy(
                        isSaving = false,
                        error = result.message?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.error_generic)
                    )
                }
                sendEffect(
                    AddPaymentContract.Effect.ShowError(
                        result.message?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.error_generic)
                    )
                )
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }

    /** Epoch millis → DD-MM-YYYY for the date picker input (device zone). */
    private fun formatDateForInput(timestampMillis: Long?): String {
        if (timestampMillis == null || timestampMillis == 0L) return ""
        return FleetEpoch.toDisplayDate(timestampMillis) ?: ""
    }

    /** Epoch millis → HH:mm for the time picker input (device zone). */
    private fun formatTimeForInput(timestampMillis: Long?): String {
        if (timestampMillis == null || timestampMillis == 0L) return ""
        val value = FleetEpoch.toValue(timestampMillis) ?: return ""
        return FleetDateTime.formatTime(value)
    }
}
