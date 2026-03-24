package com.ijs.customer.presentation.create

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState

/**
 * MVI Contract for Create Customer Screen.
 * Navigation effects use specific callbacks instead of FleetRoute.
 */
object CreateCustomerContract {

    data class State(
        val isSaving: Boolean = false,
        val error: String? = null,

        // Form fields
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
        val gstNumberError: String? = null
    ) : UiState {

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

        val hasData: Boolean
            get() = companyName.isNotBlank() ||
                    personName.isNotBlank() ||
                    primaryContact.isNotBlank()

        val formCompletionPercentage: Int
            get() {
                var completed = 0
                val total = 3

                if (companyName.isNotBlank()) completed++
                if (personName.isNotBlank()) completed++
                if (primaryContact.length == 10) completed++

                return (completed * 100) / total
            }
    }

    sealed interface Intent : UiIntent {
        data class UpdateCompanyName(val value: String) : Intent
        data class UpdatePersonName(val value: String) : Intent
        data class UpdatePrimaryContact(val value: String) : Intent
        data class UpdateSecondaryContact(val value: String) : Intent
        data class UpdateCompanyAddress(val value: String) : Intent
        data class UpdateEmail(val value: String) : Intent
        data class UpdateGstNumber(val value: String) : Intent
        data class UpdateNotes(val value: String) : Intent
        data object CreateCustomer : Intent
        data object ClearError : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class NavigateToCustomerDetail(val customerId: String) : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}

