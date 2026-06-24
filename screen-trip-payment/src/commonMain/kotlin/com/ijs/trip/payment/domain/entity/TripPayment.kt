package com.ijs.trip.payment.domain.entity

/**
 * Trip payment entity representing a payment record for a trip.
 */
data class TripPayment(
    val id: String,
    val tripId: String,
    val vehicleId: String? = null,
    val driverId: String? = null,
    val customerId: String? = null,
    val customerName: String? = null,
    val customerContact: String? = null,
    val customerCompany: String? = null,
    val customerGst: String? = null,
    val amount: Double = 0.0,
    val tdsAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val netAmount: Double = 0.0,
    val paymentType: PaymentType = PaymentType.PARTIAL,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val paymentSource: String? = null,
    /** Payment timestamp as UTC epoch millis (null/0 = unset). */
    val paymentDate: Long? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.RECEIVED,
    val transactionId: String? = null,
    val bankName: String? = null,
    val receiptNumber: String? = null,
    val financialYear: String? = null,
    val financialMonth: String? = null,
    val notes: String? = null,
    val receivedBy: String? = null,
    val receivedAtLocation: String? = null,
    val ownerId: String? = null,
    val createdById: String? = null,
    val createdByName: String? = null,
    /** Created timestamp as UTC epoch millis (null/0 = unset). */
    val createdAt: Long? = null,
    /** Updated timestamp as UTC epoch millis (null/0 = unset). */
    val updatedAt: Long? = null,
    // Embedded trip info (for list display)
    val tripInfo: TripPaymentTripInfo? = null
) {

    val amountDisplay: String
        get() = "₹${formatAmount(amount)}"

    val netAmountDisplay: String
        get() = "₹${formatAmount(netAmount)}"

    val tdsDisplay: String
        get() = if (tdsAmount > 0) "₹${formatAmount(tdsAmount)}" else "-"

    val discountDisplay: String
        get() = if (discountAmount > 0) "₹${formatAmount(discountAmount)}" else "-"

    val modeIcon: String
        get() = paymentMode.icon

    val modeDisplay: String
        get() = paymentMode.displayName

    val typeDisplay: String
        get() = paymentType.displayName

    val statusDisplay: String
        get() = paymentStatus.displayName

    val statusIcon: String
        get() = paymentStatus.icon

    val isReceived: Boolean
        get() = paymentStatus == PaymentStatus.RECEIVED

    val isPending: Boolean
        get() = paymentStatus == PaymentStatus.PENDING

    val isCancelled: Boolean
        get() = paymentStatus == PaymentStatus.CANCELLED

    val hasTds: Boolean
        get() = tdsAmount > 0

    val hasDiscount: Boolean
        get() = discountAmount > 0

    val routeDisplay: String
        get() = tripInfo?.let { "${it.startLocation ?: "Unknown"} → ${it.endLocation ?: "Unknown"}" } ?: "N/A"

    private fun formatAmount(value: Double): String {
        return when {
            value >= 100000 -> "${(value / 100000.0).toString().take(5)}L"
            value >= 1000 -> value.toInt().toString()
            else -> value.toString().take(6)
        }
    }
}

/**
 * Embedded trip info for payment list display.
 */
