package com.indusjs.fleet.domain.repository.finance

import com.indusjs.error.result.Result
import com.indusjs.fleet.domain.entity.finance.*

/**
 * Repository interface for Vehicle Finance operations.
 */
interface VehicleFinanceRepository {

    // ==================== Purchase APIs ====================

    /**
     * Get purchase info for a vehicle.
     */
    suspend fun getPurchase(vehicleId: Int): Result<VehiclePurchase?>

    /**
     * Create purchase info for a vehicle.
     */
    suspend fun createPurchase(
        vehicleId: Int,
        purchaseDate: String,
        purchasePrice: Double,
        vendorName: String?,
        invoiceNumber: String?,
        paymentType: PaymentType,
        downPayment: Double?,
        loanAmount: Double?,
        interestRate: Double?,
        tenureMonths: Int?,
        emiAmount: Double?,
        loanStartDate: String?,
        financierName: String?,
        loanAccountNumber: String?,
        bankName: String?,
        bankAccountNumber: String?,
        bankIfsc: String?,
        autoDebitEnabled: Boolean?,
        notes: String?
    ): Result<VehiclePurchase>

    /**
     * Update purchase info for a vehicle.
     */
    suspend fun updatePurchase(
        vehicleId: Int,
        vendorName: String?,
        invoiceNumber: String?,
        bankName: String?,
        bankAccountNumber: String?,
        bankIfsc: String?,
        autoDebitEnabled: Boolean?,
        notes: String?
    ): Result<VehiclePurchase>

    // ==================== Loan Summary ====================

    /**
     * Get loan summary for a vehicle.
     */
    suspend fun getLoanSummary(vehicleId: Int): Result<LoanSummary>

    // ==================== Loan Payments ====================

    /**
     * Get loan payments for a vehicle.
     */
    suspend fun getLoanPayments(
        vehicleId: Int,
        page: Int = 1,
        perPage: Int = 20,
        status: String? = null
    ): Result<List<LoanPayment>>

    /**
     * Get all loan payments across all vehicles.
     */
    suspend fun getAllLoanPayments(
        page: Int = 1,
        perPage: Int = 20,
        vehicleId: Int? = null,
        status: String? = null
    ): Result<List<LoanPayment>>

    /**
     * Get payment by ID.
     */
    suspend fun getPaymentById(paymentId: Int): Result<LoanPayment>

    /**
     * Record an EMI payment.
     */
    suspend fun recordPayment(
        vehiclePurchaseId: Int,
        amount: Double,
        paymentDate: String,
        paymentMode: PaymentMode?,
        paymentSource: String?,
        transactionRef: String?,
        lateFee: Double?,
        prepaymentAmount: Double?,
        notes: String?
    ): Result<LoanPayment>

    /**
     * Update an existing payment.
     */
    suspend fun updatePayment(
        paymentId: Int,
        paymentMode: PaymentMode?,
        paymentSource: String?,
        transactionRef: String?,
        notes: String?
    ): Result<LoanPayment>

    /**
     * Mark a scheduled EMI as paid.
     */
    suspend fun markEmiPaid(
        paymentId: Int,
        paymentDate: String,
        paymentMode: PaymentMode?,
        transactionRef: String?,
        notes: String?
    ): Result<LoanPayment>

    /**
     * Delete a payment record.
     */
    suspend fun deletePayment(paymentId: Int): Result<Unit>

    // ==================== Alerts ====================

    /**
     * Get upcoming EMIs (next N days).
     */
    suspend fun getUpcomingEmis(days: Int = 30): Result<List<EmiAlert>>

    /**
     * Get overdue EMIs.
     */
    suspend fun getOverdueEmis(): Result<List<EmiAlert>>

    /**
     * Get all EMI alerts (upcoming + overdue).
     */
    suspend fun getEmiAlerts(): Result<Pair<List<EmiAlert>, List<EmiAlert>>>
}
