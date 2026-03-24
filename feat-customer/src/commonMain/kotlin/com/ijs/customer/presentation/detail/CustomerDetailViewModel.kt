package com.ijs.customer.presentation.detail

import co.touchlab.kermit.Logger
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.util.ValidationUtils
import com.ijs.customer.domain.repository.CustomerRepository
import com.ijs.customer.domain.entity.FinancialPeriod
import com.ijs.customer.presentation.detail.CustomerDetailContract.CustomerDetailTab
import com.ijs.customer.presentation.detail.CustomerDetailContract.Effect
import com.ijs.customer.presentation.detail.CustomerDetailContract.Intent
import com.ijs.customer.presentation.detail.CustomerDetailContract.State
import com.indusjs.error.result.Result
import com.indusjs.datetimeutils.FleetDateTime
import dev.zacsweers.metro.Inject

/**
 * ViewModel for Customer Detail Screen.
 * Delegates tab-specific logic to handler classes to stay under 500 lines.
 */
@Inject
class CustomerDetailViewModel(
    private val customerRepository: CustomerRepository
) : MviViewModel<State, Intent, Effect>(State()) {

    private val log = Logger.withTag("CustomerDetailViewModel")
    private var customerId: String = ""

    private val tripsHandler = CustomerDetailTripsHandler(
        customerRepository = customerRepository,
        getState = { state.value },
        setState = { transform -> updateState(transform) },
        log = log
    )

    private val paymentsHandler = CustomerDetailPaymentsHandler(
        customerRepository = customerRepository,
        getState = { state.value },
        setState = { transform -> updateState(transform) },
        log = log
    )

    private val financialsHandler = CustomerDetailFinancialsHandler(
        customerRepository = customerRepository,
        getState = { state.value },
        setState = { transform -> updateState(transform) },
        log = log
    )

    private val pdfExporter = CustomerDetailPdfExporter(
        getState = { state.value },
        setState = { transform -> updateState(transform) },
        sendEffect = { effect -> sendEffect(effect) },
        log = log
    )

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

            // Trips (delegated)
            is Intent.LoadTrips -> tripsHandler.loadTrips(customerId)
            is Intent.LoadMoreTrips -> tripsHandler.loadMoreTrips(customerId)
            is Intent.SetTripStateFilter -> tripsHandler.setTripStateFilter(customerId, intent.filter)
            is Intent.RefreshTrips -> tripsHandler.refreshTrips(customerId)

            // Pending Payments (delegated)
            is Intent.LoadPendingPayments -> paymentsHandler.loadPendingPayments(customerId)
            is Intent.LoadMorePendingPayments -> paymentsHandler.loadMorePendingPayments(customerId)
            is Intent.RefreshPendingPayments -> paymentsHandler.refreshPendingPayments(customerId)

            // Received Payments (delegated)
            is Intent.LoadPayments -> paymentsHandler.loadPayments(customerId)
            is Intent.LoadMorePayments -> paymentsHandler.loadMorePayments(customerId)
            is Intent.SetPaymentModeFilter -> paymentsHandler.setPaymentModeFilter(customerId, intent.mode)
            is Intent.RefreshPayments -> paymentsHandler.refreshPayments(customerId)

            // Financials (delegated)
            is Intent.LoadFinancialReport -> financialsHandler.loadFinancialReport(customerId)
            is Intent.SetFinancialsPeriod -> financialsHandler.setFinancialsPeriod(customerId, intent.period)
            is Intent.SetFinancialsDateRange -> financialsHandler.setFinancialsDateRange(customerId, intent.startDate, intent.endDate)
            is Intent.ShowDateRangePicker -> updateState { copy(showDateRangePicker = true) }
            is Intent.HideDateRangePicker -> updateState { copy(showDateRangePicker = false) }
            is Intent.RefreshFinancials -> financialsHandler.refreshFinancials(customerId)

            // PDF Export (delegated)
            is Intent.ExportPdf -> pdfExporter.exportPdf(intent.reportType)
        }
    }

    // ============= Core Data Loading =============

    private suspend fun loadCustomer(id: String) {
        customerId = id
        updateState { copy(isLoading = true, error = null) }
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
                        canViewFinancials = true // TODO: Check user role
                    )
                }
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
        val today = FleetDateTime.today()
        val parts = today.split("-")
        if (parts.size == 3) {
            val month = parts[1]
            val year = parts[2]
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
            updateState { copy(financialsStartDate = startDate, financialsEndDate = endDate) }
        }
    }

    private suspend fun loadStatistics() {
        if (!state.value.canViewFinancials || customerId.isBlank()) return
        updateState { copy(isLoadingStatistics = true) }

        when (val result = customerRepository.getCustomerStatistics(customerId)) {
            is Result.Success -> updateState { copy(isLoadingStatistics = false, statistics = result.data) }
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
        when (tab) {
            CustomerDetailTab.OVERVIEW -> { }
            CustomerDetailTab.TRIPS -> {
                if (!state.value.tripsDataLoaded) tripsHandler.loadTrips(customerId)
            }
            CustomerDetailTab.PAYMENTS -> {
                if (!state.value.paymentsDataLoaded && state.value.canViewFinancials) {
                    paymentsHandler.loadPayments(customerId)
                }
            }
            CustomerDetailTab.FINANCIALS -> {
                if (!state.value.financialsDataLoaded && state.value.canViewFinancials) {
                    financialsHandler.loadFinancialReport(customerId)
                }
            }
        }
    }

    // ============= Edit Mode =============

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
                updateState { copy(isSaving = false, isEditMode = false, customer = result.data) }
                sendEffect(Effect.ShowSnackbar("Customer updated successfully"))
            }
            is Result.Error -> {
                updateState {
                    copy(isSaving = false, error = result.message ?: "Failed to update customer")
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
                companyNameError = null, personNameError = null,
                primaryContactError = null, secondaryContactError = null,
                emailError = null, gstNumberError = null
            )
        }
    }

    private suspend fun toggleStatus() {
        updateState { copy(isSaving = true) }
        when (val result = customerRepository.toggleCustomerStatus(customerId)) {
            is Result.Success -> {
                val status = if (result.data.isActive) "activated" else "deactivated"
                updateState { copy(isSaving = false, customer = result.data) }
                sendEffect(Effect.ShowSnackbar("Customer $status successfully"))
            }
            is Result.Error -> {
                updateState { copy(isSaving = false, error = result.message ?: "Failed to toggle status") }
            }
            is Result.Loading -> { }
        }
    }

    // ============= Validation =============

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
        val s = state.value
        val companyNameError = if (s.companyName.isBlank()) "Company name is required" else null
        val personNameError = if (s.personName.isBlank()) "Contact person is required" else null
        val primaryContactError = s.primaryContact.let { c ->
            when {
                c.isBlank() -> "Mobile number is required"
                c.length != 10 -> "Enter 10-digit mobile number"
                !c.first().toString().matches(Regex("[6-9]")) -> "Mobile must start with 6-9"
                else -> null
            }
        }
        val secondaryContactError = s.secondaryContact.let { c ->
            if (c.isNotBlank()) {
                when {
                    c.length != 10 -> "Enter valid 10-digit mobile"
                    !c.first().toString().matches(Regex("[6-9]")) -> "Mobile must start with 6-9"
                    else -> null
                }
            } else null
        }
        val emailError = if (s.email.isNotBlank() && !ValidationUtils.isValidEmail(s.email)) "Invalid email format" else null
        val gstNumberError = s.gstNumber.let { g ->
            if (g.isNotBlank()) {
                when {
                    g.length != 15 -> "GST must be 15 characters"
                    !g.matches(Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")) -> "Invalid GST format"
                    else -> null
                }
            } else null
        }

        updateState {
            copy(
                companyNameError = companyNameError, personNameError = personNameError,
                primaryContactError = primaryContactError, secondaryContactError = secondaryContactError,
                emailError = emailError, gstNumberError = gstNumberError
            )
        }

        return companyNameError == null && personNameError == null &&
               primaryContactError == null && secondaryContactError == null &&
               emailError == null && gstNumberError == null
    }
}

