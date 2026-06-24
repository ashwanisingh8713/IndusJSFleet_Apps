package com.ijs.finance.domain.entity

/**
 * Domain entity representing vehicle purchase information.
 */
data class VehiclePurchase(
    val id: Int = 0,
    val vehicleId: Int = 0,
    val vehicle: VehicleBasicInfo? = null,
    val purchaseDate: Long? = null,
    val purchasePrice: Double = 0.0,
    val vendorName: String? = null,
    val invoiceNumber: String? = null,
    val paymentType: PaymentType = PaymentType.CASH,
    val downPayment: Double = 0.0,
    val loanAmount: Double = 0.0,
    val interestRate: Double = 0.0,
    val tenureMonths: Int = 0,
    val emiAmount: Double = 0.0,
    val loanStartDate: Long? = null,
    val loanEndDate: Long? = null,
    val financierName: String? = null,
    val loanAccountNumber: String? = null,
    val bankName: String? = null,
    val bankAccountNumber: String? = null,
    val bankIfsc: String? = null,
    val autoDebitEnabled: Boolean = false,
    val totalPaid: Double = 0.0,
    val outstandingBalance: Double = 0.0,
    val emisPaid: Int = 0,
    val emisRemaining: Int = 0,
    val nextEmiDueDate: Long? = null,
    val loanStatus: LoanStatus = LoanStatus.NOT_APPLICABLE,
    val notes: String? = null,
    val ownerId: Int = 0,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) {
    /**
     * Check if this is a financed purchase (loan)
     */
    val isFinanced: Boolean get() = paymentType == PaymentType.LOAN

    /**
     * Calculate loan progress percentage
     */
    val loanProgressPercent: Int get() {
        if (tenureMonths <= 0) return 0
        return ((emisPaid.toDouble() / tenureMonths) * 100).toInt().coerceIn(0, 100)
    }

    /**
     * Total interest payable
     */
    val totalInterest: Double get() {
        if (!isFinanced || emiAmount <= 0 || tenureMonths <= 0) return 0.0
        return (emiAmount * tenureMonths) - loanAmount
    }

    /**
     * Total amount payable (loan + interest)
     */
    val totalPayable: Double get() {
        if (!isFinanced) return purchasePrice
        return emiAmount * tenureMonths
    }

    /**
     * Down payment percentage
     */
    val downPaymentPercent: Int get() {
        if (purchasePrice <= 0) return 0
        return ((downPayment / purchasePrice) * 100).toInt()
    }
}

/**
 * Basic vehicle info for display in finance screens
 */
data class VehicleBasicInfo(
    val id: Int = 0,
    val registrationNumber: String = "",
    val make: String = "",
    val model: String = ""
) {
    val displayName: String get() = "$registrationNumber - $make $model"
}

/**
 * Payment type for vehicle purchase
 */
enum class PaymentType(val value: String, val label: String) {
    CASH("cash", "Cash / Full Payment"),
    LOAN("loan", "Loan (EMI)");

    companion object {
        fun fromValue(value: String): PaymentType {
            return entries.find { it.value == value } ?: CASH
        }
    }
}

/**
 * Loan status
 */
enum class LoanStatus(val value: String) {
    NOT_APPLICABLE("not_applicable"),
    ACTIVE("active"),
    CLOSED("closed"),
    DEFAULTED("defaulted");

    companion object {
        fun fromValue(value: String): LoanStatus {
            return entries.find { it.value == value } ?: NOT_APPLICABLE
        }
    }
}
