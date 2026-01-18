package com.indusjs.fleet.presentation.payments

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.payment.*
import com.indusjs.fleet.domain.entity.trip.Trip
import com.indusjs.fleet.domain.entity.customer.CustomerSummary

/**
 * MVI Contract for Payments List Screen.
 */
object PaymentsContract {

    data class State(
        // List state
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val payments: List<TripPayment> = emptyList(),
        val summary: TripPaymentSummary? = null,
        val pendingSummary: PendingPaymentsSummary? = null,
        val error: String? = null,
        val page: Int = 1,
        val hasMore: Boolean = false,
        val isLoadingMore: Boolean = false,

        // Filter state
        val showFilterSheet: Boolean = false,
        val filter: TripPaymentFilter = TripPaymentFilter(),
        val tempFilter: TripPaymentFilter = TripPaymentFilter(),

        // Delete confirmation
        val showDeleteConfirmation: Boolean = false,
        val paymentToDelete: TripPayment? = null,
        val isDeleting: Boolean = false
    ) : UiState {
        val hasFilters: Boolean get() = filter.hasFilters
        val isEmpty: Boolean get() = payments.isEmpty() && !isLoading

        // Computed summary from payments when API doesn't provide summary
        private val computedReceived: Double get() =
            summary?.totalReceived?.takeIf { it > 0 }
                ?: payments.filter { it.paymentStatus == PaymentStatus.RECEIVED }.sumOf { it.amount }

        // Use pendingSummary from dashboard API first (total remaining across all trips)
        // Fallback to payments with PENDING status if dashboard API is not available
        private val computedPending: Double get() =
            pendingSummary?.totalPending?.takeIf { it > 0 }
                ?: summary?.totalPending?.takeIf { it > 0 }
                ?: 0.0

        // For this month, use summary if available, otherwise show total received as fallback
        private val computedThisMonth: Double get() =
            summary?.thisMonthTotal?.takeIf { it > 0 } ?: computedReceived

        val totalReceived: String get() = formatAmount(computedReceived)
        val totalPending: String get() = formatAmount(computedPending)
        val thisMonth: String get() = formatAmount(computedThisMonth)

        private fun formatAmount(value: Double): String {
            return when {
                value <= 0 -> "₹0.00"
                value >= 10000000 -> {
                    val crore = value / 10000000
                    "₹${formatDecimal(crore)}Cr"
                }
                value >= 100000 -> {
                    val lakh = value / 100000
                    "₹${formatDecimal(lakh)}L"
                }
                else -> "₹${formatDecimalWithCommas(value)}"
            }
        }

        private fun formatDecimal(value: Double): String {
            val intPart = value.toLong()
            val decPart = ((value - intPart) * 100).toLong()
            return if (decPart == 0L) "$intPart.00" else "$intPart.${decPart.toString().padStart(2, '0')}"
        }

        private fun formatDecimalWithCommas(value: Double): String {
            val intPart = value.toLong()
            val decPart = ((value - intPart) * 100).toLong()
            val formattedInt = formatWithIndianCommas(intPart)
            return if (decPart == 0L) "$formattedInt.00" else "$formattedInt.${decPart.toString().padStart(2, '0')}"
        }

        private fun formatWithIndianCommas(value: Long): String {
            if (value < 1000) return value.toString()
            val str = value.toString()
            val len = str.length
            val sb = StringBuilder()
            var count = 0
            for (i in len - 1 downTo 0) {
                sb.insert(0, str[i])
                count++
                if (i > 0) {
                    if (count == 3 || (count > 3 && (count - 3) % 2 == 0)) {
                        sb.insert(0, ',')
                    }
                }
            }
            return sb.toString()
        }
    }

    sealed interface Intent : UiIntent {
        data object LoadPayments : Intent
        data object Refresh : Intent
        data object LoadMore : Intent

