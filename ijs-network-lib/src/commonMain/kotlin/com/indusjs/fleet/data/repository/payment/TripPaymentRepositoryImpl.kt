package com.indusjs.fleet.data.repository.payment

import co.touchlab.kermit.Logger
import com.indusjs.error.exception.ApiException
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.auth.AuthTokenHelper
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.fleet.data.datasource.dashboard.DashboardRemoteDataSource
import com.indusjs.fleet.data.datasource.payment.TripPaymentRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.mapper.payment.TripPaymentMapper
import com.indusjs.fleet.data.mapper.payment.TripPaymentMapper.toDomain
import com.indusjs.fleet.data.mapper.payment.TripPaymentMapper.toDomainList
import com.indusjs.fleet.domain.entity.payment.*
import com.indusjs.fleet.domain.repository.payment.TripPaymentRepository
import dev.zacsweers.metro.Inject

/**
 * Implementation of TripPaymentRepository.
 */
@Inject
class TripPaymentRepositoryImpl(
    private val remoteDataSource: TripPaymentRemoteDataSource,
    private val dashboardDataSource: DashboardRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource
) : TripPaymentRepository {

    private val log = Logger.withTag("TripPaymentRepository")

    // ==================== Create ====================

    override suspend fun createPayment(
        tripId: Int?,
        vehicleId: Int?,
        driverId: Int?,
        customerId: Int?,
        customerName: String?,
        customerContact: String?,
        customerCompany: String?,
        customerGst: String?,
        amount: Double,
        tdsAmount: Double,
        discountAmount: Double,
        paymentType: PaymentType,
        paymentMode: PaymentMode,
        paymentSource: String?,
        paymentDate: String,
        transactionId: String?,
        bankName: String?,
        notes: String?,
        receivedBy: String?,
        receivedAtLocation: String?
    ): Result<TripPayment> = try {
        val token = requireAuthToken()
        val request = TripPaymentMapper.createRequest(
            tripId = tripId,
            vehicleId = vehicleId,
            driverId = driverId,
            customerId = customerId,
            customerName = customerName,
            customerContact = customerContact,
            customerCompany = customerCompany,
            customerGst = customerGst,
            amount = amount,
            tdsAmount = tdsAmount,
            discountAmount = discountAmount,
            paymentType = paymentType,
            paymentMode = paymentMode,
            paymentSource = paymentSource,
            paymentDate = paymentDate,
            transactionId = transactionId,
            bankName = bankName,
            notes = notes,
            receivedBy = receivedBy,
            receivedAtLocation = receivedAtLocation
        )

        val response = remoteDataSource.createPayment(token, request)
        if (response.success && response.data != null) {
            log.d { "Payment created successfully: ${response.data.id}" }
            Result.Success(response.data.toDomain())
        } else {
            log.e { "Create payment failed: ${response.message}" }
            Result.Error(ApiException(response.message ?: "Failed to create payment"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error creating payment: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    override suspend fun addPaymentToTrip(
        tripId: Int,
        vehicleId: Int,
        driverId: Int,
        customerId: Int?,
        amount: Double,
        tdsAmount: Double,
        discountAmount: Double,
        paymentType: PaymentType,
        paymentMode: PaymentMode,
        paymentSource: String?,
        paymentDate: String,
        transactionId: String?,
        bankName: String?,
        notes: String?,
        receivedBy: String?,
        receivedAtLocation: String?
    ): Result<TripPayment> = try {
        val token = requireAuthToken()
        val request = TripPaymentMapper.createAddToTripRequest(
            tripId = tripId,
            vehicleId = vehicleId,
            driverId = driverId,
            customerId = customerId,
            amount = amount,
            tdsAmount = tdsAmount,
            discountAmount = discountAmount,
            paymentType = paymentType,
            paymentMode = paymentMode,
            paymentSource = paymentSource,
            paymentDate = paymentDate,
            transactionId = transactionId,
            bankName = bankName,
            notes = notes,
            receivedBy = receivedBy,
            receivedAtLocation = receivedAtLocation
        )

        val response = remoteDataSource.addPaymentToTrip(token, tripId, request)
        if (response.success && response.data != null) {
            log.d { "Payment added to trip successfully: ${response.data.id}" }
            Result.Success(response.data.toDomain())
        } else {
            log.e { "Add payment to trip failed: ${response.message}" }
            Result.Error(ApiException(response.message ?: "Failed to add payment"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error adding payment to trip: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    // ==================== Read ====================

    override suspend fun listPayments(
        filter: TripPaymentFilter
    ): Result<TripPaymentListResult> = try {
        val token = requireAuthToken()
        val response = remoteDataSource.listPayments(
            token = token,
            page = filter.page,
            perPage = filter.perPage,
            tripId = filter.tripId?.toIntOrNull(),
            customerId = filter.customerId?.toIntOrNull(),
            paymentType = filter.paymentType?.apiValue,
            paymentMode = filter.paymentMode?.apiValue,
            paymentStatus = filter.paymentStatus?.apiValue,
            startDate = filter.startDate,
            endDate = filter.endDate
        )

        if (response.success) {
            val result = response.toDomain()
            log.d { "Fetched ${result.payments.size} payments" }
            Result.Success(result)
        } else {
            log.e { "List payments failed: ${response.message}" }
            Result.Error(ApiException(response.message ?: "Failed to fetch payments"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error listing payments: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    override suspend fun getTripPayments(
        tripId: String,
        page: Int,
        perPage: Int
    ): Result<TripPaymentListResult> = try {
        val token = requireAuthToken()
        val tripIdInt = tripId.toIntOrNull() ?: throw IllegalArgumentException("Invalid trip ID")

        val response = remoteDataSource.getTripPayments(token, tripIdInt, page, perPage)
        if (response.success && response.data != null) {
            val result = response.data.toDomain()
            log.d { "Fetched ${result.payments.size} payments for trip $tripId" }
            Result.Success(result)
        } else {
            log.e { "Get trip payments failed: ${response.message}" }
            Result.Error(ApiException(response.message ?: "Failed to fetch trip payments"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error fetching trip payments: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    override suspend fun getPayment(paymentId: String): Result<TripPayment> = try {
        val token = requireAuthToken()
        val paymentIdInt = paymentId.toIntOrNull() ?: throw IllegalArgumentException("Invalid payment ID")

        val response = remoteDataSource.getPayment(token, paymentIdInt)
        if (response.success && response.data != null) {
            log.d { "Fetched payment: $paymentId" }
            Result.Success(response.data.toDomain())
        } else {
            log.e { "Get payment failed: ${response.message}" }
            Result.Error(ApiException(response.message ?: "Failed to fetch payment"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error fetching payment: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    // ==================== Update ====================

    override suspend fun updatePayment(
        paymentId: String,
        amount: Double?,
        tdsAmount: Double?,
        discountAmount: Double?,
        paymentType: PaymentType?,
        paymentMode: PaymentMode?,
        paymentSource: String?,
        paymentDate: String?,
        paymentStatus: PaymentStatus?,
        transactionId: String?,
        bankName: String?,
        notes: String?,
        receivedBy: String?,
        receivedAtLocation: String?
    ): Result<TripPayment> = try {
        val token = requireAuthToken()
        val paymentIdInt = paymentId.toIntOrNull() ?: throw IllegalArgumentException("Invalid payment ID")

        val request = TripPaymentMapper.createUpdateRequest(
            amount = amount,
            tdsAmount = tdsAmount,
            discountAmount = discountAmount,
            paymentType = paymentType,
            paymentMode = paymentMode,
            paymentSource = paymentSource,
            paymentDate = paymentDate,
            paymentStatus = paymentStatus,
            transactionId = transactionId,
            bankName = bankName,
            notes = notes,
            receivedBy = receivedBy,
            receivedAtLocation = receivedAtLocation
        )

        val response = remoteDataSource.updatePayment(token, paymentIdInt, request)
        if (response.success && response.data != null) {
            log.d { "Payment updated: $paymentId" }
            Result.Success(response.data.toDomain())
        } else {
            log.e { "Update payment failed: ${response.message}" }
            Result.Error(ApiException(response.message ?: "Failed to update payment"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error updating payment: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    // ==================== Delete ====================

    override suspend fun deletePayment(paymentId: String): Result<Unit> = try {
        val token = requireAuthToken()
        val paymentIdInt = paymentId.toIntOrNull() ?: throw IllegalArgumentException("Invalid payment ID")

        val response = remoteDataSource.deletePayment(token, paymentIdInt)
        if (response.success) {
            log.d { "Payment deleted: $paymentId" }
            Result.Success(Unit)
        } else {
            log.e { "Delete payment failed: ${response.message}" }
            Result.Error(ApiException(response.message ?: "Failed to delete payment"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error deleting payment: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    // ==================== Reports ====================

    override suspend fun getPaymentSummary(
        startDate: String?,
        endDate: String?
    ): Result<TripPaymentSummary> = try {
        val token = requireAuthToken()
        val response = remoteDataSource.getPaymentSummary(token, startDate, endDate)

        if (response.success && response.data != null) {
            val data = response.data
            val summary = TripPaymentSummary(
                totalReceived = data.totalAmount,
                totalTds = data.totalTds,
                totalDiscount = data.totalDiscount,
                totalNetAmount = data.totalNetAmount,
                paymentCount = data.paymentCount
            )
            log.d { "Fetched payment summary" }
            Result.Success(summary)
        } else {
            log.e { "Get payment summary failed: ${response.message}" }
            Result.Error(ApiException(response.message ?: "Failed to fetch summary"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error fetching payment summary: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    override suspend fun getPendingPaymentsSummary(): Result<PendingPaymentsSummary> = try {
        val token = requireAuthToken()
        val response = dashboardDataSource.getPendingPayments(token, page = 1, perPage = 1)

        if (response.success && response.data != null) {
            val summary = PendingPaymentsSummary(
                totalPending = response.data.totalPending,
                totalCount = response.data.totalCount
            )
            log.d { "Fetched pending payments summary: total=${summary.totalPending}, count=${summary.totalCount}" }
            Result.Success(summary)
        } else {
            log.e { "Get pending payments summary failed: ${response.message}" }
            Result.Error(ApiException(response.message ?: "Failed to fetch pending summary"))
        }
    } catch (e: Exception) {
        log.e(e) { "Error fetching pending payments summary: ${e.message}" }
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    // ==================== Helper ====================

    private suspend fun requireAuthToken(): String {
        return AuthTokenHelper.requireAuthTokenOrRedirect {
            userLocalDataSource.getAuthToken()
        }
    }
}
