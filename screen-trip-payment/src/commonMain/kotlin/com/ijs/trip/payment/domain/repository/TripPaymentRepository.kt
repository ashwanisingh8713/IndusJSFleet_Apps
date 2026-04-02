package com.ijs.trip.payment.domain.repository

import com.indusjs.error.result.Result
import com.ijs.trip.payment.domain.entity.*

/**
 * Repository interface for trip payment operations.
 */
interface TripPaymentRepository {

    // ==================== Create ====================

    /**
     * Create a new trip payment.
     */
    suspend fun createPayment(
        tripId: Int?,
        vehicleId: Int?,
        driverId: Int?,
        customerId: Int?,
        customerName: String?,
        customerContact: String?,
        customerCompany: String?,
        customerGst: String?,
        amount: Double,
        tdsAmount: Double = 0.0,
        discountAmount: Double = 0.0,
        paymentType: PaymentType,
        paymentMode: PaymentMode,
        paymentSource: String?,
        paymentDate: String,
        transactionId: String?,
        bankName: String?,
        notes: String?,
        receivedBy: String?,
        receivedAtLocation: String?
    ): Result<TripPayment>

    /**
     * Add payment to a specific trip.
     * Customer info is auto-filled from trip.
     */
    suspend fun addPaymentToTrip(
        tripId: Int,
        vehicleId: Int,
        driverId: Int,
        customerId: Int?,
        amount: Double,
        tdsAmount: Double = 0.0,
        discountAmount: Double = 0.0,
        paymentType: PaymentType,
        paymentMode: PaymentMode,
        paymentSource: String?,
        paymentDate: String,
        transactionId: String?,
        bankName: String?,
        notes: String?,
        receivedBy: String?,
        receivedAtLocation: String?
    ): Result<TripPayment>

    // ==================== Read ====================

    /**
     * List all trip payments with optional filters.
     */
    suspend fun listPayments(
        filter: TripPaymentFilter = TripPaymentFilter()
    ): Result<TripPaymentListResult>

    /**
     * Get payments for a specific trip.
     */
    suspend fun getTripPayments(
        tripId: String,
        page: Int = 1,
        perPage: Int = 20
    ): Result<TripPaymentListResult>

    /**
     * Get payment by ID.
     */
    suspend fun getPayment(paymentId: String): Result<TripPayment>

    // ==================== Update ====================

    /**
     * Update a trip payment.
     */
    suspend fun updatePayment(
        paymentId: String,
        amount: Double? = null,
        tdsAmount: Double? = null,
        discountAmount: Double? = null,
        paymentType: PaymentType? = null,
        paymentMode: PaymentMode? = null,
        paymentSource: String? = null,
        paymentDate: String? = null,
        paymentStatus: PaymentStatus? = null,
        transactionId: String? = null,
        bankName: String? = null,
        notes: String? = null,
        receivedBy: String? = null,
        receivedAtLocation: String? = null
    ): Result<TripPayment>

    // ==================== Delete ====================

    /**
     * Delete a trip payment.
     */
    suspend fun deletePayment(paymentId: String): Result<Unit>

    // ==================== Reports ====================

    /**
     * Get payment summary report.
     */
    suspend fun getPaymentSummary(
        startDate: String? = null,
        endDate: String? = null
    ): Result<TripPaymentSummary>

    /**
     * Get pending payments summary (total pending amount across all trips).
     * This fetches from dashboard pending payments API.
     */
    suspend fun getPendingPaymentsSummary(): Result<PendingPaymentsSummary>
}
