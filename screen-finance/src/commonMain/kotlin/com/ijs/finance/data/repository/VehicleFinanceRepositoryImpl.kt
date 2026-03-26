package com.ijs.finance.data.repository

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.finance.TAG_FINANCE_REPO
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.error.exception.ApiException
import com.indusjs.error.exception.AuthException
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.auth.AuthTokenHelper
import com.ijs.finance.data.datasource.VehicleFinanceRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.ijs.finance.data.mapper.toDomain
import com.ijs.finance.data.mapper.toDomainAlerts
import com.ijs.finance.data.mapper.toDomainPayments
import com.ijs.finance.data.model.*
import com.ijs.finance.domain.entity.*
import com.ijs.finance.domain.repository.VehicleFinanceRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

@Inject
class VehicleFinanceRepositoryImpl(
    private val remoteDataSource: VehicleFinanceRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    private val dispatcherProvider: DispatcherProvider,
    private val logger: FleetLogger
) : VehicleFinanceRepository {
/**
     * Get auth token or emit session expired event and throw AuthException.
     * This ensures redirect to login when token is null.
     */
    private suspend fun requireAuthToken(): String =
        AuthTokenHelper.requireAuthTokenOrRedirect { userLocalDataSource.getAuthToken() }

    // ==================== Purchase APIs ====================

    override suspend fun getPurchase(vehicleId: Int): Result<VehiclePurchase?> {
        return withContext(dispatcherProvider.io) {
            try {
                val token = requireAuthToken()

                val response = remoteDataSource.getPurchase(token, vehicleId)
                if (response.success) {
                    Result.Success(response.data?.toDomain())
                } else {
                    // No purchase info is not an error
                    if (response.data == null) {
                        Result.Success(null)
                    } else {
                        Result.Error(ApiException(response.message), response.message)
                    }
                }
            } catch (e: AuthException) {
                Result.Error(e, e.message)
            } catch (e: Exception) {
                logger.e(TAG_FINANCE_REPO, "Error getting purchase: ${e.message}", e)
                Result.Error(e, e.message ?: "Failed to get purchase info")
            }
        }
    }

    override suspend fun createPurchase(
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
    ): Result<VehiclePurchase> {
        return withContext(dispatcherProvider.io) {
            try {
                val token = requireAuthToken()

                val request = CreatePurchaseRequest(
                    purchaseDate = purchaseDate,
                    purchasePrice = purchasePrice,
                    vendorName = vendorName,
                    invoiceNumber = invoiceNumber,
                    paymentType = paymentType.value,
                    downPayment = downPayment,
                    loanAmount = loanAmount,
                    interestRate = interestRate,
                    tenureMonths = tenureMonths,
                    emiAmount = emiAmount,
                    loanStartDate = loanStartDate,
                    financierName = financierName,
                    loanAccountNumber = loanAccountNumber,
                    bankName = bankName,
                    bankAccountNumber = bankAccountNumber,
                    bankIfsc = bankIfsc,
                    autoDebitEnabled = autoDebitEnabled,
                    notes = notes
                )

                val response = remoteDataSource.createPurchase(token, vehicleId, request)
                if (response.success && response.data != null) {
                    Result.Success(response.data.toDomain())
                } else {
                    Result.Error(ApiException(response.message), response.message)
                }
            } catch (e: Exception) {
                logger.e(TAG_FINANCE_REPO, "Error creating purchase: ${e.message}", e)
                Result.Error(e, e.message ?: "Failed to create purchase info")
            }
        }
    }

    override suspend fun updatePurchase(
        vehicleId: Int,
        vendorName: String?,
        invoiceNumber: String?,
        bankName: String?,
        bankAccountNumber: String?,
        bankIfsc: String?,
        autoDebitEnabled: Boolean?,
        notes: String?
    ): Result<VehiclePurchase> {
        return withContext(dispatcherProvider.io) {
            try {
                val token = requireAuthToken()

                val request = UpdatePurchaseRequest(
                    vendorName = vendorName,
                    invoiceNumber = invoiceNumber,
                    bankName = bankName,
                    bankAccountNumber = bankAccountNumber,
                    bankIfsc = bankIfsc,
                    autoDebitEnabled = autoDebitEnabled,
                    notes = notes
                )

                val response = remoteDataSource.updatePurchase(token, vehicleId, request)
                if (response.success && response.data != null) {
                    Result.Success(response.data.toDomain())
                } else {
                    Result.Error(ApiException(response.message), response.message)
                }
            } catch (e: Exception) {
                logger.e(TAG_FINANCE_REPO, "Error updating purchase: ${e.message}", e)
                Result.Error(e, e.message ?: "Failed to update purchase info")
            }
        }
    }

    // ==================== Loan Summary ====================

    override suspend fun getLoanSummary(vehicleId: Int): Result<LoanSummary> {
        return withContext(dispatcherProvider.io) {
            try {
                val token = requireAuthToken()

                val response = remoteDataSource.getLoanSummary(token, vehicleId)
                if (response.success && response.data != null) {
                    Result.Success(response.data.toDomain())
                } else {
                    Result.Error(ApiException(response.message), response.message)
                }
            } catch (e: Exception) {
                logger.e(TAG_FINANCE_REPO, "Error getting loan summary: ${e.message}", e)
                Result.Error(e, e.message ?: "Failed to get loan summary")
            }
        }
    }

    // ==================== Loan Payments ====================

    override suspend fun getLoanPayments(
        vehicleId: Int,
        page: Int,
        perPage: Int,
        status: String?
    ): Result<List<LoanPayment>> {
        return withContext(dispatcherProvider.io) {
            try {
                val token = requireAuthToken()

                val response = remoteDataSource.getLoanPayments(token, vehicleId, page, perPage, status)
                if (response.success && response.data != null) {
                    Result.Success(response.data.items.toDomainPayments())
                } else {
                    Result.Error(ApiException(response.message), response.message)
                }
            } catch (e: Exception) {
                logger.e(TAG_FINANCE_REPO, "Error getting loan payments: ${e.message}", e)
                Result.Error(e, e.message ?: "Failed to get loan payments")
            }
        }
    }

    override suspend fun getAllLoanPayments(
        page: Int,
        perPage: Int,
        vehicleId: Int?,
        status: String?
    ): Result<List<LoanPayment>> {
        return withContext(dispatcherProvider.io) {
            try {
                val token = requireAuthToken()

                val response = remoteDataSource.getAllLoanPayments(token, page, perPage, vehicleId, status)
                if (response.success && response.data != null) {
                    Result.Success(response.data.items.toDomainPayments())
                } else {
                    Result.Error(ApiException(response.message), response.message)
                }
            } catch (e: Exception) {
                logger.e(TAG_FINANCE_REPO, "Error getting all loan payments: ${e.message}", e)
                Result.Error(e, e.message ?: "Failed to get all loan payments")
            }
        }
    }

    override suspend fun getPaymentById(paymentId: Int): Result<LoanPayment> {
        return withContext(dispatcherProvider.io) {
            try {
                val token = requireAuthToken()

                val response = remoteDataSource.getPaymentById(token, paymentId)
                if (response.success && response.data != null) {
                    Result.Success(response.data.toDomain())
                } else {
                    Result.Error(ApiException(response.message), response.message)
                }
            } catch (e: Exception) {
                logger.e(TAG_FINANCE_REPO, "Error getting payment by ID: ${e.message}", e)
                Result.Error(e, e.message ?: "Failed to get payment details")
            }
        }
    }

    override suspend fun updatePayment(
        paymentId: Int,
        paymentMode: PaymentMode?,
        paymentSource: String?,
        transactionRef: String?,
        notes: String?
    ): Result<LoanPayment> {
        return withContext(dispatcherProvider.io) {
            try {
                val token = requireAuthToken()

                val request = UpdatePaymentRequest(
                    paymentMode = paymentMode?.value,
                    paymentSource = paymentSource,
                    transactionRef = transactionRef,
                    notes = notes
                )

                val response = remoteDataSource.updatePayment(token, paymentId, request)
                if (response.success && response.data != null) {
                    Result.Success(response.data.toDomain())
                } else {
                    Result.Error(ApiException(response.message), response.message)
                }
            } catch (e: Exception) {
                logger.e(TAG_FINANCE_REPO, "Error updating payment: ${e.message}", e)
                Result.Error(e, e.message ?: "Failed to update payment")
            }
        }
    }

    override suspend fun recordPayment(
        vehiclePurchaseId: Int,
        amount: Double,
        paymentDate: String,
        paymentMode: PaymentMode?,
        paymentSource: String?,
        transactionRef: String?,
        lateFee: Double?,
        prepaymentAmount: Double?,
        notes: String?
    ): Result<LoanPayment> {
        return withContext(dispatcherProvider.io) {
            try {
                val token = requireAuthToken()

                val request = RecordPaymentRequest(
                    vehiclePurchaseId = vehiclePurchaseId,
                    amount = amount,
                    paymentDate = paymentDate,
                    paymentMode = paymentMode?.value,
                    paymentSource = paymentSource,
                    transactionRef = transactionRef,
                    lateFee = lateFee,
                    prepaymentAmount = prepaymentAmount,
                    notes = notes
                )

                val response = remoteDataSource.recordPayment(token, request)
                if (response.success && response.data != null) {
                    Result.Success(response.data.toDomain())
                } else {
                    Result.Error(ApiException(response.message), response.message)
                }
            } catch (e: Exception) {
                logger.e(TAG_FINANCE_REPO, "Error recording payment: ${e.message}", e)
                Result.Error(e, e.message ?: "Failed to record payment")
            }
        }
    }

    override suspend fun markEmiPaid(
        paymentId: Int,
        paymentDate: String,
        paymentMode: PaymentMode?,
        transactionRef: String?,
        notes: String?
    ): Result<LoanPayment> {
        return withContext(dispatcherProvider.io) {
            try {
                val token = requireAuthToken()

                val request = MarkEmiPaidRequest(
                    paymentDate = paymentDate,
                    paymentMode = paymentMode?.value,
                    transactionRef = transactionRef,
                    notes = notes
                )

                val response = remoteDataSource.markEmiPaid(token, paymentId, request)
                if (response.success && response.data != null) {
                    Result.Success(response.data.toDomain())
                } else {
                    Result.Error(ApiException(response.message), response.message)
                }
            } catch (e: Exception) {
                logger.e(TAG_FINANCE_REPO, "Error marking EMI paid: ${e.message}", e)
                Result.Error(e, e.message ?: "Failed to mark EMI paid")
            }
        }
    }

    override suspend fun deletePayment(paymentId: Int): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            try {
                val token = requireAuthToken()

                val response = remoteDataSource.deletePayment(token, paymentId)
                if (response.success) {
                    Result.Success(Unit)
                } else {
                    Result.Error(ApiException(response.message), response.message)
                }
            } catch (e: Exception) {
                logger.e(TAG_FINANCE_REPO, "Error deleting payment: ${e.message}", e)
                Result.Error(e, e.message ?: "Failed to delete payment")
            }
        }
    }

    // ==================== Alerts ====================

    override suspend fun getUpcomingEmis(days: Int): Result<List<EmiAlert>> {
        return withContext(dispatcherProvider.io) {
            try {
                val token = requireAuthToken()

                val response = remoteDataSource.getUpcomingEmis(token, days)
                if (response.success && response.data != null) {
                    Result.Success(response.data.upcoming.toDomainAlerts())
                } else {
                    Result.Error(ApiException(response.message), response.message)
                }
            } catch (e: Exception) {
                logger.e(TAG_FINANCE_REPO, "Error getting upcoming EMIs: ${e.message}", e)
                Result.Error(e, e.message ?: "Failed to get upcoming EMIs")
            }
        }
    }

    override suspend fun getOverdueEmis(): Result<List<EmiAlert>> {
        return withContext(dispatcherProvider.io) {
            try {
                val token = requireAuthToken()

                val response = remoteDataSource.getOverdueEmis(token)
                if (response.success && response.data != null) {
                    Result.Success(response.data.overdue.toDomainAlerts())
                } else {
                    Result.Error(ApiException(response.message), response.message)
                }
            } catch (e: Exception) {
                logger.e(TAG_FINANCE_REPO, "Error getting overdue EMIs: ${e.message}", e)
                Result.Error(e, e.message ?: "Failed to get overdue EMIs")
            }
        }
    }

    override suspend fun getEmiAlerts(): Result<Pair<List<EmiAlert>, List<EmiAlert>>> {
        return withContext(dispatcherProvider.io) {
            try {
                val token = requireAuthToken()

                // Fetch both upcoming and overdue
                val upcomingResponse = remoteDataSource.getUpcomingEmis(token, 30)
                val overdueResponse = remoteDataSource.getOverdueEmis(token)

                val upcoming = upcomingResponse.data?.upcoming?.toDomainAlerts() ?: emptyList()
                val overdue = overdueResponse.data?.overdue?.toDomainAlerts() ?: emptyList()

                Result.Success(Pair(upcoming, overdue))
            } catch (e: Exception) {
                logger.e(TAG_FINANCE_REPO, "Error getting EMI alerts: ${e.message}", e)
                Result.Error(e, e.message ?: "Failed to get EMI alerts")
            }
        }
    }
}