        // Filter
        data object ShowFilterSheet : Intent
        data object HideFilterSheet : Intent
        data class UpdateTempFilterTrip(val tripId: String?) : Intent
        data class UpdateTempFilterCustomer(val customerId: String?) : Intent
        data class UpdateTempFilterType(val type: PaymentType?) : Intent
        data class UpdateTempFilterMode(val mode: PaymentMode?) : Intent
        data class UpdateTempFilterStatus(val status: PaymentStatus?) : Intent
        data class UpdateTempFilterDateRange(val startDate: String?, val endDate: String?) : Intent
        data object ApplyFilter : Intent
        data object ResetFilter : Intent

        // Navigation
        data class NavigateToPaymentDetail(val paymentId: String) : Intent
        data object NavigateToAddPayment : Intent
        data class NavigateToAddPaymentForTrip(val tripId: String) : Intent

        // Delete
        data class ShowDeleteConfirmation(val payment: TripPayment) : Intent
        data object HideDeleteConfirmation : Intent
        data object ConfirmDelete : Intent
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val paymentId: String) : Effect
        data object NavigateToAddPayment : Effect
        data class NavigateToAddPaymentForTrip(val tripId: String) : Effect
        data class ShowSnackbar(val message: String) : Effect
        data class ShowError(val message: String) : Effect
        data object PaymentDeleted : Effect
    }
}

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
        val error: String? = null,
        val amountError: String? = null,
        val tripError: String? = null,
        val dateError: String? = null
    ) : UiState {

        val netAmount: Double
            get() {
                val amt = amount.toDoubleOrNull() ?: 0.0
                val tds = tdsAmount.toDoubleOrNull() ?: 0.0
                val discount = discountAmount.toDoubleOrNull() ?: 0.0
                return amt - tds - discount
            }

        val netAmountDisplay: String
            get() = "₹${netAmount.toInt()}"

        val pendingAmount: Double
            get() = selectedTrip?.pendingAmount ?: 0.0

        val pendingAmountDisplay: String
            get() = "₹${pendingAmount.toInt()}"

        val canSave: Boolean
            get() {
                val baseValidation = selectedTrip != null &&
                        amount.isNotBlank() &&
                        amount.toDoubleOrNull()?.let { it > 0 } == true &&
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

        val title: String
            get() = if (isEditMode) "Edit Payment" else "Add Payment"
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
        data class ShowSnackbar(val message: String) : Effect
        data class ShowError(val message: String) : Effect
        data object PaymentSaved : Effect
    }
}

/**
 * MVI Contract for Payment Detail Screen.
 */
object PaymentDetailContract {

    data class State(
        val paymentId: String = "",
        val isLoading: Boolean = false,
        val payment: TripPayment? = null,
        val error: String? = null,

        // Delete confirmation
        val showDeleteConfirmation: Boolean = false,
        val isDeleting: Boolean = false,

        // User role (for edit/delete visibility)
        val canEdit: Boolean = false,
        val canDelete: Boolean = false
    ) : UiState

    sealed interface Intent : UiIntent {
        data class LoadPayment(val paymentId: String) : Intent
        data object Refresh : Intent
        data object NavigateToEdit : Intent
        data object ShowDeleteConfirmation : Intent
        data object HideDeleteConfirmation : Intent
        data object ConfirmDelete : Intent
        data object DownloadReceipt : Intent
    }

    sealed interface Effect : UiEffect {
        data class NavigateToEdit(val paymentId: String) : Effect
        data object NavigateBack : Effect
        data class ShowSnackbar(val message: String) : Effect
        data class ShowError(val message: String) : Effect
        data object PaymentDeleted : Effect
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
    val state: String?
) {
    val routeDisplay: String
        get() = "${startLocation.take(15)} → ${endLocation.take(15)}"

    val tripPriceDisplay: String
        get() = "₹${tripPrice.toInt()}"

    val pendingDisplay: String
        get() = "₹${pendingAmount.toInt()}"

    val displayText: String
        get() = "#$id • $vehicleRegistration • $routeDisplay"

    val hasPendingAmount: Boolean
        get() = pendingAmount > 0
}
