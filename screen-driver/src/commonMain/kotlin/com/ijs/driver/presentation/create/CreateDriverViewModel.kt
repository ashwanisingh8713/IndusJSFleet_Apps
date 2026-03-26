package com.ijs.driver.presentation.create

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.error.result.Result
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
        val error = if (value.isBlank()) "First name is required" else null
        updateState { copy(firstName = value, firstNameError = error) }
    }

    private fun updateLastName(value: String) {
        val error = if (value.isBlank()) "Last name is required" else null
        updateState { copy(lastName = value, lastNameError = error) }
    }

    private fun updateMobile(value: String) {
        val error = validateMobile(value)
        updateState { copy(mobile = value, mobileError = error) }
    }

    private fun updateLicenseNumber(value: String) {
        val error = if (value.isBlank()) "License number is required" else null
        updateState { copy(licenseNumber = value.uppercase(), licenseNumberError = error) }
    }

    private fun updateEmail(value: String) {
        val error = if (value.isNotBlank() && !isValidEmail(value)) "Invalid email format" else null
        updateState { copy(email = value, emailError = error) }
    }

    private fun validateMobile(value: String): String? {
        return when {
            value.isBlank() -> "Mobile number is required"
            value.length < 10 -> "Mobile number must be at least 10 digits"
            !value.all { it.isDigit() } -> "Mobile number must contain only digits"
            else -> null
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email)
    }

    private fun validateForm(): Boolean {
        val firstNameError = if (currentState.firstName.isBlank()) "First name is required" else null
        val lastNameError = if (currentState.lastName.isBlank()) "Last name is required" else null
        val mobileError = validateMobile(currentState.mobile)
        val licenseNumberError = if (currentState.licenseNumber.isBlank()) "License number is required" else null
        val licenseExpiryError = if (currentState.licenseExpiry.isBlank()) "License expiry date is required" else null
        val emailError = if (currentState.email.isNotBlank() && !isValidEmail(currentState.email)) "Invalid email format" else null

        updateState {
            copy(
                firstNameError = firstNameError,
                lastNameError = lastNameError,
                mobileError = mobileError,
                licenseNumberError = licenseNumberError,
                licenseExpiryError = licenseExpiryError,
                emailError = emailError
            )
        }

        return firstNameError == null && lastNameError == null &&
                mobileError == null && licenseNumberError == null &&
                licenseExpiryError == null && emailError == null
    }

    private suspend fun submitDriver() {
        if (!validateForm()) {
            sendEffect(Effect.ShowSnackbar("Please fill all required fields correctly"))
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

                when (val result = createDriverUseCase(driver)) {
                    is Result.Success -> {
                        updateState { copy(isSaving = false) }
                        sendEffect(Effect.ShowSnackbar("Driver created successfully!"))
                        sendEffect(Effect.DriverCreated(result.data.id))
                        sendEffect(Effect.NavigateBack)
                    }
                    is Result.Error -> {
                        updateState {
                            copy(
                                isSaving = false,
                                error = result.message ?: "Failed to create driver"
                            )
                        }
                        sendEffect(Effect.ShowError(result.message ?: "Failed to create driver"))
                    }
                    is Result.Loading -> { /* Not applicable */ }
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isSaving = false,
                        error = e.message ?: "Failed to create driver"
                    )
                }
                sendEffect(Effect.ShowError(e.message ?: "Failed to create driver"))
            }
        }
    }

    /**
     * Parse date string (DD-MM-YYYY format from DatePicker) to timestamp.
     */
    private fun parseDateToTimestamp(dateString: String): Long {
        if (dateString.isBlank()) return 0L
        return try {
            val parts = dateString.split("-")
            if (parts.size == 3) {
                // DatePicker returns DD-MM-YYYY format
                val day = parts[0].toInt()
                val month = parts[1].toInt()
                val year = parts[2].toInt()
                // Approximate conversion to epoch milliseconds
                val baseYear = 1970
                val daysFromBase = ((year - baseYear) * 365.25).toLong() +
                        (month - 1) * 30L + day
                daysFromBase * 24 * 60 * 60 * 1000
            } else {
                0L
            }
        } catch (_: Exception) {
            0L
        }
    }

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
                        member.role.name.lowercase() in listOf("supervisor", "manager")
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
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

