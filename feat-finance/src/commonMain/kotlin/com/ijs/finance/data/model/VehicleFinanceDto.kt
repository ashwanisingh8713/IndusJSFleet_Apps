package com.ijs.finance.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ==================== Vehicle Purchase DTOs ====================

/**
 * Vehicle Purchase DTO from API response
 */
@Serializable
data class VehiclePurchaseDto(
    val id: Int = 0,
    @SerialName("vehicle_id")
    val vehicleId: Int = 0,
    val vehicle: VehicleBasicInfoDto? = null,
    @SerialName("purchase_date")
    val purchaseDate: String = "",
    @SerialName("purchase_price")
    val purchasePrice: Double = 0.0,
    @SerialName("vendor_name")
    val vendorName: String? = null,
    @SerialName("invoice_number")
    val invoiceNumber: String? = null,
    @SerialName("payment_type")
    val paymentType: String = "cash",
    @SerialName("down_payment")
    val downPayment: Double = 0.0,
    @SerialName("loan_amount")
    val loanAmount: Double? = null,
    @SerialName("interest_rate")
    val interestRate: Double? = null,
    @SerialName("tenure_months")
    val tenureMonths: Int? = null,
    @SerialName("emi_amount")
    val emiAmount: Double? = null,
    @SerialName("loan_start_date")
    val loanStartDate: String? = null,
    @SerialName("loan_end_date")
    val loanEndDate: String? = null,
    @SerialName("financier_name")
    val financierName: String? = null,
    @SerialName("loan_account_number")
    val loanAccountNumber: String? = null,
    @SerialName("bank_name")
    val bankName: String? = null,
    @SerialName("bank_account_number")
    val bankAccountNumber: String? = null,
    @SerialName("bank_ifsc")
    val bankIfsc: String? = null,
    @SerialName("auto_debit_enabled")
    val autoDebitEnabled: Boolean = false,
    @SerialName("total_paid")
    val totalPaid: Double = 0.0,
    @SerialName("outstanding_balance")
    val outstandingBalance: Double = 0.0,
    @SerialName("emis_paid")
    val emisPaid: Int = 0,
    @SerialName("emis_remaining")
    val emisRemaining: Int = 0,
    @SerialName("next_emi_due_date")
    val nextEmiDueDate: String? = null,
    @SerialName("loan_status")
    val loanStatus: String = "not_applicable",
    val notes: String? = null,
    @SerialName("owner_id")
    val ownerId: Int = 0,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Basic vehicle info embedded in purchase response
 */
@Serializable
data class VehicleBasicInfoDto(
    val id: Int = 0,
    @SerialName("registration_number")
    val registrationNumber: String = "",
    val make: String = "",
    val model: String = ""
)

/**
 * Request to create vehicle purchase
 */
@Serializable
data class CreatePurchaseRequest(
    @SerialName("purchase_date")
    val purchaseDate: String,
    @SerialName("purchase_price")
    val purchasePrice: Double,
    @SerialName("vendor_name")
    val vendorName: String? = null,
    @SerialName("invoice_number")
    val invoiceNumber: String? = null,
    @SerialName("payment_type")
    val paymentType: String,
    @SerialName("down_payment")
    val downPayment: Double? = null,
    @SerialName("loan_amount")
    val loanAmount: Double? = null,
    @SerialName("interest_rate")
    val interestRate: Double? = null,
    @SerialName("tenure_months")
    val tenureMonths: Int? = null,
    @SerialName("emi_amount")
    val emiAmount: Double? = null,
    @SerialName("loan_start_date")
    val loanStartDate: String? = null,
    @SerialName("financier_name")
    val financierName: String? = null,
    @SerialName("loan_account_number")
    val loanAccountNumber: String? = null,
    @SerialName("bank_name")
    val bankName: String? = null,
    @SerialName("bank_account_number")
    val bankAccountNumber: String? = null,
    @SerialName("bank_ifsc")
    val bankIfsc: String? = null,
    @SerialName("auto_debit_enabled")
    val autoDebitEnabled: Boolean? = null,
    val notes: String? = null
)

/**
 * Request to update vehicle purchase
 */
@Serializable
data class UpdatePurchaseRequest(
    @SerialName("vendor_name")
    val vendorName: String? = null,
    @SerialName("invoice_number")
    val invoiceNumber: String? = null,
    @SerialName("bank_name")
    val bankName: String? = null,
    @SerialName("bank_account_number")
    val bankAccountNumber: String? = null,
    @SerialName("bank_ifsc")
    val bankIfsc: String? = null,
    @SerialName("auto_debit_enabled")
    val autoDebitEnabled: Boolean? = null,
    val notes: String? = null
)

// ==================== Loan Payment DTOs ====================

/**
 * Loan Payment DTO from API response
 */
@Serializable
data class LoanPaymentDto(
    val id: Int = 0,
    @SerialName("vehicle_purchase_id")
    val vehiclePurchaseId: Int = 0,
    @SerialName("vehicle_id")
    val vehicleId: Int = 0,
    @SerialName("emi_number")
    val emiNumber: Int? = null,
    @SerialName("due_date")
    val dueDate: String? = null,
    val amount: Double = 0.0,
    @SerialName("payment_date")
    val paymentDate: String? = null,
    @SerialName("principal_amount")
    val principalAmount: Double? = null,
    @SerialName("interest_amount")
    val interestAmount: Double? = null,
    @SerialName("late_fee")
    val lateFee: Double? = null,
    @SerialName("prepayment_amount")
    val prepaymentAmount: Double? = null,
    @SerialName("entry_type")
    val entryType: String = "manual",
    @SerialName("payment_mode")
    val paymentMode: String? = null,
    @SerialName("payment_source")
    val paymentSource: String? = null,
    @SerialName("payment_status")
    val paymentStatus: String = "pending",
    @SerialName("transaction_ref")
    val transactionRef: String? = null,
    val notes: String? = null,
    @SerialName("owner_id")
    val ownerId: Int = 0,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Request to record EMI payment (minimal - only amount and date required)
 */
@Serializable
data class RecordPaymentRequest(
    @SerialName("vehicle_purchase_id")
    val vehiclePurchaseId: Int,
    val amount: Double,
    @SerialName("payment_date")
    val paymentDate: String,
    @SerialName("payment_mode")
    val paymentMode: String? = null,
    @SerialName("payment_source")
    val paymentSource: String? = null,
    @SerialName("transaction_ref")
    val transactionRef: String? = null,
    @SerialName("late_fee")
    val lateFee: Double? = null,
    @SerialName("prepayment_amount")
    val prepaymentAmount: Double? = null,
    val notes: String? = null
)

/**
 * Request to mark scheduled EMI as paid
 */
@Serializable
data class MarkEmiPaidRequest(
    @SerialName("payment_date")
    val paymentDate: String,
    @SerialName("payment_mode")
    val paymentMode: String? = null,
    @SerialName("transaction_ref")
    val transactionRef: String? = null,
    val notes: String? = null
)

/**
 * Request to update an existing loan payment
 */
@Serializable
data class UpdatePaymentRequest(
    @SerialName("payment_mode")
    val paymentMode: String? = null,
    @SerialName("payment_source")
    val paymentSource: String? = null,
    @SerialName("transaction_ref")
    val transactionRef: String? = null,
    val notes: String? = null
)

// ==================== Loan Summary DTO ====================

@Serializable
data class LoanSummaryDto(
    @SerialName("vehicle_id")
    val vehicleId: Int = 0,
    @SerialName("vehicle_purchase_id")
    val vehiclePurchaseId: Int = 0,
    val vehicle: VehicleBasicInfoDto? = null,
    @SerialName("loan_amount")
    val loanAmount: Double = 0.0,
    @SerialName("emi_amount")
    val emiAmount: Double = 0.0,
    @SerialName("tenure_months")
    val tenureMonths: Int = 0,
    @SerialName("interest_rate")
    val interestRate: Double = 0.0,
    @SerialName("total_paid")
    val totalPaid: Double = 0.0,
    @SerialName("outstanding_balance")
    val outstandingBalance: Double = 0.0,
    @SerialName("emis_paid")
    val emisPaid: Int = 0,
    @SerialName("emis_remaining")
    val emisRemaining: Int = 0,
    @SerialName("next_emi_due_date")
    val nextEmiDueDate: String? = null,
    @SerialName("next_emi_amount")
    val nextEmiAmount: Double = 0.0,
    @SerialName("loan_status")
    val loanStatus: String = "active",
    @SerialName("financier_name")
    val financierName: String? = null
)

// ==================== EMI Alert DTO ====================

@Serializable
data class EmiAlertDto(
    @SerialName("payment_id")
    val paymentId: Int = 0,
    @SerialName("vehicle_id")
    val vehicleId: Int = 0,
    @SerialName("vehicle_purchase_id")
    val vehiclePurchaseId: Int = 0,
    val vehicle: VehicleBasicInfoDto? = null,
    @SerialName("emi_number")
    val emiNumber: Int = 0,
    @SerialName("due_date")
    val dueDate: String = "",
    val amount: Double = 0.0,
    @SerialName("days_until_due")
    val daysUntilDue: Int = 0,
    @SerialName("days_overdue")
    val daysOverdue: Int = 0,
    @SerialName("is_overdue")
    val isOverdue: Boolean = false,
    @SerialName("financier_name")
    val financierName: String? = null
)

// ==================== API Response Wrappers ====================

@Serializable
data class VehiclePurchaseResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: VehiclePurchaseDto? = null
)

@Serializable
data class LoanSummaryResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: LoanSummaryDto? = null
)

@Serializable
data class LoanPaymentResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: LoanPaymentDto? = null
)

@Serializable
data class LoanPaymentsListResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: LoanPaymentsListData? = null
)

@Serializable
data class LoanPaymentsListData(
    val items: List<LoanPaymentDto> = emptyList(),
    val count: Int = 0,
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1,
    @SerialName("has_more")
    val hasMore: Boolean = false
)

@Serializable
data class EmiAlertsResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: EmiAlertsData? = null
)

@Serializable
data class EmiAlertsData(
    val upcoming: List<EmiAlertDto> = emptyList(),
    val overdue: List<EmiAlertDto> = emptyList()
)

@Serializable
data class DeleteResponse(
    val success: Boolean = false,
    val message: String = ""
)
