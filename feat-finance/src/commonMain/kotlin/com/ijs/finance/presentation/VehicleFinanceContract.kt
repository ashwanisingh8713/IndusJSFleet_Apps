package com.ijs.finance.presentation

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.ijs.finance.domain.entity.*
import com.ijs.vehicle.domain.entity.Vehicle
import kotlin.math.pow

/**
 * MVI Contract for Vehicle Finance screens.
 */
object VehicleFinanceContract {

    // ==================== State ====================

    data class State(
        // Loading states
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val isSaving: Boolean = false,

        // Error
        val error: String? = null,

        // Vehicles list (all vehicles to show finance status)
        val vehicles: List<Vehicle> = emptyList(),
        val vehiclePurchases: Map<Int, VehiclePurchase?> = emptyMap(), // vehicleId -> purchase

        // Filter
        val selectedFilter: FinanceFilter = FinanceFilter.ALL,
        val searchQuery: String = "",

        // Summary
        val totalVehicles: Int = 0,
        val financedVehicles: Int = 0,
        val cashVehicles: Int = 0,
        val pendingVehicles: Int = 0,
        val totalLoanAmount: Double = 0.0,
        val monthlyEmiTotal: Double = 0.0,
        val totalPaid: Double = 0.0,
        val totalOutstanding: Double = 0.0,

        // Alerts
        val upcomingAlerts: List<EmiAlert> = emptyList(),
        val overdueAlerts: List<EmiAlert> = emptyList(),

        // Selected vehicle for detail/add
        val selectedVehicleId: Int? = null,
        val selectedPurchase: VehiclePurchase? = null,
        val loanSummary: LoanSummary? = null,
        val loanPayments: List<LoanPayment> = emptyList(),

        // Add/Edit form state
        val showAddPurchaseSheet: Boolean = false,
        val showRecordPaymentSheet: Boolean = false,

        // Form fields - Add Purchase
        val formVehicleId: Int? = null,
        val formPurchaseDate: String = "",
        val formPurchasePrice: String = "",
        val formVendorName: String = "",
        val formInvoiceNumber: String = "",
        val formPaymentType: PaymentType = PaymentType.CASH,
        val formDownPayment: String = "",
        val formInterestRate: String = "",
        val formTenureMonths: String = "",
        val formLoanStartDate: String = "",
        val formFinancierName: String = "",
        val formLoanAccountNumber: String = "",
        val formBankName: String = "",
        val formBankAccountNumber: String = "",
        val formBankIfsc: String = "",
        val formAutoDebitEnabled: Boolean = false,
        val formNotes: String = "",

        // Form fields - Record Payment
        val paymentAmount: String = "",
        val paymentDate: String = "",
        val paymentMode: PaymentMode? = null,
        val paymentSource: String = "",
        val transactionRef: String = "",
        val lateFee: String = "",
        val paymentNotes: String = "",

        // Payment detail bottom sheet
        val showPaymentDetailSheet: Boolean = false,
        val selectedPaymentForDetail: LoanPayment? = null,

        // Validation errors
        val purchaseDateError: String? = null,
        val purchasePriceError: String? = null,
        val downPaymentError: String? = null,
        val interestRateError: String? = null,
        val tenureError: String? = null,
        val financierError: String? = null,
        val loanStartDateError: String? = null,
        val paymentAmountError: String? = null,
        val paymentDateError: String? = null
    ) : UiState {

        // Computed properties
        val filteredVehicles: List<VehicleFinanceItem> get() {
            val items = vehicles.map { vehicle ->
                val purchase = vehiclePurchases[vehicle.id.toIntOrNull() ?: 0]
                VehicleFinanceItem(
                    vehicle = vehicle,
                    purchase = purchase,
                    status = when {
                        purchase == null -> FinanceStatus.PENDING
                        purchase.isFinanced -> FinanceStatus.LOAN
                        else -> FinanceStatus.CASH
                    }
                )
            }

            return items
                .filter { item ->
                    when (selectedFilter) {
                        FinanceFilter.ALL -> true
                        FinanceFilter.LOAN -> item.status == FinanceStatus.LOAN
                        FinanceFilter.CASH -> item.status == FinanceStatus.CASH
                        FinanceFilter.PENDING -> item.status == FinanceStatus.PENDING
                    }
                }
                .filter { item ->
                    searchQuery.isBlank() ||
                    item.vehicle.registrationNumber.contains(searchQuery, ignoreCase = true) ||
                    item.vehicle.make.contains(searchQuery, ignoreCase = true) ||
                    item.vehicle.model.contains(searchQuery, ignoreCase = true)
                }
        }

        val hasAlerts: Boolean get() = upcomingAlerts.isNotEmpty() || overdueAlerts.isNotEmpty()

        val vehiclesWithoutPurchase: List<Vehicle> get() =
            vehicles.filter { vehiclePurchases[it.id.toIntOrNull() ?: 0] == null }

        // EMI calculation preview
        val calculatedEmi: Double get() {
            val principal = formDownPayment.toDoubleOrNull()?.let { dp ->
                formPurchasePrice.toDoubleOrNull()?.minus(dp)
            } ?: 0.0
            val rate = formInterestRate.toDoubleOrNull() ?: 0.0
            val months = formTenureMonths.toIntOrNull() ?: 0

            if (principal <= 0 || months <= 0) return 0.0

            if (rate <= 0) return principal / months

            val monthlyRate = rate / 12 / 100
            val power = (1 + monthlyRate).pow(months.toDouble())
            return principal * monthlyRate * power / (power - 1)
        }

        val calculatedLoanAmount: Double get() {
            val price = formPurchasePrice.toDoubleOrNull() ?: 0.0
            val down = formDownPayment.toDoubleOrNull() ?: 0.0
            return (price - down).coerceAtLeast(0.0)
        }

        val calculatedTotalInterest: Double get() {
            val months = formTenureMonths.toIntOrNull() ?: 0
            val loanAmount = calculatedLoanAmount
            return if (months > 0 && calculatedEmi > 0) {
                (calculatedEmi * months) - loanAmount
            } else 0.0
        }

        val calculatedTotalPayable: Double get() {
            val months = formTenureMonths.toIntOrNull() ?: 0
            return if (months > 0) calculatedEmi * months else 0.0
        }

        val isFormValid: Boolean get() {
            if (formVehicleId == null) return false
            if (formPurchaseDate.isBlank()) return false
            if (formPurchasePrice.toDoubleOrNull() == null || formPurchasePrice.toDouble() <= 0) return false

            if (formPaymentType == PaymentType.LOAN) {
                if (formDownPayment.toDoubleOrNull() == null) return false
                if (formInterestRate.toDoubleOrNull() == null) return false
                if (formTenureMonths.toIntOrNull() == null || formTenureMonths.toInt() <= 0) return false
                if (formFinancierName.isBlank()) return false
                if (formLoanStartDate.isBlank()) return false
            }

            return true
        }

        val isPaymentFormValid: Boolean get() {
            return paymentAmount.toDoubleOrNull() != null &&
                   paymentAmount.toDouble() > 0 &&
                   paymentDate.isNotBlank()
        }
    }

