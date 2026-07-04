package com.ijs.trip.payment.domain.entity

/**
 * Payment type enumeration.
 * Matches API values: advance, partial, final, refund
 */
enum class PaymentType(
    val apiValue: String,
    val displayName: String,
    val description: String
) {
    ADVANCE("advance", "Advance", "Before trip starts"),
    PARTIAL("partial", "Partial", "During or after trip"),
    FINAL("final", "Final", "Closes the trip payment"),
    REFUND("refund", "Refund", "Cancellation/overpayment");

    companion object {
        fun fromApiValue(value: String?): PaymentType =
            entries.find { it.apiValue.equals(value, ignoreCase = true) } ?: PARTIAL
    }
}

/**
 * Payment mode enumeration.
 * Matches API values: cash, upi, bank_transfer, card, credit
 */
enum class PaymentMode(
    val apiValue: String,
    val displayName: String
) {
    CASH("cash", "Cash"),
    UPI("upi", "UPI"),
    BANK_TRANSFER("bank_transfer", "Bank Transfer"),
    CARD("card", "Card"),
    CREDIT("credit", "Credit");

    companion object {
        fun fromApiValue(value: String?): PaymentMode =
            entries.find { it.apiValue.equals(value, ignoreCase = true) } ?: CASH
    }
}

/**
 * Payment status enumeration.
 * Matches API values: received, pending, cancelled, partial
 */
enum class PaymentStatus(
    val apiValue: String,
    val displayName: String
) {
    RECEIVED("received", "Received"),
    PENDING("pending", "Pending"),
    CANCELLED("cancelled", "Cancelled"),
    PARTIAL("partial", "Partial");

    companion object {
        fun fromApiValue(value: String?): PaymentStatus =
            entries.find { it.apiValue.equals(value, ignoreCase = true) } ?: PENDING
    }
}
