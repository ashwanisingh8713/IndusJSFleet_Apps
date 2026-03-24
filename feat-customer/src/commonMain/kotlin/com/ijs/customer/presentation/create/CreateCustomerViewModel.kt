package com.ijs.customer.presentation.create

import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.util.ValidationUtils
import com.ijs.customer.domain.usecase.CreateCustomerUseCase
import com.ijs.customer.presentation.create.CreateCustomerContract.Effect
import com.ijs.customer.presentation.create.CreateCustomerContract.Intent
import com.ijs.customer.presentation.create.CreateCustomerContract.State
import com.indusjs.error.result.Result
import dev.zacsweers.metro.Inject

/**
 * ViewModel for Create Customer Screen.
 */
@Inject
class CreateCustomerViewModel(
    private val createCustomerUseCase: CreateCustomerUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.UpdateCompanyName -> updateCompanyName(intent.value)
            is Intent.UpdatePersonName -> updatePersonName(intent.value)
            is Intent.UpdatePrimaryContact -> updatePrimaryContact(intent.value)
            is Intent.UpdateSecondaryContact -> updateSecondaryContact(intent.value)
            is Intent.UpdateCompanyAddress -> updateState { copy(companyAddress = intent.value) }
            is Intent.UpdateEmail -> updateEmail(intent.value)
            is Intent.UpdateGstNumber -> updateGstNumber(intent.value)
            is Intent.UpdateNotes -> updateState { copy(notes = intent.value) }
            is Intent.CreateCustomer -> createCustomer()
            is Intent.ClearError -> updateState { copy(error = null) }
        }
    }

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

    private suspend fun createCustomer() {
        if (!validateFields()) return

        updateState { copy(isSaving = true, error = null) }

        when (val result = createCustomerUseCase(
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
                updateState { copy(isSaving = false) }
                sendEffect(Effect.ShowSnackbar("Customer created successfully"))
                sendEffect(Effect.NavigateToCustomerDetail(result.data.id))
            }
            is Result.Error -> {
                updateState {
                    copy(
                        isSaving = false,
                        error = result.message ?: result.exception.message ?: "Failed to create customer"
                    )
                }
            }
            is Result.Loading -> { }
        }
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

