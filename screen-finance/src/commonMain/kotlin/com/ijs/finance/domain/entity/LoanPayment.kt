package com.ijs.finance.domain.entity

/**
 * Domain entity representing a loan/EMI payment.
 */
data class LoanPayment(
    val id: Int = 0,
    val vehiclePurchaseId: Int = 0,
    val vehicleId: Int = 0,
    val emiNumber: Int? = null,
    val dueDate: Long? = null,
    val amount: Double = 0.0,
    val paymentDate: Long? = null,
    val principalAmount: Double = 0.0,
    val interestAmount: Double = 0.0,
    val lateFee: Double = 0.0,
    val prepaymentAmount: Double = 0.0,
    val entryType: EntryType = EntryType.MANUAL,
    val paymentMode: PaymentMode? = null,
    val paymentSource: String? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val transactionRef: String? = null,
    val notes: String? = null,
    val ownerId: Int = 0,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) {
    /**
     * Check if payment is completed
     */
    val isPaid: Boolean get() = paymentStatus == PaymentStatus.PAID

    /**
     * Check if payment is overdue
     */
    val isOverdue: Boolean get() = paymentStatus == PaymentStatus.OVERDUE

    /**
     * Check if payment is pending
     */
    val isPending: Boolean get() = paymentStatus == PaymentStatus.PENDING
}

/**
 * Entry type - how the payment record was created
 */
enum class EntryType(val value: String) {
    SCHEDULED("scheduled"),
    MANUAL("manual"),
    APP_PAYMENT("app_payment"),
    AUTO_DEBIT("auto_debit");

    companion object {
        fun fromValue(value: String): EntryType {
            return entries.find { it.value == value } ?: MANUAL
        }
    }
}

/**
 * Payment mode
 */
enum class PaymentMode(val value: String, val label: String) {
    CASH("cash", "Cash"),
    NETBANKING("netbanking", "Netbanking"),
    UPI("upi", "UPI"),
    AUTO_DEBIT("auto_debit", "Auto Debit"),
    CHEQUE("cheque", "Cheque"),
    OTHER("other", "Other");

    companion object {
        fun fromValue(value: String?): PaymentMode? {
            if (value == null) return null
            return entries.find { it.value == value }
        }
    }
}

/**
 * Payment status
 */
enum class PaymentStatus(val value: String) {
    PENDING("pending"),
    PAID("paid"),
    OVERDUE("overdue"),
    FAILED("failed"),
    CANCELLED("cancelled");

    companion object {
        fun fromValue(value: String): PaymentStatus {
            return entries.find { it.value == value } ?: PENDING
        }
    }
}

/**
 * Loan summary for a vehicle
 */
data class LoanSummary(
    val vehicleId: Int = 0,
    val vehiclePurchaseId: Int = 0,
    val vehicle: VehicleBasicInfo? = null,
    val loanAmount: Double = 0.0,
    val emiAmount: Double = 0.0,
    val tenureMonths: Int = 0,
    val interestRate: Double = 0.0,
    val totalPaid: Double = 0.0,
    val outstandingBalance: Double = 0.0,
    val emisPaid: Int = 0,
    val emisRemaining: Int = 0,
    val nextEmiDueDate: Long? = null,
    val nextEmiAmount: Double = 0.0,
    val loanStatus: LoanStatus = LoanStatus.ACTIVE,
    val financierName: String? = null
) {
    val progressPercent: Int get() {
        if (tenureMonths <= 0) return 0
        return ((emisPaid.toDouble() / tenureMonths) * 100).toInt().coerceIn(0, 100)
    }
}

/**
 * Upcoming/Overdue EMI alert
 */
data class EmiAlert(
    val paymentId: Int = 0,
    val vehicleId: Int = 0,
    val vehiclePurchaseId: Int = 0,
    val vehicle: VehicleBasicInfo? = null,
    val emiNumber: Int = 0,
    val dueDate: Long? = null,
    val amount: Double = 0.0,
    val daysUntilDue: Int = 0,
    val daysOverdue: Int = 0,
    val isOverdue: Boolean = false,
    val financierName: String? = null
) {
    val severity: AlertSeverity get() = when {
        isOverdue -> AlertSeverity.CRITICAL
        daysUntilDue <= 3 -> AlertSeverity.WARNING
        else -> AlertSeverity.INFO
    }
}

enum class AlertSeverity {
    INFO, WARNING, CRITICAL
}
