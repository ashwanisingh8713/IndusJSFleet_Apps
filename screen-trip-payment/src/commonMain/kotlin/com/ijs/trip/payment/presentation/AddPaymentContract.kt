package com.ijs.trip.payment.presentation

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.core.util.formatCurrencyFull
import com.indusjs.uicomponents.components.UiText
import com.ijs.trip.payment.domain.entity.*

/**
 * MVI Contract for Add/Edit Payment Screen.
 */
object AddPaymentContract {

    data class State(
        // Mode
        val isEditMode: Boolean = false,
        val paymentId: String? = null,
        val prefilledTripId: String? = null,

        // Loading states
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val isLoadingTrips: Boolean = false,

        // Trip selection
        val trips: List<TripSummaryForPayment> = emptyList(),
        val selectedTrip: TripSummaryForPayment? = null,
        val tripSearchQuery: String = "",
        val showTripSelector: Boolean = false,

        // Customer info (auto-filled from trip or manual)
        val customerId: String? = null,
        val customerName: String = "",
        val customerContact: String = "",
        val customerCompany: String = "",
        val customerGst: String = "",

        // Amount details
        val amount: String = "",
        val tdsAmount: String = "",
        val discountAmount: String = "",
        /** In edit mode, the amount this payment already contributes to the trip. It is added back to
         *  the cap so an existing payment can be adjusted up to (but not past) the trip price. 0.0 in add mode. */
        val originalAmount: Double = 0.0,

        // Payment details
        val paymentType: PaymentType = PaymentType.PARTIAL,
        val paymentMode: PaymentMode = PaymentMode.CASH,
        val paymentDate: String = "",
        val paymentTime: String = "",
        val dueDate: String = "",  // For Advance/Partial payments

        // Transaction details (for bank/UPI)
        val transactionId: String = "",
        val bankName: String = "",
        val paymentSource: String = "",

        // Additional info
        val notes: String = "",
        val receivedBy: String = "",
        val receivedAtLocation: String = "",

        // Errors
        val error: UiText? = null,
        val amountError: UiText? = null,
        val tripError: UiText? = null,
        val dateError: UiText? = null,

        /** DB-cached payment state labels (apiValue -> displayLabel) */
        val paymentStateLabels: Map<String, String> = emptyMap()
    ) : UiState {

        val netAmount: Double
            get() {
                val amt = amount.toDoubleOrNull() ?: 0.0
                val tds = tdsAmount.toDoubleOrNull() ?: 0.0
                val discount = discountAmount.toDoubleOrNull() ?: 0.0
                return amt - tds - discount
            }

        val netAmountDisplay: String
            get() = formatCurrencyFull(netAmount)

        val pendingAmount: Double
            get() = selectedTrip?.pendingAmount ?: 0.0

        val pendingAmountDisplay: String
            get() = formatCurrencyFull(pendingAmount)

        /** The most this payment may be: the trip's pending balance (+ this payment's own amount when
         *  editing). Recording more would push total collected past the trip price. */
        val maxPayableAmount: Double
            get() = pendingAmount + originalAmount

        val maxPayableAmountDisplay: String
            get() = formatCurrencyFull(maxPayableAmount)

        /** True when a (non-refund) payment would collect more than the trip price. Refunds are exempt
         *  (money out / overpayment handling), and it stays false until a trip is selected. */
        val amountExceedsPayable: Boolean
            get() {
                if (selectedTrip == null || paymentType == PaymentType.REFUND) return false
                val entered = amount.toDoubleOrNull() ?: return false
                return entered - maxPayableAmount > 0.01
            }

        val canSave: Boolean
            get() {
                val baseValidation = selectedTrip != null &&
                        amount.isNotBlank() &&
                        amount.toDoubleOrNull()?.let { it > 0 } == true &&
                        !amountExceedsPayable &&
                        paymentDate.isNotBlank() &&
                        !isSaving

                // Additional validation based on payment mode
                val transactionValid = when (paymentMode) {
                    PaymentMode.UPI -> transactionId.isNotBlank() // UPI ID required
                    PaymentMode.BANK_TRANSFER -> bankName.isNotBlank() && paymentSource.isNotBlank() // Bank & Account required
                    else -> true // No additional requirements for Cash, Card, Credit
                }

                return baseValidation && transactionValid
            }

        val showTransactionFields: Boolean
            get() = paymentMode == PaymentMode.BANK_TRANSFER || paymentMode == PaymentMode.UPI

        val showDueDate: Boolean
            get() = paymentType == PaymentType.ADVANCE || paymentType == PaymentType.PARTIAL

        /**
         * Minimum date for payment and due date (Trip Start Date).
         * Uses trip's scheduled date in DD-MM-YYYY format.
         */
        val minPaymentDate: String?
            get() = selectedTrip?.tripStartDate

        /**
         * Maximum date for payment date (Current Date + 1 day).
         * Uses FleetDateTime utility for cross-platform date calculation.
         */
        val maxPaymentDate: String
            get() = com.indusjs.datetimeutils.FleetDateTime.getDateFromToday(1)
    }

