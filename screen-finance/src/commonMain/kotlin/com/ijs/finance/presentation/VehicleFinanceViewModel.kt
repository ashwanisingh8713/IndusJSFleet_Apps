package com.ijs.finance.presentation

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.finance.TAG_FINANCE_VM
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.uicomponents.components.UiText
import com.ijs.finance.domain.entity.PaymentType
import com.ijs.finance.domain.repository.VehicleFinanceRepository
import com.ijs.vehicle.domain.repository.VehicleRepository
import com.ijs.finance.presentation.VehicleFinanceContract.Effect
import com.ijs.finance.presentation.VehicleFinanceContract.Intent
import com.ijs.finance.presentation.VehicleFinanceContract.State
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_down_payment_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_financier_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_interest_rate_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_loan_start_date_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_payment_date_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_purchase_date_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_purchase_price_invalid
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_tenure_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_valid_amount
import indusjsfleet.ijs_ui_components_lib.generated.resources.finance_payment_record_failed
import indusjsfleet.ijs_ui_components_lib.generated.resources.finance_save_failed
import indusjsfleet.ijs_ui_components_lib.generated.resources.success_payment_recorded
import indusjsfleet.ijs_ui_components_lib.generated.resources.success_purchase_saved
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect

@Inject
class VehicleFinanceViewModel(
    private val vehicleRepository: VehicleRepository,
    private val financeRepository: VehicleFinanceRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val logger: FleetLogger
) : MviViewModel<State, Intent, Effect>(State()) {
init {
        sendIntent(Intent.LoadData)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadData -> loadData()
            is Intent.Refresh -> refresh()
            is Intent.SelectVehicle -> selectVehicle(intent.vehicleId)
            is Intent.ClearSelection -> clearSelection()

            is Intent.SetFilter -> updateState { copy(selectedFilter = intent.filter) }
            is Intent.UpdateSearch -> updateState { copy(searchQuery = intent.query) }

            // Add Purchase form
            is Intent.ShowAddPurchaseSheet -> updateState { copy(showAddPurchaseSheet = true) }
            is Intent.HideAddPurchaseSheet -> hideAddPurchaseSheet()
            is Intent.SelectVehicleForPurchase -> updateState { copy(formVehicleId = intent.vehicleId) }
            is Intent.UpdatePurchaseDate -> updateState { copy(formPurchaseDate = intent.date, purchaseDateError = null) }
            is Intent.UpdatePurchasePrice -> updateState { copy(formPurchasePrice = intent.price, purchasePriceError = null) }
            is Intent.UpdateVendorName -> updateState { copy(formVendorName = intent.name) }
            is Intent.UpdateInvoiceNumber -> updateState { copy(formInvoiceNumber = intent.number) }
            is Intent.UpdatePaymentType -> updateState { copy(formPaymentType = intent.type) }
            is Intent.UpdateDownPayment -> updateState { copy(formDownPayment = intent.amount, downPaymentError = null) }
            is Intent.UpdateInterestRate -> updateState { copy(formInterestRate = intent.rate, interestRateError = null) }
            is Intent.UpdateTenureMonths -> updateState { copy(formTenureMonths = intent.months, tenureError = null) }
            is Intent.UpdateLoanStartDate -> updateState { copy(formLoanStartDate = intent.date, loanStartDateError = null) }
            is Intent.UpdateFinancierName -> updateState { copy(formFinancierName = intent.name, financierError = null) }
            is Intent.UpdateLoanAccountNumber -> updateState { copy(formLoanAccountNumber = intent.number) }
            is Intent.UpdateBankName -> updateState { copy(formBankName = intent.name) }
            is Intent.UpdateBankAccountNumber -> updateState { copy(formBankAccountNumber = intent.number) }
            is Intent.UpdateBankIfsc -> updateState { copy(formBankIfsc = intent.ifsc) }
            is Intent.UpdateAutoDebitEnabled -> updateState { copy(formAutoDebitEnabled = intent.enabled) }
            is Intent.UpdateNotes -> updateState { copy(formNotes = intent.notes) }
            is Intent.SavePurchase -> savePurchase()

            // Record Payment form
            is Intent.ShowRecordPaymentSheet -> updateState { copy(showRecordPaymentSheet = true) }
            is Intent.HideRecordPaymentSheet -> hideRecordPaymentSheet()
            is Intent.UpdatePaymentAmount -> updateState { copy(paymentAmount = intent.amount, paymentAmountError = null) }
            is Intent.UpdatePaymentDate -> updateState { copy(paymentDate = intent.date, paymentDateError = null) }
            is Intent.UpdatePaymentMode -> updateState { copy(paymentMode = intent.mode) }
            is Intent.UpdatePaymentSource -> updateState { copy(paymentSource = intent.source) }
            is Intent.UpdateTransactionRef -> updateState { copy(transactionRef = intent.ref) }
            is Intent.UpdateLateFee -> updateState { copy(lateFee = intent.fee) }
            is Intent.UpdatePaymentNotes -> updateState { copy(paymentNotes = intent.notes) }
            is Intent.RecordPayment -> recordPayment()

            // Payment detail
            is Intent.ShowPaymentDetail -> updateState {
                copy(showPaymentDetailSheet = true, selectedPaymentForDetail = intent.payment)
            }
            is Intent.HidePaymentDetail -> updateState {
                copy(showPaymentDetailSheet = false, selectedPaymentForDetail = null)
            }

            is Intent.NavigateToDetail -> sendEffect(Effect.NavigateToDetail(intent.vehicleId))
            is Intent.NavigateToAddPurchase -> sendEffect(Effect.NavigateToAddPurchase)
        }
    }

    private suspend fun loadData() {
        updateState { copy(isLoading = true, error = null) }

        try {
            // Load vehicles
            vehicleRepository.getVehicles().collect { vehiclesResult ->
                when (vehiclesResult) {
                    is Result.Success -> {
                        val vehicleList = vehiclesResult.data
                        updateState { copy(vehicles = vehicleList, totalVehicles = vehicleList.size) }

                        // Load purchase info for each vehicle
                        loadPurchaseInfoForVehicles(vehicleList.map { vehicle -> vehicle.id.toIntOrNull() ?: 0 })
                    }
                    is Result.Error -> {
                        updateState {
                            copy(
                                isLoading = false,
                                error = vehiclesResult.message?.let { UiText.Raw(it) }
                            )
                        }
                    }
                    is Result.Loading -> { /* Already loading */ }
                }
            }

            // Load EMI alerts
            loadEmiAlerts()

        } catch (e: Exception) {
            logger.e(TAG_FINANCE_VM, "Error loading data: ${e.message}", e)
            updateState { copy(isLoading = false, error = e.message?.let { UiText.Raw(it) }) }
        }
    }

    private suspend fun loadPurchaseInfoForVehicles(vehicleIds: List<Int>) {
        val purchasesMap = mutableMapOf<Int, com.ijs.finance.domain.entity.VehiclePurchase?>()
        var financedCount = 0
        var cashCount = 0
        var noInfoCount = 0
        var totalLoan = 0.0
        var monthlyEmi = 0.0
        var totalPaidAmount = 0.0
        var totalOutstandingAmount = 0.0

        // Parallelize purchase info loading instead of sequential N+1 requests
        val validIds = vehicleIds.filter { it > 0 }
        val results = coroutineScope {
            validIds.map { vehicleId ->
                async { vehicleId to financeRepository.getPurchase(vehicleId) }
            }.awaitAll()
        }

        for ((vehicleId, result) in results) {
            when (result) {
                is Result.Success -> {
                    val purchase = result.data
                    purchasesMap[vehicleId] = purchase

                    when {
                        purchase == null -> noInfoCount++
                        purchase.isFinanced -> {
                            financedCount++
                            totalLoan += purchase.loanAmount
                            monthlyEmi += purchase.emiAmount
                            totalPaidAmount += purchase.totalPaid
                            totalOutstandingAmount += purchase.outstandingBalance
                        }
                        else -> {
                            cashCount++
                            totalPaidAmount += purchase.purchasePrice
                        }
                    }
                }
                is Result.Error -> {
                    purchasesMap[vehicleId] = null
                    noInfoCount++
                }
                is Result.Loading -> { /* Skip */ }
            }
        }

        updateState {
            copy(
                isLoading = false,
                vehiclePurchases = purchasesMap,
                financedVehicles = financedCount,
                cashVehicles = cashCount,
                pendingVehicles = noInfoCount,
                totalLoanAmount = totalLoan,
                monthlyEmiTotal = monthlyEmi,
                totalPaid = totalPaidAmount,
                totalOutstanding = totalOutstandingAmount
            )
        }
    }

    private suspend fun loadEmiAlerts() {
        when (val result = financeRepository.getEmiAlerts()) {
            is Result.Success -> {
                val (upcoming, overdue) = result.data
                updateState {
                    copy(
                        upcomingAlerts = upcoming,
                        overdueAlerts = overdue
                    )
                }
            }
            is Result.Error -> {
                logger.w(TAG_FINANCE_VM, "Failed to load EMI alerts: ${result.message}")
            }
            is Result.Loading -> { /* Skip */ }
        }
    }

    private suspend fun refresh() {
        updateState { copy(isRefreshing = true) }
        loadData()
        updateState { copy(isRefreshing = false) }
    }

    private suspend fun selectVehicle(vehicleId: Int) {
        val currentState = state.value

        // Skip API call if same vehicle is already selected (regardless of whether purchase exists or not)
        // This prevents re-fetching when navigating back from Payment History
        if (currentState.selectedVehicleId == vehicleId && !currentState.isLoading) {
            logger.d(TAG_FINANCE_VM, "selectVehicle: Vehicle $vehicleId already selected, skipping API call")
            return
        }

        updateState { copy(selectedVehicleId = vehicleId, isLoading = true, error = null) }

        // Load purchase details
        when (val purchaseResult = financeRepository.getPurchase(vehicleId)) {
            is Result.Success -> {
                updateState { copy(selectedPurchase = purchaseResult.data) }

                // If financed, load loan summary and payments
                if (purchaseResult.data?.isFinanced == true) {
                    loadLoanDetails(vehicleId)
                }
            }
            is Result.Error -> {
                updateState {
                    copy(
                        isLoading = false,
                        error = purchaseResult.message?.let { UiText.Raw(it) }
                    )
                }
                return
            }
            is Result.Loading -> { /* Already loading */ }
        }

        updateState { copy(isLoading = false) }
    }

    private suspend fun loadLoanDetails(vehicleId: Int) {
        // Load loan summary
        when (val summaryResult = financeRepository.getLoanSummary(vehicleId)) {
            is Result.Success -> {
                updateState { copy(loanSummary = summaryResult.data) }
            }
            is Result.Error -> {
                logger.w(TAG_FINANCE_VM, "Failed to load loan summary: ${summaryResult.message}")
            }
            is Result.Loading -> { /* Skip */ }
        }

        // Load payments - fetch up to 100 to ensure all payments are loaded
        when (val paymentsResult = financeRepository.getLoanPayments(vehicleId, page = 1, perPage = 100)) {
            is Result.Success -> {
                updateState { copy(loanPayments = paymentsResult.data) }
            }
            is Result.Error -> {
                logger.w(TAG_FINANCE_VM, "Failed to load loan payments: ${paymentsResult.message}")
            }
            is Result.Loading -> { /* Skip */ }
        }
    }

    private fun clearSelection() {
        updateState {
            copy(
                selectedVehicleId = null,
                selectedPurchase = null,
                loanSummary = null,
                loanPayments = emptyList()
            )
        }
    }

    private fun hideAddPurchaseSheet() {
        updateState {
            copy(
                showAddPurchaseSheet = false,
                formVehicleId = null,
                formPurchaseDate = "",
                formPurchasePrice = "",
                formVendorName = "",
                formInvoiceNumber = "",
                formPaymentType = PaymentType.CASH,
                formDownPayment = "",
                formInterestRate = "",
                formTenureMonths = "",
                formLoanStartDate = "",
                formFinancierName = "",
                formLoanAccountNumber = "",
                formBankName = "",
                formBankAccountNumber = "",
                formBankIfsc = "",
                formAutoDebitEnabled = false,
                formNotes = "",
                purchaseDateError = null,
                purchasePriceError = null,
                downPaymentError = null,
                interestRateError = null,
                tenureError = null,
                financierError = null,
                loanStartDateError = null
            )
        }
    }

    private suspend fun savePurchase() {
        val currentState = state.value

        // Validation
        var hasError = false

        if (currentState.formPurchaseDate.isBlank()) {
            updateState { copy(purchaseDateError = UiText.StringRes(Res.string.error_purchase_date_required)) }
            hasError = true
        }

        val price = currentState.formPurchasePrice.toDoubleOrNull()
        if (price == null || price <= 0) {
            updateState { copy(purchasePriceError = UiText.StringRes(Res.string.error_purchase_price_invalid)) }
            hasError = true
        }

        if (currentState.formPaymentType == PaymentType.LOAN) {
            if (currentState.formDownPayment.toDoubleOrNull() == null) {
                updateState { copy(downPaymentError = UiText.StringRes(Res.string.error_down_payment_required)) }
                hasError = true
            }
            if (currentState.formInterestRate.toDoubleOrNull() == null) {
                updateState { copy(interestRateError = UiText.StringRes(Res.string.error_interest_rate_required)) }
                hasError = true
            }
            val tenure = currentState.formTenureMonths.toIntOrNull()
            if (tenure == null || tenure <= 0) {
                updateState { copy(tenureError = UiText.StringRes(Res.string.error_tenure_required)) }
                hasError = true
            }
            if (currentState.formFinancierName.isBlank()) {
                updateState { copy(financierError = UiText.StringRes(Res.string.error_financier_required)) }
                hasError = true
            }
            if (currentState.formLoanStartDate.isBlank()) {
                updateState { copy(loanStartDateError = UiText.StringRes(Res.string.error_loan_start_date_required)) }
                hasError = true
            }
        }

        if (hasError || currentState.formVehicleId == null) return

        updateState { copy(isSaving = true) }

        // Calculate loan amount and EMI for loan purchases
        val isLoan = currentState.formPaymentType == PaymentType.LOAN
        val loanAmount = if (isLoan) currentState.calculatedLoanAmount.takeIf { it > 0 } else null
        val emiAmount = if (isLoan) currentState.calculatedEmi.takeIf { it > 0 } else null

        val result = financeRepository.createPurchase(
            vehicleId = currentState.formVehicleId,
            purchaseDate = currentState.formPurchaseDate,
            purchasePrice = price!!,
            vendorName = currentState.formVendorName.takeIf { it.isNotBlank() },
            invoiceNumber = currentState.formInvoiceNumber.takeIf { it.isNotBlank() },
            paymentType = currentState.formPaymentType,
            downPayment = currentState.formDownPayment.toDoubleOrNull(),
            loanAmount = loanAmount,
            interestRate = currentState.formInterestRate.toDoubleOrNull(),
            tenureMonths = currentState.formTenureMonths.toIntOrNull(),
            emiAmount = emiAmount,
            loanStartDate = currentState.formLoanStartDate.takeIf { it.isNotBlank() },
            financierName = currentState.formFinancierName.takeIf { it.isNotBlank() },
            loanAccountNumber = currentState.formLoanAccountNumber.takeIf { it.isNotBlank() },
            bankName = currentState.formBankName.takeIf { it.isNotBlank() },
            bankAccountNumber = currentState.formBankAccountNumber.takeIf { it.isNotBlank() },
            bankIfsc = currentState.formBankIfsc.takeIf { it.isNotBlank() },
            autoDebitEnabled = currentState.formAutoDebitEnabled,
            notes = currentState.formNotes.takeIf { it.isNotBlank() }
        )

        when (result) {
            is Result.Success -> {
                updateState { copy(isSaving = false) }
                hideAddPurchaseSheet()
                sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.success_purchase_saved)))
                sendEffect(Effect.PurchaseSaved)
                loadData() // Refresh
            }
            is Result.Error -> {
                updateState { copy(isSaving = false, error = result.message?.let { UiText.Raw(it) }) }
                sendEffect(
                    Effect.ShowSnackbar(
                        result.message?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.finance_save_failed)
                    )
                )
            }
            is Result.Loading -> { /* Skip */ }
        }
    }

    private fun hideRecordPaymentSheet() {
        updateState {
            copy(
                showRecordPaymentSheet = false,
                paymentAmount = "",
                paymentDate = "",
                paymentMode = null,
                paymentSource = "",
                transactionRef = "",
                lateFee = "",
                paymentNotes = "",
                paymentAmountError = null,
                paymentDateError = null
            )
        }
    }

    private suspend fun recordPayment() {
        val currentState = state.value
        val purchase = currentState.selectedPurchase ?: return

        // Validation
        var hasError = false

        val amount = currentState.paymentAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            updateState { copy(paymentAmountError = UiText.StringRes(Res.string.error_valid_amount)) }
            hasError = true
        }

        if (currentState.paymentDate.isBlank()) {
            updateState { copy(paymentDateError = UiText.StringRes(Res.string.error_payment_date_required)) }
            hasError = true
        }

        if (hasError) return

        updateState { copy(isSaving = true) }

        val result = financeRepository.recordPayment(
            vehiclePurchaseId = purchase.id,
            amount = amount!!,
            paymentDate = currentState.paymentDate,
            paymentMode = currentState.paymentMode,
            paymentSource = currentState.paymentSource.takeIf { it.isNotBlank() },
            transactionRef = currentState.transactionRef.takeIf { it.isNotBlank() },
            lateFee = currentState.lateFee.toDoubleOrNull(),
            prepaymentAmount = null,
            notes = currentState.paymentNotes.takeIf { it.isNotBlank() }
        )

        when (result) {
            is Result.Success -> {
                updateState { copy(isSaving = false) }
                hideRecordPaymentSheet()
                sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.success_payment_recorded)))
                sendEffect(Effect.PaymentRecorded)

                // Refresh current vehicle details
                currentState.selectedVehicleId?.let { selectVehicle(it) }
                loadData() // Refresh summary
            }
            is Result.Error -> {
                updateState { copy(isSaving = false, error = result.message?.let { UiText.Raw(it) }) }
                sendEffect(
                    Effect.ShowSnackbar(
                        result.message?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.finance_payment_record_failed)
                    )
                )
            }
            is Result.Loading -> { /* Skip */ }
        }
    }
}
