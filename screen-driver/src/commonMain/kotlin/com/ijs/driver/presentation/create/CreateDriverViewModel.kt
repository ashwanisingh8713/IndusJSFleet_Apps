package com.ijs.driver.presentation.create

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.uicomponents.components.UiText
import com.ijs.team.data.model.TeamMemberDto
import com.ijs.driver.domain.entity.Driver
import com.ijs.driver.domain.entity.DriverStatus
import com.ijs.driver.domain.entity.LicenseType
import com.ijs.team.domain.repository.TeamRepository
import com.ijs.driver.domain.usecase.CreateDriverUseCase
import com.ijs.driver.presentation.create.CreateDriverContract.Effect
import com.ijs.driver.presentation.create.CreateDriverContract.Intent
import com.ijs.driver.presentation.create.CreateDriverContract.State
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_first_name_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_last_name_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_mobile_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_mobile_invalid
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_password_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_license_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_license_expiry_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_email_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_email_invalid
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_fill_required_fields
import indusjsfleet.ijs_ui_components_lib.generated.resources.success_driver_created
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_create_driver
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Create Driver screen implementing MVI pattern.
 */
@Inject
class CreateDriverViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val createDriverUseCase: CreateDriverUseCase,
    private val teamRepository: TeamRepository
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        // Load caretakers (supervisors + managers) on init
        sendIntent(Intent.LoadCaretakers)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            // Required field updates
            is Intent.UpdateFirstName -> updateFirstName(intent.value)
            is Intent.UpdateLastName -> updateLastName(intent.value)
            is Intent.UpdateMobile -> updateMobile(intent.value)
            is Intent.UpdatePassword -> updatePassword(intent.value)
            is Intent.UpdateLicenseNumber -> updateLicenseNumber(intent.value)

            // Optional field updates
            is Intent.UpdateEmail -> updateEmail(intent.value)
            is Intent.UpdateLicenseType -> updateState { copy(licenseType = intent.type) }
            is Intent.UpdateLicenseExpiry -> updateState { copy(licenseExpiry = intent.value) }
            is Intent.UpdateDateOfBirth -> updateState { copy(dateOfBirth = intent.value) }
            is Intent.UpdateAddress -> updateState { copy(address = intent.value) }
            is Intent.UpdateEmergencyContact -> updateState { copy(emergencyContact = intent.value) }
            is Intent.UpdateBloodGroup -> updateState { copy(bloodGroup = intent.value) }
            is Intent.UpdateJoiningDate -> updateState { copy(joiningDate = intent.value) }

            // Caretaker management
            is Intent.LoadCaretakers -> loadCaretakers()
            is Intent.RefreshCaretakers -> refreshCaretakers()
            is Intent.ToggleCaretakerDropdown -> updateState { copy(showCaretakerDropdown = !showCaretakerDropdown) }
            is Intent.SelectCaretaker -> updateState { copy(selectedCaretaker = intent.caretaker, showCaretakerDropdown = false) }

            // Form actions
            is Intent.ValidateForm -> validateForm()
            is Intent.SubmitDriver -> submitDriver()
            is Intent.Cancel -> sendEffect(Effect.NavigateBack)
            is Intent.ClearError -> updateState { copy(error = null) }
        }
    }

    private fun updateFirstName(value: String) {
        val error = if (value.isBlank()) UiText.StringRes(Res.string.error_first_name_required) else null
        updateState { copy(firstName = value, firstNameError = error) }
    }

    private fun updateLastName(value: String) {
        val error = if (value.isBlank()) UiText.StringRes(Res.string.error_last_name_required) else null
        updateState { copy(lastName = value, lastNameError = error) }
    }

    private fun updateMobile(value: String) {
        val error = validateMobile(value)
        updateState { copy(mobile = value, mobileError = error) }
    }

    private fun updatePassword(value: String) {
        updateState { copy(password = value, passwordError = validatePassword(value)) }
    }

    private fun updateLicenseNumber(value: String) {
        val error = if (value.isBlank()) UiText.StringRes(Res.string.error_license_required) else null
        updateState { copy(licenseNumber = value.uppercase(), licenseNumberError = error) }
    }

    private fun updateEmail(value: String) {
        // Email is REQUIRED: the backend creates an IAM login for the driver and rejects
        // a missing email (500 "Email: this field is required").
        val error = when {
            value.isBlank() -> UiText.StringRes(Res.string.error_email_required)
            !isValidEmail(value) -> UiText.StringRes(Res.string.error_email_invalid)
            else -> null
        }
        updateState { copy(email = value, emailError = error) }
    }

    private fun validateMobile(value: String): UiText? {
        return when {
            value.isBlank() -> UiText.StringRes(Res.string.error_mobile_required)
            !ValidationUtils.isValidIndianMobile(value) -> UiText.StringRes(Res.string.error_mobile_invalid)
            else -> null
        }
    }

    private fun validatePassword(value: String): UiText? {
        // Required only; password policy (length/complexity) is enforced by the backend.
        return if (value.isBlank()) UiText.StringRes(Res.string.error_password_required) else null
    }

    private fun isValidEmail(email: String): Boolean = ValidationUtils.isValidEmail(email)

    private fun validateForm(): Boolean {
        val firstNameError = if (currentState.firstName.isBlank()) UiText.StringRes(Res.string.error_first_name_required) else null
        val lastNameError = if (currentState.lastName.isBlank()) UiText.StringRes(Res.string.error_last_name_required) else null
        val mobileError = validateMobile(currentState.mobile)
        val passwordError = validatePassword(currentState.password)
        val licenseNumberError = if (currentState.licenseNumber.isBlank()) UiText.StringRes(Res.string.error_license_required) else null
        val licenseExpiryError = if (currentState.licenseExpiry.isBlank()) UiText.StringRes(Res.string.error_license_expiry_required) else null
        val emailError = when {
            currentState.email.isBlank() -> UiText.StringRes(Res.string.error_email_required)
            !isValidEmail(currentState.email) -> UiText.StringRes(Res.string.error_email_invalid)
            else -> null
        }

        updateState {
            copy(
                firstNameError = firstNameError,
                lastNameError = lastNameError,
                mobileError = mobileError,
                passwordError = passwordError,
                licenseNumberError = licenseNumberError,
                licenseExpiryError = licenseExpiryError,
                emailError = emailError
            )
        }

        return firstNameError == null && lastNameError == null &&
                mobileError == null && passwordError == null && licenseNumberError == null &&
                licenseExpiryError == null && emailError == null
    }

    private suspend fun submitDriver() {
        if (!validateForm()) {
            sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.error_fill_required_fields)))
            return
        }

        updateState { copy(isSaving = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                val driver = Driver(
                    id = "", // Will be assigned by backend
                    firstName = currentState.firstName.trim(),
                    lastName = currentState.lastName.trim(),
                    email = currentState.email.trim(),
                    mobile = currentState.mobile.trim(),
                    licenseNumber = currentState.licenseNumber.trim(),
                    licenseType = currentState.licenseType,
                    licenseExpiry = parseDateToTimestamp(currentState.licenseExpiry),
                    dateOfBirth = currentState.dateOfBirth.takeIf { it.isNotBlank() }?.let { parseDateToTimestamp(it) },
                    address = currentState.address.takeIf { it.isNotBlank() },
                    emergencyContact = currentState.emergencyContact.takeIf { it.isNotBlank() },
                    bloodGroup = currentState.bloodGroup.takeIf { it.isNotBlank() },
                    joiningDate = currentState.joiningDate.takeIf { it.isNotBlank() }?.let { parseDateToTimestamp(it) },
                    status = DriverStatus.ACTIVE,
                    isActive = true
                )

                when (val result = createDriverUseCase(
                    driver = driver,
                    password = currentState.password,
                    caretakerId = currentState.selectedCaretaker?.id
                )) {
                    is Result.Success -> {
                        updateState { copy(isSaving = false) }
                        sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.success_driver_created)))
                        sendEffect(Effect.DriverCreated(result.data.id))
                        sendEffect(Effect.NavigateBack)
                    }
                    is Result.Error -> {
                        val errorText = result.message?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.error_create_driver)
                        updateState {
                            copy(
                                isSaving = false,
                                error = errorText
                            )
                        }
                        sendEffect(Effect.ShowError(errorText))
                    }
                    is Result.Loading -> { /* Not applicable */ }
                }
            } catch (e: Exception) {
                val errorText = e.message?.let { UiText.Raw(it) }
                    ?: UiText.StringRes(Res.string.error_create_driver)
                updateState {
                    copy(
                        isSaving = false,
                        error = errorText
                    )
                }
                sendEffect(Effect.ShowError(errorText))
            }
        }
    }

    /**
     * Parse a picked date string (DD-MM-YYYY, from FleetDatePicker) to UTC epoch
     * millis via the canonical helper. Returns 0L when blank/invalid.
     */
    private fun parseDateToTimestamp(dateString: String): Long =
        com.indusjs.fleet.core.util.convertToEpochMillis(dateString) ?: 0L

    /**
     * Load caretakers from local cache first.
     * If cache is empty, fetch from API.
     */
    private suspend fun loadCaretakers() {
        updateState { copy(isLoadingCaretakers = true) }

        withContext(dispatcherProvider.io) {
            // First try to load from cache
            val cacheResult = teamRepository.getCaretakersFromCache()
            cacheResult.fold(
                onSuccess = { cachedCaretakers ->
                    if (cachedCaretakers.isNotEmpty()) {
                        val caretakers = cachedCaretakers.map { it.toDto() }
                        updateState { copy(isLoadingCaretakers = false, caretakers = caretakers) }
                    } else {
                        // Cache is empty, fetch from API
                        fetchCaretakersFromApi()
                    }
                },
                onFailure = {
                    // Cache failed, fetch from API
                    fetchCaretakersFromApi()
                }
            )
        }
    }

    /**
     * Refresh caretakers from API and update local cache.
     */
    private suspend fun refreshCaretakers() {
        updateState { copy(isLoadingCaretakers = true) }
        withContext(dispatcherProvider.io) {
            fetchCaretakersFromApi()
        }
    }

    private suspend fun fetchCaretakersFromApi() {
        val result = teamRepository.refreshTeamMembers()
        result.fold(
            onSuccess = { teamMembers ->
                // Filter to only supervisors and managers
                val caretakers = teamMembers
                    .filter { member ->
                        member.isCaretakerEligible
                    }
                    .map { it.toDto() }
                updateState { copy(isLoadingCaretakers = false, caretakers = caretakers) }
            },
            onFailure = {
                updateState { copy(isLoadingCaretakers = false) }
            }
        )
    }

    private fun com.ijs.team.domain.entity.TeamMember.toDto(): TeamMemberDto {
        return TeamMemberDto(
            id = id.toIntOrNull() ?: 0,
            email = email,
            mobile = mobile,
            firstName = firstName,
            lastName = lastName,
            role = role.toApiString(),
            ownerId = ownerId.toIntOrNull() ?: 0,
            isActive = isActive,
            isCaretakerEligible = isCaretakerEligible,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