    // ==================== Intent ====================

    sealed interface Intent : UiIntent {
        // Navigation
        data object LoadData : Intent
        data object Refresh : Intent
        data class SelectVehicle(val vehicleId: Int) : Intent
        data object ClearSelection : Intent

        // Filters
        data class SetFilter(val filter: FinanceFilter) : Intent
        data class UpdateSearch(val query: String) : Intent

        // Add Purchase
        data object ShowAddPurchaseSheet : Intent
        data object HideAddPurchaseSheet : Intent
        data class SelectVehicleForPurchase(val vehicleId: Int) : Intent
        data class UpdatePurchaseDate(val date: String) : Intent
        data class UpdatePurchasePrice(val price: String) : Intent
        data class UpdateVendorName(val name: String) : Intent
        data class UpdateInvoiceNumber(val number: String) : Intent
        data class UpdatePaymentType(val type: PaymentType) : Intent
        data class UpdateDownPayment(val amount: String) : Intent
        data class UpdateInterestRate(val rate: String) : Intent
        data class UpdateTenureMonths(val months: String) : Intent
        data class UpdateLoanStartDate(val date: String) : Intent
        data class UpdateFinancierName(val name: String) : Intent
        data class UpdateLoanAccountNumber(val number: String) : Intent
        data class UpdateBankName(val name: String) : Intent
        data class UpdateBankAccountNumber(val number: String) : Intent
        data class UpdateBankIfsc(val ifsc: String) : Intent
        data class UpdateAutoDebitEnabled(val enabled: Boolean) : Intent
        data class UpdateNotes(val notes: String) : Intent
        data object SavePurchase : Intent

        // Record Payment
        data object ShowRecordPaymentSheet : Intent
        data object HideRecordPaymentSheet : Intent
        data class UpdatePaymentAmount(val amount: String) : Intent
        data class UpdatePaymentDate(val date: String) : Intent
        data class UpdatePaymentMode(val mode: PaymentMode?) : Intent
        data class UpdatePaymentSource(val source: String) : Intent
        data class UpdateTransactionRef(val ref: String) : Intent
        data class UpdateLateFee(val fee: String) : Intent
        data class UpdatePaymentNotes(val notes: String) : Intent
        data object RecordPayment : Intent

        // Payment detail
        data class ShowPaymentDetail(val payment: LoanPayment) : Intent
        data object HidePaymentDetail : Intent

        // Navigation intents
        data class NavigateToDetail(val vehicleId: Int) : Intent
        data object NavigateToAddPurchase : Intent
    }

    // ==================== Effect ====================

    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data class NavigateToDetail(val vehicleId: Int) : Effect
        data object NavigateToAddPurchase : Effect
        data object NavigateBack : Effect
        data object PurchaseSaved : Effect
        data object PaymentRecorded : Effect
    }
}

// ==================== Supporting Types ====================

data class VehicleFinanceItem(
    val vehicle: Vehicle,
    val purchase: VehiclePurchase?,
    val status: FinanceStatus
)

enum class FinanceFilter(val label: String) {
    ALL("All"),
    LOAN("Financed"),
    CASH("Cash Purchase"),
    PENDING("Not Recorded")
}

enum class FinanceStatus {
    LOAN, CASH, PENDING
}
