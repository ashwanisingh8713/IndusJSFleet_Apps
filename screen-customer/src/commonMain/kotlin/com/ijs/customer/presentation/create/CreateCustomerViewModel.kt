package com.ijs.customer.presentation.create

import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.util.ValidationUtils
import com.ijs.customer.domain.usecase.CreateCustomerUseCase
import com.ijs.customer.presentation.create.CreateCustomerContract.Effect
import com.ijs.customer.presentation.create.CreateCustomerContract.Intent
import com.ijs.customer.presentation.create.CreateCustomerContract.State
import com.indusjs.error.result.Result
import com.indusjs.uicomponents.components.UiText
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_company_name_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_contact_person_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_create_customer
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_email_invalid
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_gst_invalid
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_mobile_10_digits
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_mobile_invalid
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_mobile_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_mobile_start_digit
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_validation
import indusjsfleet.ijs_ui_components_lib.generated.resources.success_customer_created

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
        val error = if (value.isBlank()) UiText.StringRes(Res.string.error_company_name_required) else null
        updateState { copy(companyName = value, companyNameError = error) }
    }

    private fun updatePersonName(value: String) {
        updateState { copy(personName = value, personNameError = personNameError(value)) }
    }

    /**
     * Person/contact name validation. Uses [ValidationUtils.getNameError] (letters, spaces and
     * . ' - joiners; 2–50 chars) — NOT used for the company name, which may legitimately contain
     * digits/&. Blank maps to the dedicated "contact person required" message; any other rule
     * failure maps to the shared validation message until a dedicated string exists.
     */
    private fun personNameError(value: String): UiText? {
        if (value.isBlank()) return UiText.StringRes(Res.string.error_contact_person_required)
        return if (ValidationUtils.getNameError(name = value, fieldName = "Contact person") == null) {
            null
        } else {
            UiText.StringRes(Res.string.error_validation)
        }
    }

    private fun updatePrimaryContact(value: String) {
        val digits = value.filter { it.isDigit() }.take(10)
        val error = when {
            digits.isBlank() -> UiText.StringRes(Res.string.error_mobile_required)
            !ValidationUtils.isValidIndianMobile(digits) -> UiText.StringRes(Res.string.error_mobile_invalid)
            else -> null
        }
        updateState { copy(primaryContact = digits, primaryContactError = error) }
    }

    private fun updateSecondaryContact(value: String) {
        val digits = value.filter { it.isDigit() }.take(10)
        val error = if (digits.isNotBlank() && !ValidationUtils.isValidIndianMobile(digits)) {
            UiText.StringRes(Res.string.error_mobile_invalid)
        } else null
        updateState { copy(secondaryContact = digits, secondaryContactError = error) }
    }

    private fun updateEmail(value: String) {
        val error = if (value.isNotBlank() && !ValidationUtils.isValidEmail(value)) {
            UiText.StringRes(Res.string.error_email_invalid)
        } else null
        updateState { copy(email = value, emailError = error) }
    }

    private fun updateGstNumber(value: String) {
        val gst = value.uppercase().filter { it.isLetterOrDigit() }.take(15)
        val error = if (gst.isNotBlank()) {
            when {
                gst.length != 15 -> UiText.StringRes(Res.string.error_gst_invalid)
                !gst.matches(Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")) -> UiText.StringRes(Res.string.error_gst_invalid)
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
                sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.success_customer_created)))
                sendEffect(Effect.NavigateToCustomerDetail(result.data.id))
            }
            is Result.Error -> {
                updateState {
                    copy(
                        isSaving = false,
                        error = (result.message ?: result.exception.message)
                            ?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.error_create_customer)
                    )
                }
            }
            is Result.Loading -> { }
        }
    }

    private fun validateFields(): Boolean {
        val companyNameError = if (state.value.companyName.isBlank()) UiText.StringRes(Res.string.error_company_name_required) else null
        val personNameError = personNameError(state.value.personName)

        val primaryContactError = state.value.primaryContact.let { contact ->
            when {
                contact.isBlank() -> UiText.StringRes(Res.string.error_mobile_required)
                contact.length != 10 -> UiText.StringRes(Res.string.error_mobile_10_digits)
                !contact.first().toString().matches(Regex("[6-9]")) -> UiText.StringRes(Res.string.error_mobile_start_digit)
                else -> null
            }
        }

        val secondaryContactError = state.value.secondaryContact.let { contact ->
            if (contact.isNotBlank()) {
                when {
                    contact.length != 10 -> UiText.StringRes(Res.string.error_mobile_invalid)
                    !contact.first().toString().matches(Regex("[6-9]")) -> UiText.StringRes(Res.string.error_mobile_start_digit)
                    else -> null
                }
            } else null
        }

        val emailError = if (state.value.email.isNotBlank() && !ValidationUtils.isValidEmail(state.value.email)) {
            UiText.StringRes(Res.string.error_email_invalid)
        } else null

        val gstNumberError = state.value.gstNumber.let { gst ->
            if (gst.isNotBlank()) {
                when {
                    gst.length != 15 -> UiText.StringRes(Res.string.error_gst_invalid)
                    !gst.matches(Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")) -> UiText.StringRes(Res.string.error_gst_invalid)
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

