package com.indusjs.fleet.presentation.customer.detail

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.customer.*
import com.indusjs.pdfreport.model.CustomerTripsPdfData
import com.indusjs.pdfreport.model.CustomerPaymentsPdfData
import com.indusjs.pdfreport.model.CustomerFinancialsPdfData

/**
 * MVI Contract for Customer Detail Screen.
 * Navigation effects use specific callbacks instead of FleetRoute.
 */
object CustomerDetailContract {

    enum class CustomerDetailTab(val title: String, val icon: String) {
        OVERVIEW("Overview", "📋"),
        TRIPS("Trips", "🚛"),
        PAYMENTS("Payments", "💳"),
        FINANCIALS("Financials", "📊")
    }

    enum class TripStateFilter(val apiValue: String?, val displayName: String) {
        ALL(null, "All"),
        PLANNED("planned", "Planned"),
        ON_ROUTE("on_route", "On Route"),
        COMPLETED("completed", "Completed"),
        CANCELLED("cancelled", "Cancelled")
    }

    data class State(
        val isLoading: Boolean = false,
        val customer: Customer? = null,
        val statistics: CustomerStatistics? = null,
        val isLoadingStatistics: Boolean = false,
        val canViewFinancials: Boolean = false,
        val isEditMode: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,

        // Tab management
        val selectedTab: CustomerDetailTab = CustomerDetailTab.OVERVIEW,

        // Edit fields
        val companyName: String = "",
        val personName: String = "",
        val primaryContact: String = "",
        val secondaryContact: String = "",
        val companyAddress: String = "",
        val email: String = "",
        val gstNumber: String = "",
        val notes: String = "",

        // Validation errors
        val companyNameError: String? = null,
        val personNameError: String? = null,
        val primaryContactError: String? = null,
        val secondaryContactError: String? = null,
        val emailError: String? = null,
        val gstNumberError: String? = null,

        // Trips Tab State
        val trips: List<CustomerTrip> = emptyList(),
        val tripsSummary: CustomerTripsSummary? = null,
        val isLoadingTrips: Boolean = false,
        val tripsError: String? = null,
        val tripsPage: Int = 1,
        val hasMoreTrips: Boolean = false,
        val tripStateFilter: TripStateFilter = TripStateFilter.ALL,
        val tripsDataLoaded: Boolean = false,

        // Pending Payments State
        val pendingPayments: List<CustomerPendingPayment> = emptyList(),
        val isLoadingPendingPayments: Boolean = false,
        val pendingPaymentsError: String? = null,
        val totalPendingAmount: Double = 0.0,
        val overdueCount: Int = 0,
        val pendingPaymentsPage: Int = 1,
        val hasMorePendingPayments: Boolean = false,
        val pendingPaymentsDataLoaded: Boolean = false,

        // Received Payments State
        val receivedPayments: List<CustomerPayment> = emptyList(),
        val isLoadingPayments: Boolean = false,
        val paymentsError: String? = null,
        val totalReceivedAmount: Double = 0.0,
        val paymentModeFilter: PaymentMode? = null,
        val paymentsPage: Int = 1,
        val hasMorePayments: Boolean = false,
        val paymentSummary: CustomerPaymentSummary? = null,
        val paymentsDataLoaded: Boolean = false,

        // Financials State
        val financialReport: CustomerFinancialReport? = null,
        val isLoadingFinancials: Boolean = false,
        val financialsError: String? = null,
        val financialsPeriod: FinancialPeriod = FinancialPeriod.MONTHLY,
        val financialsStartDate: String = "",
        val financialsEndDate: String = "",
        val showDateRangePicker: Boolean = false,
        val financialsDataLoaded: Boolean = false,

        // PDF Export State
        val isExportingPdf: Boolean = false,
        val exportType: ReportType? = null
    ) : UiState {

        val isExporting: Boolean
            get() = isExportingPdf

        val hasChanges: Boolean
            get() = customer?.let { c ->
                companyName != c.companyName ||
                personName != c.personName ||
                primaryContact != c.primaryContact ||
                secondaryContact != (c.secondaryContact ?: "") ||
                companyAddress != (c.companyAddress ?: "") ||
                email != (c.email ?: "") ||
                gstNumber != (c.gstNumber ?: "") ||
                notes != (c.notes ?: "")
            } ?: false

        val isValid: Boolean
            get() = companyNameError == null &&
                    personNameError == null &&
                    primaryContactError == null &&
                    secondaryContactError == null &&
                    emailError == null &&
                    gstNumberError == null &&
                    companyName.isNotBlank() &&
                    personName.isNotBlank() &&
                    primaryContact.length == 10

        val canSave: Boolean
            get() = isValid && hasChanges && !isSaving

        val visibleTabs: List<CustomerDetailTab>
            get() = if (canViewFinancials) {
                CustomerDetailTab.entries
            } else {
                listOf(CustomerDetailTab.OVERVIEW, CustomerDetailTab.TRIPS)
            }
    }

    enum class ReportType {
        TRIPS,
        PENDING_PAYMENTS,
        PAYMENTS,
        FINANCIALS
    }

    sealed interface Intent : UiIntent {
        data class LoadCustomer(val customerId: String) : Intent
        data object LoadStatistics : Intent
        data object ToggleEditMode : Intent
        data object SaveChanges : Intent
        data object CancelEdit : Intent
        data object ToggleStatus : Intent

        // Edit field updates
        data class UpdateCompanyName(val value: String) : Intent
        data class UpdatePersonName(val value: String) : Intent
        data class UpdatePrimaryContact(val value: String) : Intent
        data class UpdateSecondaryContact(val value: String) : Intent
        data class UpdateCompanyAddress(val value: String) : Intent
        data class UpdateEmail(val value: String) : Intent
        data class UpdateGstNumber(val value: String) : Intent
        data class UpdateNotes(val value: String) : Intent

        // Tab navigation
        data class SelectTab(val tab: CustomerDetailTab) : Intent

        // Trips tab
        data object LoadTrips : Intent
        data object LoadMoreTrips : Intent
        data class SetTripStateFilter(val filter: TripStateFilter) : Intent
        data object RefreshTrips : Intent

        // Pending Payments
        data object LoadPendingPayments : Intent
        data object LoadMorePendingPayments : Intent
        data object RefreshPendingPayments : Intent

        // Received Payments
        data object LoadPayments : Intent
        data object LoadMorePayments : Intent
        data class SetPaymentModeFilter(val mode: PaymentMode?) : Intent
        data object RefreshPayments : Intent

        // Financials
        data object LoadFinancialReport : Intent
        data class SetFinancialsPeriod(val period: FinancialPeriod) : Intent
        data class SetFinancialsDateRange(val startDate: String, val endDate: String) : Intent
        data object ShowDateRangePicker : Intent
        data object HideDateRangePicker : Intent
        data object RefreshFinancials : Intent

        // PDF Export
        data class ExportPdf(val reportType: ReportType) : Intent

        data object ClearError : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class ShowSnackbar(val message: String) : Effect
        data class PdfExported(val filePath: String) : Effect
        data class PdfExportError(val message: String) : Effect
        data class ExportHtml(val content: String, val fileName: String) : Effect
        data class ExportTripsPdf(val pdfData: CustomerTripsPdfData) : Effect
        data class ExportPaymentsPdf(val pdfData: CustomerPaymentsPdfData) : Effect
        data class ExportFinancialsPdf(val pdfData: CustomerFinancialsPdfData) : Effect
    }
}

