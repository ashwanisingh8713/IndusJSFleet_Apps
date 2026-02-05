package com.indusjs.fleet.presentation.customers.detail

import co.touchlab.kermit.Logger
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.fleet.domain.repository.customer.CustomerRepository
import com.indusjs.fleet.domain.entity.customer.FinancialPeriod
import com.indusjs.fleet.domain.entity.customer.PaymentMode
import com.indusjs.fleet.presentation.customers.detail.CustomerDetailContract.CustomerDetailTab
import com.indusjs.fleet.presentation.customers.detail.CustomerDetailContract.Effect
import com.indusjs.fleet.presentation.customers.detail.CustomerDetailContract.Intent
import com.indusjs.fleet.presentation.customers.detail.CustomerDetailContract.ReportType
import com.indusjs.fleet.presentation.customers.detail.CustomerDetailContract.State
import com.indusjs.fleet.presentation.customers.detail.CustomerDetailContract.TripStateFilter
import com.indusjs.error.result.Result
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.pdfreport.model.CustomerTripsPdfData
import com.indusjs.pdfreport.model.CustomerTripItem
import com.indusjs.pdfreport.model.CustomerPaymentsPdfData
import com.indusjs.pdfreport.model.CustomerPaymentItem
import com.indusjs.pdfreport.model.CustomerFinancialsPdfData
import com.indusjs.pdfreport.model.CustomerPaymentStatsPdf
import com.indusjs.pdfreport.model.CustomerPaymentPdfItem
import com.indusjs.pdfreport.model.CustomerTripSummaryPdfItem
import dev.zacsweers.metro.Inject

/**
 * ViewModel for Customer Detail Screen.
 * Handles overview, trips, pending payments, payments, and financial report.
 */
