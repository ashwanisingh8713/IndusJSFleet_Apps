package com.ijs.trip.payment.data.repository

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.trip.payment.TAG_PAYMENT_REPO
import com.indusjs.error.exception.ApiException
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.auth.AuthTokenHelper
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.fleet.data.datasource.dashboard.DashboardRemoteDataSource
import com.ijs.trip.payment.data.datasource.TripPaymentRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.ijs.trip.payment.data.mapper.TripPaymentMapper
import com.ijs.trip.payment.data.mapper.TripPaymentMapper.toDomain
import com.ijs.trip.payment.data.mapper.TripPaymentMapper.toDomainList
import com.ijs.trip.payment.domain.entity.*
import com.ijs.trip.payment.domain.repository.TripPaymentRepository
import com.indusjs.fleet.core.util.convertToEpochMillis
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
        paymentDate: Long,
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
        paymentDate: Long,
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
            // Picker collects DD-MM-YYYY; backend now expects UTC epoch millis. Start of
            // day for the "from" bound, end of day for the "to" bound.
            startDate = filter.startDate?.let { convertToEpochMillis(it, "0000") },
            endDate = filter.endDate?.let { convertToEpochMillis(it, "2359") }
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

        // 1. Try nested endpoint first
        val nestedResponse = remoteDataSource.getTripPayments(token, tripIdInt, page, perPage)
        nestedResponse.data?.payments?.forEach { dto ->
            logger.d(TAG_PAYMENT_REPO, "PARSE_DTO_TEST - id: ${dto.id}, received_by: '${dto.receivedBy}'")
        }
        if (nestedResponse.success && nestedResponse.data != null && nestedResponse.data.payments.isNotEmpty()) {
            val result = nestedResponse.data.toDomain()
            logger.d(TAG_PAYMENT_REPO, "Fetched ${result.payments.size} payments for trip $tripId via nested route")
            Result.Success(result)
        } else {
            // 2. If nested is empty or fails, fall back to flat listPayments endpoint
            logger.d(TAG_PAYMENT_REPO, "Nested route empty or failed, trying flat /trip-payments endpoint")
            val flatResponse = remoteDataSource.listPayments(token, page, perPage, tripIdInt)
            if (flatResponse.success && flatResponse.data != null) {
                val result = flatResponse.toDomain()
                logger.d(TAG_PAYMENT_REPO, "Fetched ${result.payments.size} payments for trip $tripId via flat route fallback")
                Result.Success(result)
            } else {
                logger.e(TAG_PAYMENT_REPO, "Both nested and flat payment routes failed/returned empty")
                if (nestedResponse.success && nestedResponse.data != null) {
                    Result.Success(nestedResponse.data.toDomain())
                } else {
                    Result.Error(ApiException(nestedResponse.message ?: flatResponse.message ?: "Failed to fetch trip payments"))
                }
            }
        }
    } catch (e: Exception) {
        logger.e(TAG_PAYMENT_REPO, "Error fetching trip payments, trying flat route fallback: ${e.message}")
        try {
            val token = requireAuthToken()
            val tripIdInt = tripId.toInt()
            val flatResponse = remoteDataSource.listPayments(token, page, perPage, tripIdInt)
            if (flatResponse.success) {
                Result.Success(flatResponse.toDomain())
            } else {
                Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
            }
        } catch (fallbackEx: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
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
        paymentDate: Long?,
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
            tdsAmount = tdsAmount,
            discountAmount = discountAmount,
            // Backend requires payment_mode on update; the edit screen always
            // supplies it. Fall back to CASH only as a non-null safety net.
            paymentMode = paymentMode ?: PaymentMode.CASH,
            paymentSource = paymentSource,
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
        // Picker collects DD-MM-YYYY; backend now expects UTC epoch millis.
        val response = remoteDataSource.getPaymentSummary(
            token,
            startDate?.let { convertToEpochMillis(it, "0000") },
            endDate?.let { convertToEpochMillis(it, "2359") }
        )

        if (response.success && response.data != null) {
            val data = response.data
            val summary = TripPaymentSummary(
                totalReceived = data.totalReceived,
                totalPending = data.totalPending,
                totalCancelled = data.totalCancelled,
                totalTds = data.totalTds,
                totalDiscount = data.totalDiscount,
                totalNetAmount = data.netPayments,
                paymentCount = data.receiptCount,
                byMode = data.byMode
                    ?.mapNotNull { item -> item.paymentMode?.let { PaymentMode.fromApiValue(it) to item.amount } }
                    ?.toMap()
                    ?: emptyMap(),
                byType = data.byType
                    ?.mapNotNull { item -> item.paymentType?.let { PaymentType.fromApiValue(it) to item.amount } }
                    ?.toMap()
                    ?: emptyMap()
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
