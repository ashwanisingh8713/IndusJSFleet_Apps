package com.ijs.payment.data.repository

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.payment.TAG_PAYMENT_REPO
import com.indusjs.error.exception.ApiException
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.auth.AuthTokenHelper
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.fleet.data.datasource.dashboard.DashboardRemoteDataSource
import com.ijs.payment.data.datasource.TripPaymentRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.ijs.payment.data.mapper.TripPaymentMapper
import com.ijs.payment.data.mapper.TripPaymentMapper.toDomain
import com.ijs.payment.data.mapper.TripPaymentMapper.toDomainList
import com.ijs.payment.domain.entity.*
import com.ijs.payment.domain.repository.TripPaymentRepository
import dev.zacsweers.metro.Inject

/**
 * Implementation of TripPaymentRepository.
 */
@Inject
class TripPaymentRepositoryImpl(
    private val remoteDataSource: TripPaymentRemoteDataSource,
    private val dashboardDataSource: DashboardRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    private val logger: FleetLogger
) : TripPaymentRepository {
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
            logger.d(TAG_PAYMENT_REPO, "Payment created successfully: ${response.data.id}")
            Result.Success(response.data.toDomain())
        } else {
            logger.e(TAG_PAYMENT_REPO, "Create payment failed: ${response.message}")
            Result.Error(ApiException(response.message ?: "Failed to create payment"))
        }
    } catch (e: Exception) {
        logger.e(TAG_PAYMENT_REPO, "Error creating payment: ${e.message}", e)
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
            logger.d(TAG_PAYMENT_REPO, "Payment added to trip successfully: ${response.data.id}")
            Result.Success(response.data.toDomain())
        } else {
            logger.e(TAG_PAYMENT_REPO, "Add payment to trip failed: ${response.message}")
            Result.Error(ApiException(response.message ?: "Failed to add payment"))
        }
    } catch (e: Exception) {
        logger.e(TAG_PAYMENT_REPO, "Error adding payment to trip: ${e.message}", e)
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
            logger.d(TAG_PAYMENT_REPO, "Fetched ${result.payments.size} payments")
            Result.Success(result)
        } else {
            logger.e(TAG_PAYMENT_REPO, "List payments failed: ${response.message}")
            Result.Error(ApiException(response.message ?: "Failed to fetch payments"))
        }
    } catch (e: Exception) {
        logger.e(TAG_PAYMENT_REPO, "Error listing payments: ${e.message}", e)
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
            logger.d(TAG_PAYMENT_REPO, "Fetched ${result.payments.size} payments for trip $tripId")
            Result.Success(result)
        } else {
            logger.e(TAG_PAYMENT_REPO, "Get trip payments failed: ${response.message}")
            Result.Error(ApiException(response.message ?: "Failed to fetch trip payments"))
        }
    } catch (e: Exception) {
        logger.e(TAG_PAYMENT_REPO, "Error fetching trip payments: ${e.message}", e)
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    override suspend fun getPayment(paymentId: String): Result<TripPayment> = try {
        val token = requireAuthToken()
        val paymentIdInt = paymentId.toIntOrNull() ?: throw IllegalArgumentException("Invalid payment ID")

        val response = remoteDataSource.getPayment(token, paymentIdInt)
        if (response.success && response.data != null) {
            logger.d(TAG_PAYMENT_REPO, "Fetched payment: $paymentId")
            Result.Success(response.data.toDomain())
        } else {
            logger.e(TAG_PAYMENT_REPO, "Get payment failed: ${response.message}")
            Result.Error(ApiException(response.message ?: "Failed to fetch payment"))
        }
    } catch (e: Exception) {
        logger.e(TAG_PAYMENT_REPO, "Error fetching payment: ${e.message}", e)
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
            logger.d(TAG_PAYMENT_REPO, "Payment updated: $paymentId")
            Result.Success(response.data.toDomain())
        } else {
            logger.e(TAG_PAYMENT_REPO, "Update payment failed: ${response.message}")
            Result.Error(ApiException(response.message ?: "Failed to update payment"))
        }
    } catch (e: Exception) {
        logger.e(TAG_PAYMENT_REPO, "Error updating payment: ${e.message}", e)
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    // ==================== Delete ====================

    override suspend fun deletePayment(paymentId: String): Result<Unit> = try {
        val token = requireAuthToken()
        val paymentIdInt = paymentId.toIntOrNull() ?: throw IllegalArgumentException("Invalid payment ID")

        val response = remoteDataSource.deletePayment(token, paymentIdInt)
        if (response.success) {
            logger.d(TAG_PAYMENT_REPO, "Payment deleted: $paymentId")
            Result.Success(Unit)
        } else {
            logger.e(TAG_PAYMENT_REPO, "Delete payment failed: ${response.message}")
            Result.Error(ApiException(response.message ?: "Failed to delete payment"))
        }
    } catch (e: Exception) {
        logger.e(TAG_PAYMENT_REPO, "Error deleting payment: ${e.message}", e)
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
            logger.d(TAG_PAYMENT_REPO, "Fetched payment summary")
            Result.Success(summary)
        } else {
            logger.e(TAG_PAYMENT_REPO, "Get payment summary failed: ${response.message}")
            Result.Error(ApiException(response.message ?: "Failed to fetch summary"))
        }
    } catch (e: Exception) {
        logger.e(TAG_PAYMENT_REPO, "Error fetching payment summary: ${e.message}", e)
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    override suspend fun getPendingPaymentsSummary(): Result<PendingPaymentsSummary> = try {
        val token = requireAuthToken()
        val response = dashboardDataSource.getPendingPayments(token, page = 1, perPage = 1)
        val data = response.data

        if (response.success && data != null) {
            val summary = PendingPaymentsSummary(
                totalPending = data.totalPending,
                totalCount = data.totalCount
            )
            logger.d(TAG_PAYMENT_REPO, "Fetched pending payments summary: total=${summary.totalPending}, count=${summary.totalCount}")
            Result.Success(summary)
        } else {
            logger.e(TAG_PAYMENT_REPO, "Get pending payments summary failed: ${response.message}")
            Result.Error(ApiException(response.message ?: "Failed to fetch pending summary"))
        }
    } catch (e: Exception) {
        logger.e(TAG_PAYMENT_REPO, "Error fetching pending payments summary: ${e.message}", e)
        Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
    }

    // ==================== Helper ====================

    private suspend fun requireAuthToken(): String {
        return AuthTokenHelper.requireAuthTokenOrRedirect {
            userLocalDataSource.getAuthToken()
        }
    }
}