data class TripPaymentTripInfo(
    val tripId: String? = null,
    val vehicleRegistration: String? = null,
    val vehicleMake: String? = null,
    val vehicleModel: String? = null,
    val driverName: String? = null,
    val customerName: String? = null,
    val startLocation: String? = null,
    val endLocation: String? = null,
    val tripPrice: Double? = null,
    val tripState: String? = null,
    // Trip dates for display (DD-MM-YYYY format)
    val tripStartDate: String? = null,
    val tripEndDate: String? = null,
    // Trip times for display (HH:mm format)
    val tripStartTime: String? = null,
    val tripEndTime: String? = null
) {
    val vehicleDisplay: String
        get() = vehicleRegistration ?: "N/A"

    val routeDisplay: String
        get() = "${startLocation?.take(15) ?: "Unknown"} → ${endLocation?.take(15) ?: "Unknown"}"

    val tripPriceDisplay: String
        get() = tripPrice?.let { "₹${it.toInt()}" } ?: "N/A"

    /** Formatted start date & time display */
    val startDateTimeDisplay: String
        get() {
            val date = tripStartDate ?: return "N/A"
            val time = tripStartTime ?: ""
            return if (time.isNotBlank()) "$date $time" else date
        }

    /** Formatted end date & time display */
    val endDateTimeDisplay: String
        get() {
            val date = tripEndDate ?: return "N/A"
            val time = tripEndTime ?: ""
            return if (time.isNotBlank()) "$date $time" else date
        }
}

/**
 * Trip payment summary for stats display.
 */
data class TripPaymentSummary(
    val totalReceived: Double = 0.0,
    val totalPending: Double = 0.0,
    val totalCancelled: Double = 0.0,
    val totalTds: Double = 0.0,
    val totalDiscount: Double = 0.0,
    val totalNetAmount: Double = 0.0,
    val paymentCount: Int = 0,
    val receivedCount: Int = 0,
    val pendingCount: Int = 0,
    val thisMonthTotal: Double = 0.0,
    val byMode: Map<PaymentMode, Double> = emptyMap(),
    val byType: Map<PaymentType, Double> = emptyMap(),
    /**
     * Per-trip payment status from the trip payments history summary
     * (GET /trips/{id}/payments → summary.payment_status). Null for the
     * all-payments summary, which has no single status.
     */
    val paymentStatus: String? = null
) {
    val totalReceivedDisplay: String
        get() = "₹${formatWithSuffix(totalReceived)}"

    val totalPendingDisplay: String
        get() = "₹${formatWithSuffix(totalPending)}"

    val thisMonthDisplay: String
        get() = "₹${formatWithSuffix(thisMonthTotal)}"

    private fun formatWithSuffix(value: Double): String {
        return when {
            value >= 10000000 -> "${(value / 10000000.0).toString().take(5)}Cr"
            value >= 100000 -> "${(value / 100000.0).toString().take(5)}L"
            value >= 1000 -> "${(value / 1000.0).toString().take(4)}K"
            else -> value.toInt().toString()
        }
    }
}

/**
 * Trip payment list result with pagination.
 */
data class TripPaymentListResult(
    val payments: List<TripPayment>,
    val summary: TripPaymentSummary? = null,
    val page: Int = 1,
    val perPage: Int = 20,
    val total: Int = 0,
    val totalPages: Int = 1,
    val hasMore: Boolean = false
)

/**
 * Filter for listing trip payments.
 */
data class TripPaymentFilter(
    val tripId: String? = null,
    val customerId: String? = null,
    val paymentType: PaymentType? = null,
    val paymentMode: PaymentMode? = null,
    val paymentStatus: PaymentStatus? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val page: Int = 1,
    val perPage: Int = 20
) {
    val hasFilters: Boolean
        get() = tripId != null || customerId != null || paymentType != null ||
                paymentMode != null || paymentStatus != null ||
                startDate != null || endDate != null
}

/**
 * Summary of pending payments across all trips.
 * Used for displaying total pending amount in PaymentsScreen header.
 */
data class PendingPaymentsSummary(
    val totalPending: Double = 0.0,
    val totalCount: Int = 0
) {
    val totalPendingDisplay: String
        get() = formatAmount(totalPending)

    private fun formatAmount(value: Double): String {
        return when {
            value <= 0 -> "₹0"
            value >= 10000000 -> "₹${(value / 10000000).toInt()}Cr"
            value >= 100000 -> "₹${(value / 100000).toInt()}L"
            value >= 1000 -> "₹${(value / 1000).toInt()}K"
            else -> "₹${value.toInt()}"
        }
    }
}