@Inject
class CustomerDetailViewModel(
    private val customerRepository: CustomerRepository
) : MviViewModel<State, Intent, Effect>(State()) {

    private val log = Logger.withTag("CustomerDetailViewModel")
    private var customerId: String = ""

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadCustomer -> loadCustomer(intent.customerId)
            is Intent.LoadStatistics -> loadStatistics()
            is Intent.ToggleEditMode -> toggleEditMode()
            is Intent.SaveChanges -> saveChanges()
            is Intent.CancelEdit -> cancelEdit()
            is Intent.ToggleStatus -> toggleStatus()
            is Intent.UpdateCompanyName -> updateCompanyName(intent.value)
            is Intent.UpdatePersonName -> updatePersonName(intent.value)
            is Intent.UpdatePrimaryContact -> updatePrimaryContact(intent.value)
            is Intent.UpdateSecondaryContact -> updateSecondaryContact(intent.value)
            is Intent.UpdateCompanyAddress -> updateState { copy(companyAddress = intent.value) }
            is Intent.UpdateEmail -> updateEmail(intent.value)
            is Intent.UpdateGstNumber -> updateGstNumber(intent.value)
            is Intent.UpdateNotes -> updateState { copy(notes = intent.value) }
            is Intent.ClearError -> updateState { copy(error = null) }

            // Tab navigation
            is Intent.SelectTab -> selectTab(intent.tab)

            // Trips
            is Intent.LoadTrips -> loadTrips()
            is Intent.LoadMoreTrips -> loadMoreTrips()
            is Intent.SetTripStateFilter -> setTripStateFilter(intent.filter)
            is Intent.RefreshTrips -> refreshTrips()

            // Pending Payments
            is Intent.LoadPendingPayments -> loadPendingPayments()
            is Intent.LoadMorePendingPayments -> loadMorePendingPayments()
            is Intent.RefreshPendingPayments -> refreshPendingPayments()

            // Received Payments
            is Intent.LoadPayments -> loadPayments()
            is Intent.LoadMorePayments -> loadMorePayments()
            is Intent.SetPaymentModeFilter -> setPaymentModeFilter(intent.mode)
            is Intent.RefreshPayments -> refreshPayments()

            // Financials
            is Intent.LoadFinancialReport -> loadFinancialReport()
            is Intent.SetFinancialsPeriod -> setFinancialsPeriod(intent.period)
            is Intent.SetFinancialsDateRange -> setFinancialsDateRange(intent.startDate, intent.endDate)
            is Intent.ShowDateRangePicker -> updateState { copy(showDateRangePicker = true) }
            is Intent.HideDateRangePicker -> updateState { copy(showDateRangePicker = false) }
            is Intent.RefreshFinancials -> refreshFinancials()

            // PDF Export
            is Intent.ExportPdf -> exportPdf(intent.reportType)
        }
    }

    private suspend fun loadCustomer(id: String) {
        customerId = id
        updateState { copy(isLoading = true, error = null) }

        // Initialize default date range for financials (current month)
        initializeDefaultDates()

        when (val result = customerRepository.getCustomer(id)) {
            is Result.Success -> {
                val customer = result.data
                updateState {
                    copy(
                        isLoading = false,
                        customer = customer,
                        companyName = customer.companyName,
                        personName = customer.personName,
                        primaryContact = customer.primaryContact,
                        secondaryContact = customer.secondaryContact ?: "",
                        companyAddress = customer.companyAddress ?: "",
                        email = customer.email ?: "",
                        gstNumber = customer.gstNumber ?: "",
                        notes = customer.notes ?: "",
                        canViewFinancials = true // TODO: Check user role (Owner/GM only)
                    )
                }

                // Load statistics if user can view them
                if (state.value.canViewFinancials) {
                    loadStatistics()
                }
            }
            is Result.Error -> {
                updateState {
                    copy(
                        isLoading = false,
                        error = result.message ?: result.exception.message ?: "Failed to load customer"
                    )
                }
            }
            is Result.Loading -> { }
        }
    }

    private fun initializeDefaultDates() {
        // Use FleetDateTime utility for current month range
        val today = FleetDateTime.today()  // Returns DD-MM-YYYY
        val parts = today.split("-")
        if (parts.size == 3) {
            val day = parts[0]
            val month = parts[1]
            val year = parts[2]

            // Current month range in DD-MM-YYYY format for API
            val startDate = "01-$month-$year"
            val monthNum = month.toIntOrNull() ?: 1
            val yearNum = year.toIntOrNull() ?: 2026
            val lastDay = when (monthNum) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (yearNum % 4 == 0 && (yearNum % 100 != 0 || yearNum % 400 == 0)) 29 else 28
                else -> 30
            }
            val endDate = "$lastDay-$month-$year"

            updateState {
                copy(
                    financialsStartDate = startDate,
                    financialsEndDate = endDate
                )
            }
        }
    }

    private suspend fun loadStatistics() {
        if (!state.value.canViewFinancials || customerId.isBlank()) return

        updateState { copy(isLoadingStatistics = true) }

        when (val result = customerRepository.getCustomerStatistics(customerId)) {
            is Result.Success -> {
                updateState {
                    copy(
                        isLoadingStatistics = false,
                        statistics = result.data
                    )
                }
            }
            is Result.Error -> {
                log.w { "Failed to load statistics: ${result.message}" }
                updateState { copy(isLoadingStatistics = false) }
            }
            is Result.Loading -> { }
        }
    }

    // ============= Tab Navigation =============

    private suspend fun selectTab(tab: CustomerDetailTab) {
        updateState { copy(selectedTab = tab) }

        // Lazy load data for the selected tab
        when (tab) {
            CustomerDetailTab.OVERVIEW -> { /* Already loaded */ }
            CustomerDetailTab.TRIPS -> {
                // Payment info is integrated into each trip row
                if (!state.value.tripsDataLoaded) {
                    loadTrips()
                }
            }
            CustomerDetailTab.PAYMENTS -> {
                if (!state.value.paymentsDataLoaded && state.value.canViewFinancials) {
                    loadPayments()
                }
            }
            CustomerDetailTab.FINANCIALS -> {
                if (!state.value.financialsDataLoaded && state.value.canViewFinancials) {
                    loadFinancialReport()
                }
            }
        }
    }

    // ============= Trips Tab =============

    private suspend fun loadTrips() {
        if (customerId.isBlank()) {
            log.w { "loadTrips: customerId is blank, skipping" }
            return
        }

        log.d { "loadTrips: Starting load for customer $customerId, filter: ${state.value.tripStateFilter}" }
        updateState { copy(isLoadingTrips = true, tripsError = null, tripsPage = 1) }

        when (val result = customerRepository.getCustomerTrips(
            customerId = customerId,
            page = 1,
            state = state.value.tripStateFilter.apiValue
        )) {
            is Result.Success -> {
                log.d { "loadTrips: Success - ${result.data.trips.size} trips loaded" }
                updateState {
                    copy(
                        isLoadingTrips = false,
                        trips = result.data.trips,
                        tripsSummary = result.data.summary,
                        hasMoreTrips = result.data.hasMore,
                        tripsDataLoaded = true
                    )
                }
            }
            is Result.Error -> {
                log.e { "loadTrips: Error - ${result.message}" }
                updateState {
                    copy(
                        isLoadingTrips = false,
                        tripsError = result.message ?: "Failed to load trips"
                    )
                }
            }
            is Result.Loading -> { }
        }
    }

    private suspend fun loadMoreTrips() {
        if (state.value.isLoadingTrips || !state.value.hasMoreTrips) return

        val nextPage = state.value.tripsPage + 1
        updateState { copy(isLoadingTrips = true) }

        when (val result = customerRepository.getCustomerTrips(
            customerId = customerId,
            page = nextPage,
            state = state.value.tripStateFilter.apiValue
        )) {
            is Result.Success -> {
                updateState {
                    copy(
                        isLoadingTrips = false,
                        trips = trips + result.data.trips,
                        tripsPage = nextPage,
                        hasMoreTrips = result.data.hasMore
                    )
                }
            }
            is Result.Error -> {
                updateState { copy(isLoadingTrips = false) }
            }
            is Result.Loading -> { }
        }
    }

    private suspend fun setTripStateFilter(filter: TripStateFilter) {
        updateState { copy(tripStateFilter = filter, tripsDataLoaded = false) }
        loadTrips()
    }

    private suspend fun refreshTrips() {
        updateState { copy(tripsDataLoaded = false) }
        loadTrips()
    }

    // ============= Pending Payments Tab =============

    private suspend fun loadPendingPayments() {
        if (customerId.isBlank() || !state.value.canViewFinancials) return

        updateState { copy(isLoadingPendingPayments = true, pendingPaymentsError = null, pendingPaymentsPage = 1) }

        when (val result = customerRepository.getCustomerPendingPayments(customerId)) {
            is Result.Success -> {
                updateState {
                    copy(
                        isLoadingPendingPayments = false,
                        pendingPayments = result.data.payments,
                        totalPendingAmount = result.data.totalPending,
                        overdueCount = result.data.overdueCount,
                        hasMorePendingPayments = result.data.hasMore,
                        pendingPaymentsDataLoaded = true
                    )
                }
            }
            is Result.Error -> {
                updateState {
                    copy(
                        isLoadingPendingPayments = false,
                        pendingPaymentsError = result.message ?: "Failed to load pending payments"
                    )
                }
            }
            is Result.Loading -> { }
        }
    }

    private suspend fun loadMorePendingPayments() {
        if (state.value.isLoadingPendingPayments || !state.value.hasMorePendingPayments) return

        val nextPage = state.value.pendingPaymentsPage + 1
        updateState { copy(isLoadingPendingPayments = true) }

        when (val result = customerRepository.getCustomerPendingPayments(customerId, nextPage)) {
            is Result.Success -> {
                updateState {
                    copy(
                        isLoadingPendingPayments = false,
                        pendingPayments = pendingPayments + result.data.payments,
                        pendingPaymentsPage = nextPage,
                        hasMorePendingPayments = result.data.hasMore
                    )
                }
            }
            is Result.Error -> {
                updateState { copy(isLoadingPendingPayments = false) }
            }
            is Result.Loading -> { }
        }
    }

    private suspend fun refreshPendingPayments() {
        updateState { copy(pendingPaymentsDataLoaded = false) }
        loadPendingPayments()
    }

    // ============= Received Payments Tab =============

    private suspend fun loadPayments() {
        if (customerId.isBlank() || !state.value.canViewFinancials) return

        updateState { copy(isLoadingPayments = true, paymentsError = null, paymentsPage = 1) }

        when (val result = customerRepository.getCustomerPayments(
            customerId = customerId,
            mode = state.value.paymentModeFilter?.apiValue
        )) {
            is Result.Success -> {
                updateState {
                    copy(
                        isLoadingPayments = false,
                        receivedPayments = result.data.payments,
                        totalReceivedAmount = result.data.totalReceived,
                        hasMorePayments = result.data.hasMore,
                        paymentsDataLoaded = true
                    )
                }
                // Also load payment summary
                loadPaymentSummary()
            }
            is Result.Error -> {
                updateState {
                    copy(
                        isLoadingPayments = false,
                        paymentsError = result.message ?: "Failed to load payments"
                    )
                }
            }
            is Result.Loading -> { }
        }
    }

    private suspend fun loadPaymentSummary() {
        when (val result = customerRepository.getCustomerPaymentSummary(customerId)) {
            is Result.Success -> {
                updateState { copy(paymentSummary = result.data) }
            }
            is Result.Error -> {
                log.w { "Failed to load payment summary: ${result.message}" }
            }
            is Result.Loading -> { }
        }
    }

    private suspend fun loadMorePayments() {
        if (state.value.isLoadingPayments || !state.value.hasMorePayments) return

        val nextPage = state.value.paymentsPage + 1
        updateState { copy(isLoadingPayments = true) }

        when (val result = customerRepository.getCustomerPayments(
            customerId = customerId,
            page = nextPage,
            mode = state.value.paymentModeFilter?.apiValue
        )) {
            is Result.Success -> {
                updateState {
                    copy(
                        isLoadingPayments = false,
                        receivedPayments = receivedPayments + result.data.payments,
                        paymentsPage = nextPage,
                        hasMorePayments = result.data.hasMore
                    )
                }
            }
            is Result.Error -> {
                updateState { copy(isLoadingPayments = false) }
            }
            is Result.Loading -> { }
        }
    }

    private suspend fun setPaymentModeFilter(mode: PaymentMode?) {
        updateState { copy(paymentModeFilter = mode, paymentsDataLoaded = false) }
        loadPayments()
    }

    private suspend fun refreshPayments() {
        updateState { copy(paymentsDataLoaded = false) }
        loadPayments()
    }

    // ============= Financials Tab =============

    private suspend fun loadFinancialReport() {
        if (customerId.isBlank() || !state.value.canViewFinancials) return

        updateState { copy(isLoadingFinancials = true, financialsError = null) }

        when (val result = customerRepository.getCustomerFinancialReport(
            customerId = customerId,
            period = state.value.financialsPeriod.apiValue,
            startDate = state.value.financialsStartDate.takeIf { it.isNotBlank() },
            endDate = state.value.financialsEndDate.takeIf { it.isNotBlank() }
        )) {
            is Result.Success -> {
                updateState {
                    copy(
                        isLoadingFinancials = false,
                        financialReport = result.data,
                        financialsDataLoaded = true
                    )
                }
            }
            is Result.Error -> {
                updateState {
                    copy(
                        isLoadingFinancials = false,
                        financialsError = result.message ?: "Failed to load financial report"
                    )
                }
            }
            is Result.Loading -> { }
        }
    }

    private suspend fun setFinancialsPeriod(period: FinancialPeriod) {
        updateState { copy(financialsPeriod = period, financialsDataLoaded = false) }
        loadFinancialReport()
    }

    private suspend fun setFinancialsDateRange(startDate: String, endDate: String) {
        updateState {
            copy(
                financialsStartDate = startDate,
                financialsEndDate = endDate,
                financialsPeriod = FinancialPeriod.CUSTOM,
                showDateRangePicker = false,
                financialsDataLoaded = false
            )
        }
        loadFinancialReport()
    }

    private suspend fun refreshFinancials() {
        updateState { copy(financialsDataLoaded = false) }
        loadFinancialReport()
    }

    // ============= PDF Export =============

    private suspend fun exportPdf(reportType: ReportType) {
        updateState { copy(isExportingPdf = true, exportType = reportType) }

        try {
            when (reportType) {
                ReportType.TRIPS -> exportTripsPdf()
                ReportType.PENDING_PAYMENTS -> exportTripsPdf() // Use trips PDF for pending as well
                ReportType.PAYMENTS -> exportPaymentsPdf()
                ReportType.FINANCIALS -> exportFinancialsPdf()
            }
        } catch (e: Exception) {
            log.e { "PDF export failed: ${e.message}" }
            sendEffect(Effect.ShowSnackbar("Failed to export PDF: ${e.message}"))
        } finally {
            updateState { copy(isExportingPdf = false, exportType = null) }
        }
    }

    private fun exportTripsPdf() {
        val customer = state.value.customer ?: return
        val trips = state.value.trips
        val summary = state.value.tripsSummary

        if (trips.isEmpty()) {
            sendEffect(Effect.ShowSnackbar("No trips to export"))
            return
        }

        // Calculate values from trips if summary is null
        val totalTrips = summary?.totalTrips ?: trips.size
        val totalRevenue = summary?.totalRevenue ?: trips.sumOf { it.tripPrice ?: 0.0 }
        val totalPaid = trips.sumOf { it.paidAmount ?: 0.0 }
        val totalPending = trips.sumOf {
            val price = it.tripPrice ?: 0.0
            val paid = it.paidAmount ?: 0.0
            (price - paid).coerceAtLeast(0.0)
        }

        val pdfData = CustomerTripsPdfData(
            customerId = customer.id.toIntOrNull() ?: 0,
            customerName = customer.personName,
            companyName = customer.companyName,
            contactNumber = customer.primaryContact,
            totalTrips = totalTrips,
            totalRevenue = totalRevenue,
            totalPaid = totalPaid,
            totalPending = totalPending,
            dateRange = null,
            trips = trips.map { trip ->
                val dueAmount = trip.pendingAmount ?: run {
                    val price = trip.tripPrice ?: 0.0
                    val paid = trip.paidAmount ?: 0.0
                    (price - paid).coerceAtLeast(0.0)
                }
                CustomerTripItem(
                    tripId = trip.id.toIntOrNull() ?: 0,
                    vehicleNumber = trip.vehicleRegistration ?: "-",
                    startLocation = trip.startLocation ?: "-",
                    endLocation = trip.endLocation ?: "-",
                    startDate = FleetDateTime.formatIsoToDisplayDate(trip.plannedStart ?: trip.scheduledDate),
                    endDate = FleetDateTime.formatIsoToDisplayDate(trip.plannedEnd),
                    tripStatus = trip.stateDisplay,
                    tripPrice = trip.tripPrice ?: 0.0,
                    paidAmount = trip.paidAmount ?: 0.0,
                    pendingAmount = dueAmount
                )
            },
            generatedAt = FleetDateTime.formatDisplayDateTime12Hour(FleetDateTime.now())
        )

        sendEffect(Effect.ExportTripsPdf(pdfData))
    }

    private fun formatCurrencyForPdf(amount: Double): String {
        return when {
            amount >= 10000000 -> {
                val value = amount / 10000000
                "₹${((value * 10).toInt() / 10.0)}Cr"
            }
            amount >= 100000 -> {
                val value = amount / 100000
                "₹${((value * 10).toInt() / 10.0)}L"
            }
            amount >= 1000 -> {
                val value = amount / 1000
                "₹${((value * 10).toInt() / 10.0)}K"
            }
            else -> "₹${amount.toInt()}"
        }
    }

    private suspend fun exportPaymentsPdf() {
        val customer = state.value.customer ?: return
        val payments = state.value.receivedPayments

        if (payments.isEmpty()) {
            sendEffect(Effect.ShowSnackbar("No payments to export"))
            return
        }

        val pdfData = CustomerPaymentsPdfData(
            customerId = customer.id.toIntOrNull() ?: 0,
            customerName = customer.personName,
            companyName = customer.companyName,
            contactNumber = customer.primaryContact,
            totalPayments = payments.size,
            totalAmount = state.value.totalReceivedAmount,
            dateRange = null,
            payments = payments.map { payment ->
                CustomerPaymentItem(
                    paymentId = payment.id.toIntOrNull() ?: 0,
                    tripId = payment.tripId?.toIntOrNull() ?: 0,
                    vehicleNumber = "-", // Vehicle number not available in CustomerPayment
                    amount = payment.amount,
                    paymentType = payment.paymentType ?: "payment",
                    paymentMode = payment.modeDisplay,
                    paymentDate = payment.date?.let { FleetDateTime.formatIsoToDisplayDateTime12Hour(it) } ?: "-",
                    receiptNumber = payment.receiptNumber,
                    startLocation = null,
                    endLocation = null
                )
            },
            generatedAt = FleetDateTime.formatDisplayDateTime12Hour(FleetDateTime.now())
        )

        sendEffect(Effect.ExportPaymentsPdf(pdfData))
    }

    private fun exportFinancialsPdf() {
        val customer = state.value.customer ?: return
        val report = state.value.financialReport

        if (report == null) {
            sendEffect(Effect.ShowSnackbar("No financial data to export"))
            return
        }

        // Calculate payment stats
        val payments = state.value.receivedPayments
        val paymentStats = CustomerPaymentStatsPdf(
            totalPayments = payments.size,
            advancePayments = payments.count { it.paymentType?.lowercase() == "advance" },
            partialPayments = payments.count { it.paymentType?.lowercase() == "partial" },
            finalPayments = payments.count { it.paymentType?.lowercase() == "final" },
            avgPaymentAmount = if (payments.isNotEmpty()) payments.sumOf { it.amount } / payments.size else 0.0
        )

        // Get recent payments (last 10)
        val recentPayments = payments.take(10).map { payment ->
            CustomerPaymentPdfItem(
                paymentId = payment.id.toIntOrNull() ?: 0,
                tripId = payment.tripId?.toIntOrNull() ?: 0,
                paymentDate = payment.date?.let { FleetDateTime.formatIsoToDisplayDate(it) } ?: "-",
                amount = payment.amount,
                paymentType = payment.paymentType ?: "payment",
                paymentMode = payment.modeDisplay,
                receiptNumber = payment.receiptNumber ?: "-"
            )
        }

        // Get trip summary
        val trips = state.value.trips
        val tripSummary = trips.take(10).map { trip ->
            val pendingAmount = trip.pendingAmount ?: run {
                val price = trip.tripPrice ?: 0.0
                val paid = trip.paidAmount ?: 0.0
                (price - paid).coerceAtLeast(0.0)
            }
            CustomerTripSummaryPdfItem(
                tripId = trip.id.toIntOrNull() ?: 0,
                tripDate = FleetDateTime.formatIsoToDisplayDate(trip.plannedStart ?: trip.scheduledDate),
                route = trip.routeDisplay,
                tripPrice = trip.tripPrice ?: 0.0,
                paidAmount = trip.paidAmount ?: 0.0,
                pendingAmount = pendingAmount,
                paymentStatus = when {
                    pendingAmount <= 0 -> "Paid"
                    (trip.paidAmount ?: 0.0) > 0 -> "Partial"
                    else -> "Pending"
                }
            )
        }

        val dateRange = "${state.value.financialsStartDate} to ${state.value.financialsEndDate}"

        val pdfData = CustomerFinancialsPdfData(
            customerId = customer.id.toIntOrNull() ?: 0,
            customerName = customer.personName,
            companyName = customer.companyName,
            contactNumber = customer.primaryContact,
            dateRange = dateRange,
            totalTrips = report.tripSummary?.totalTrips ?: trips.size,
            completedTrips = report.tripSummary?.completedTrips ?: trips.count { it.state?.lowercase() == "completed" },
            activeTrips = trips.count { it.state?.lowercase() == "on_route" },
            totalRevenue = report.totalRevenue ?: 0.0,
            totalReceived = report.paymentReceived ?: 0.0,
            totalPending = report.paymentPending ?: 0.0,
            paymentStats = paymentStats,
            recentPayments = recentPayments,
            tripSummary = tripSummary,
            generatedAt = FleetDateTime.formatDisplayDateTime12Hour(FleetDateTime.now())
        )

        sendEffect(Effect.ExportFinancialsPdf(pdfData))
    }

    // ============= Edit Mode Functions =============

    private fun toggleEditMode() {
        updateState { copy(isEditMode = !isEditMode) }
    }

    private suspend fun saveChanges() {
        if (!validateFields()) return

        updateState { copy(isSaving = true) }

        when (val result = customerRepository.updateCustomer(
            customerId = customerId,
            companyName = state.value.companyName,
            personName = state.value.personName,
            primaryContact = state.value.primaryContact,
            secondaryContact = state.value.secondaryContact.takeIf { it.isNotBlank() },
            companyAddress = state.value.companyAddress.takeIf { it.isNotBlank() },
            email = state.value.email.takeIf { it.isNotBlank() },
            gstNumber = state.value.gstNumber.takeIf { it.isNotBlank() },
            notes = state.value.notes.takeIf { it.isNotBlank() }
        )) {
            is Result.Success -> {
                updateState {
                    copy(
                        isSaving = false,
                        isEditMode = false,
                        customer = result.data
                    )
                }
                sendEffect(Effect.ShowSnackbar("Customer updated successfully"))
            }
            is Result.Error -> {
                updateState {
                    copy(
                        isSaving = false,
                        error = result.message ?: result.exception.message ?: "Failed to update customer"
                    )
                }
            }
            is Result.Loading -> { }
        }
    }

    private fun cancelEdit() {
        val customer = state.value.customer ?: return
        updateState {
            copy(
                isEditMode = false,
                companyName = customer.companyName,
                personName = customer.personName,
                primaryContact = customer.primaryContact,
                secondaryContact = customer.secondaryContact ?: "",
                companyAddress = customer.companyAddress ?: "",
                email = customer.email ?: "",
                gstNumber = customer.gstNumber ?: "",
                notes = customer.notes ?: "",
                companyNameError = null,
                personNameError = null,
                primaryContactError = null,
                secondaryContactError = null,
                emailError = null,
                gstNumberError = null
            )
        }
    }

    private suspend fun toggleStatus() {
        updateState { copy(isSaving = true) }

        when (val result = customerRepository.toggleCustomerStatus(customerId)) {
            is Result.Success -> {
                val status = if (result.data.isActive) "activated" else "deactivated"
                updateState {
                    copy(
                        isSaving = false,
                        customer = result.data
                    )
                }
                sendEffect(Effect.ShowSnackbar("Customer $status successfully"))
            }
            is Result.Error -> {
                updateState {
                    copy(
                        isSaving = false,
                        error = result.message ?: result.exception.message ?: "Failed to toggle status"
                    )
                }
            }
            is Result.Loading -> { }
        }
    }

    // ============= Validation Functions =============

    private fun updateCompanyName(value: String) {
        val error = if (value.isBlank()) "Company name is required" else null
        updateState { copy(companyName = value, companyNameError = error) }
    }

    private fun updatePersonName(value: String) {
        val error = if (value.isBlank()) "Contact person is required" else null
        updateState { copy(personName = value, personNameError = error) }
    }

    private fun updatePrimaryContact(value: String) {
        val digits = value.filter { it.isDigit() }.take(10)
        val error = when {
            digits.isBlank() -> "Mobile number is required"
            digits.length != 10 -> "Enter 10-digit mobile number"
            !digits.first().toString().matches(Regex("[6-9]")) -> "Mobile must start with 6-9"
            else -> null
        }
        updateState { copy(primaryContact = digits, primaryContactError = error) }
    }

    private fun updateSecondaryContact(value: String) {
        val digits = value.filter { it.isDigit() }.take(10)
        val error = if (digits.isNotBlank()) {
            when {
                digits.length != 10 -> "Enter valid 10-digit mobile"
                !digits.first().toString().matches(Regex("[6-9]")) -> "Mobile must start with 6-9"
                else -> null
            }
        } else null
        updateState { copy(secondaryContact = digits, secondaryContactError = error) }
    }

    private fun updateEmail(value: String) {
        val error = if (value.isNotBlank() && !ValidationUtils.isValidEmail(value)) {
            "Invalid email format"
        } else null
        updateState { copy(email = value, emailError = error) }
    }

    private fun updateGstNumber(value: String) {
        val gst = value.uppercase().filter { it.isLetterOrDigit() }.take(15)
        val error = if (gst.isNotBlank()) {
            when {
                gst.length != 15 -> "GST must be 15 characters"
                !gst.matches(Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")) -> "Invalid GST format"
                else -> null
            }
        } else null
        updateState { copy(gstNumber = gst, gstNumberError = error) }
    }

    private fun validateFields(): Boolean {
        val companyNameError = if (state.value.companyName.isBlank()) "Company name is required" else null
        val personNameError = if (state.value.personName.isBlank()) "Contact person is required" else null

        val primaryContactError = state.value.primaryContact.let { contact ->
            when {
                contact.isBlank() -> "Mobile number is required"
                contact.length != 10 -> "Enter 10-digit mobile number"
                !contact.first().toString().matches(Regex("[6-9]")) -> "Mobile must start with 6-9"
                else -> null
            }
        }

        val secondaryContactError = state.value.secondaryContact.let { contact ->
            if (contact.isNotBlank()) {
                when {
                    contact.length != 10 -> "Enter valid 10-digit mobile"
                    !contact.first().toString().matches(Regex("[6-9]")) -> "Mobile must start with 6-9"
                    else -> null
                }
            } else null
        }

        val emailError = if (state.value.email.isNotBlank() && !ValidationUtils.isValidEmail(state.value.email)) {
            "Invalid email format"
        } else null

        val gstNumberError = state.value.gstNumber.let { gst ->
            if (gst.isNotBlank()) {
                when {
                    gst.length != 15 -> "GST must be 15 characters"
                    !gst.matches(Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")) -> "Invalid GST format"
                    else -> null
                }
            } else null
        }

        updateState {
            copy(
                companyNameError = companyNameError,
                personNameError = personNameError,
                primaryContactError = primaryContactError,
                secondaryContactError = secondaryContactError,
                emailError = emailError,
                gstNumberError = gstNumberError
            )
        }

        return companyNameError == null && personNameError == null &&
               primaryContactError == null && secondaryContactError == null &&
               emailError == null && gstNumberError == null
    }
}