    sealed interface Intent : UiIntent {
        data class Initialize(val tripId: String? = null, val paymentId: String? = null) : Intent

        // Trip selection
        data object LoadTrips : Intent
        data class SearchTrips(val query: String) : Intent
        data class SelectTrip(val trip: TripSummaryForPayment) : Intent
        data object ShowTripSelector : Intent
        data object HideTripSelector : Intent

        // Amount
        data class UpdateAmount(val amount: String) : Intent
        data class UpdateTdsAmount(val tds: String) : Intent
        data class UpdateDiscountAmount(val discount: String) : Intent
        data object QuickFillFull : Intent
        data object QuickFillHalf : Intent

        // Payment details
        data class UpdatePaymentType(val type: PaymentType) : Intent
        data class UpdatePaymentMode(val mode: PaymentMode) : Intent
        data class UpdatePaymentDateTime(val date: String, val time: String) : Intent
        data class UpdateDueDate(val date: String) : Intent

        // Transaction details
        data class UpdateTransactionId(val id: String) : Intent
        data class UpdateBankName(val name: String) : Intent
        data class UpdatePaymentSource(val source: String) : Intent

        // Additional
        data class UpdateNotes(val notes: String) : Intent
        data class UpdateReceivedBy(val name: String) : Intent
        data class UpdateReceivedAtLocation(val location: String) : Intent

        // Actions
        data object Save : Intent
        data object Cancel : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class ShowSnackbar(val message: UiText) : Effect
        data class ShowError(val message: UiText) : Effect
        data object PaymentSaved : Effect
    }
}

/**
 * Trip summary for payment selection.
 */
data class TripSummaryForPayment(
    val id: String,
    val vehicleId: String,
    val driverId: String,
    val vehicleRegistration: String,
    val driverName: String?,
    val startLocation: String,
    val endLocation: String,
    val tripPrice: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val customerId: String?,
    val customerName: String?,
    val customerContact: String?,
    val state: String?,
    // Additional fields for better trip selection
    val scheduledDate: String? = null,
    val paymentStatus: String? = null,  // pending, partial, paid
    // Trip start/end date for date validation and display (DD-MM-YYYY format)
    val tripStartDate: String? = null,
    val tripEndDate: String? = null,
    // Trip start/end time (HH:mm format)
    val tripStartTime: String? = null,
    val tripEndTime: String? = null
) {
    val routeDisplay: String
        get() = "${startLocation.take(15)} → ${endLocation.take(15)}"

    val routeDisplayFull: String
        get() = "$startLocation → $endLocation"

    val tripPriceDisplay: String
        get() = formatCurrencyFull(tripPrice)

    val paidAmountDisplay: String
        get() = formatCurrencyFull(paidAmount)

    val pendingDisplay: String
        get() = formatCurrencyFull(pendingAmount)

    val displayText: String
        get() = "#$id • $vehicleRegistration • $routeDisplay"

    val hasPendingAmount: Boolean
        get() = pendingAmount > 0

    val isFullyPaid: Boolean
        get() = pendingAmount <= 0 || paymentStatus?.lowercase() == "paid"

    /** Display formatted start date & time */
    val startDateTimeDisplay: String
        get() {
            val date = tripStartDate ?: return "N/A"
            val time = tripStartTime ?: ""
            return if (time.isNotBlank()) "$date $time" else date
        }

    /** Display formatted end date & time */
    val endDateTimeDisplay: String
        get() {
            val date = tripEndDate ?: return ""
            val time = tripEndTime ?: ""
            return if (time.isNotBlank()) "$date $time" else date
        }
}

