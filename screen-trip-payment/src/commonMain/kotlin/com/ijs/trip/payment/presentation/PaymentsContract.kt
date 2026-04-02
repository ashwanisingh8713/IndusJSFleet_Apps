package com.ijs.trip.payment.presentation

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText
import com.ijs.trip.payment.domain.entity.*

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
        val isDeleting: Boolean = false,

        // PDF Export
        val isExportingPdf: Boolean = false
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

        // PDF Export
        data object ExportPaymentsToPdf : Intent
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val paymentId: String) : Effect
        data object NavigateToAddPayment : Effect
        data class NavigateToAddPaymentForTrip(val tripId: String) : Effect
        data class ShowSnackbar(val message: UiText) : Effect
        data class ShowError(val message: UiText) : Effect
        data object PaymentDeleted : Effect
        data object PdfExportStarted : Effect
        data object PdfExportCompleted : Effect
    }
}
