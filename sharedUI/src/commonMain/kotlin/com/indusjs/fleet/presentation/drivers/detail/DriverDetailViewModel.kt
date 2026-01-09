package com.indusjs.fleet.presentation.drivers.detail

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.error.result.Result
import com.indusjs.fleet.domain.entity.driver.Driver
import com.indusjs.fleet.domain.entity.driver.DriverStatus
import com.indusjs.fleet.domain.entity.driver.LicenseType
import com.indusjs.fleet.domain.usecase.driver.DeleteDriverUseCase
import com.indusjs.fleet.domain.usecase.driver.GetDriverByIdUseCase
import com.indusjs.fleet.domain.usecase.driver.ToggleDriverActiveUseCase
import com.indusjs.fleet.domain.usecase.driver.UpdateDriverStatusUseCase
import com.indusjs.fleet.domain.usecase.driver.UpdateDriverUseCase
import com.indusjs.fleet.presentation.drivers.detail.DriverDetailContract.Effect
import com.indusjs.fleet.presentation.drivers.detail.DriverDetailContract.Intent
import com.indusjs.fleet.presentation.drivers.detail.DriverDetailContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Driver Detail screen implementing MVI pattern.
 */
@Inject
class DriverDetailViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val getDriverByIdUseCase: GetDriverByIdUseCase,
    private val updateDriverUseCase: UpdateDriverUseCase,
    private val updateDriverStatusUseCase: UpdateDriverStatusUseCase,
    private val toggleDriverActiveUseCase: ToggleDriverActiveUseCase,
    private val deleteDriverUseCase: DeleteDriverUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadDriver -> loadDriver(intent.driverId)
            is Intent.Refresh -> currentState.driver?.let { loadDriver(it.id) }

            // Edit mode
            is Intent.EnterEditMode -> enterEditMode()
            is Intent.ExitEditMode -> exitEditMode()

            // Field updates
            is Intent.UpdateFirstName -> updateFirstName(intent.value)
            is Intent.UpdateLastName -> updateLastName(intent.value)
            is Intent.UpdateEmail -> updateEmail(intent.value)
            is Intent.UpdateMobile -> updateMobile(intent.value)
            is Intent.UpdateLicenseType -> updateState { copy(licenseType = intent.type) }
            is Intent.UpdateLicenseExpiry -> updateState { copy(licenseExpiry = intent.value) }
            is Intent.UpdateDateOfBirth -> updateState { copy(dateOfBirth = intent.value) }
            is Intent.UpdateAddress -> updateState { copy(address = intent.value) }
            is Intent.UpdateEmergencyContact -> updateState { copy(emergencyContact = intent.value) }
            is Intent.UpdateBloodGroup -> updateState { copy(bloodGroup = intent.value) }

            // Actions
            is Intent.UpdateStatus -> updateStatus(intent.status)
            is Intent.ToggleActive -> toggleActive()
            is Intent.SaveChanges -> saveChanges()
            is Intent.DeleteDriver -> sendEffect(Effect.ShowDeleteConfirmation)
            is Intent.ConfirmDelete -> confirmDelete()

            // Navigation & errors
            is Intent.NavigateBack -> sendEffect(Effect.NavigateBack)
            is Intent.ClearError -> updateState { copy(error = null) }
        }
    }

    private suspend fun loadDriver(driverId: String) {
        updateState { copy(isLoading = true, error = null, driverId = driverId) }

        withContext(dispatcherProvider.io) {
            when (val result = getDriverByIdUseCase(driverId)) {
                is Result.Success -> {
                    val driver = result.data
                    updateState {
                        copy(
                            isLoading = false,
                            driver = driver,
                            // Populate editable fields
                            firstName = driver.firstName,
                            lastName = driver.lastName,
                            email = driver.email,
                            mobile = driver.mobile,
                            licenseNumber = driver.licenseNumber,
                            licenseType = driver.licenseType,
                            licenseExpiry = formatTimestamp(driver.licenseExpiry),
                            dateOfBirth = driver.dateOfBirth?.let { formatTimestamp(it) } ?: "",
                            address = driver.address ?: "",
                            emergencyContact = driver.emergencyContact ?: "",
                            bloodGroup = driver.bloodGroup ?: ""
                        )
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoading = false,
                            error = result.message ?: "Failed to load driver"
                        )
                    }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to load driver"))
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    private fun enterEditMode() {
        updateState { copy(isEditMode = true) }
    }

    private fun exitEditMode() {
        // Reset to original values
        val driver = currentState.driver
        if (driver != null) {
            updateState {
                copy(
                    isEditMode = false,
                    firstName = driver.firstName,
                    lastName = driver.lastName,
                    email = driver.email,
                    mobile = driver.mobile,
                    licenseType = driver.licenseType,
                    licenseExpiry = formatTimestamp(driver.licenseExpiry),
                    dateOfBirth = driver.dateOfBirth?.let { formatTimestamp(it) } ?: "",
                    address = driver.address ?: "",
                    emergencyContact = driver.emergencyContact ?: "",
                    bloodGroup = driver.bloodGroup ?: "",
                    // Clear errors
                    firstNameError = null,
                    lastNameError = null,
                    mobileError = null,
                    emailError = null
                )
            }
        } else {
            updateState { copy(isEditMode = false) }
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

    private fun updateEmail(value: String) {
        val error = if (value.isNotBlank() && !isValidEmail(value)) "Invalid email format" else null
        updateState { copy(email = value, emailError = error) }
    }

    private fun updateMobile(value: String) {
        val error = validateMobile(value)
        updateState { copy(mobile = value, mobileError = error) }
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

    private suspend fun updateStatus(status: DriverStatus) {
        val driverId = currentState.driver?.id ?: return
        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            when (val result = updateDriverStatusUseCase(driverId, status)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isSaving = false,
                            driver = result.data
                        )
                    }
                    sendEffect(Effect.ShowSnackbar("Status updated to ${DriverStatus.toApiString(status)}"))
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to update status"))
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    private suspend fun toggleActive() {
        val driverId = currentState.driver?.id ?: return
        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            when (val result = toggleDriverActiveUseCase(driverId)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isSaving = false,
                            driver = result.data
                        )
                    }
                    val message = if (result.data.isActive) "Driver activated" else "Driver deactivated"
                    sendEffect(Effect.ShowSnackbar(message))
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to toggle active state"))
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    private suspend fun saveChanges() {
        if (!validateForm()) {
            sendEffect(Effect.ShowSnackbar("Please fill all required fields correctly"))
            return
        }

        val currentDriver = currentState.driver ?: return
        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            val updatedDriver = currentDriver.copy(
                firstName = currentState.firstName.trim(),
                lastName = currentState.lastName.trim(),
                email = currentState.email.trim(),
                mobile = currentState.mobile.trim(),
                licenseType = currentState.licenseType,
                licenseExpiry = parseDateToTimestamp(currentState.licenseExpiry),
                dateOfBirth = currentState.dateOfBirth.takeIf { it.isNotBlank() }?.let { parseDateToTimestamp(it) },
                address = currentState.address.takeIf { it.isNotBlank() },
                emergencyContact = currentState.emergencyContact.takeIf { it.isNotBlank() },
                bloodGroup = currentState.bloodGroup.takeIf { it.isNotBlank() }
            )

            when (val result = updateDriverUseCase(updatedDriver)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isSaving = false,
                            isEditMode = false,
                            driver = result.data,
                            // Update fields with response
                            firstName = result.data.firstName,
                            lastName = result.data.lastName,
                            email = result.data.email,
                            mobile = result.data.mobile
                        )
                    }
                    sendEffect(Effect.ShowSnackbar("Driver updated successfully"))
                    sendEffect(Effect.DriverUpdated)
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to update driver"))
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    private fun validateForm(): Boolean {
        val firstNameError = if (currentState.firstName.isBlank()) "First name is required" else null
        val lastNameError = if (currentState.lastName.isBlank()) "Last name is required" else null
        val mobileError = validateMobile(currentState.mobile)
        val emailError = if (currentState.email.isNotBlank() && !isValidEmail(currentState.email)) "Invalid email format" else null

        updateState {
            copy(
                firstNameError = firstNameError,
                lastNameError = lastNameError,
                mobileError = mobileError,
                emailError = emailError
            )
        }

        return firstNameError == null && lastNameError == null && mobileError == null && emailError == null
    }

    private suspend fun confirmDelete() {
        val driverId = currentState.driver?.id ?: return
        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            when (val result = deleteDriverUseCase(driverId)) {
                is Result.Success -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowSnackbar("Driver deleted successfully"))
                    sendEffect(Effect.DriverDeleted(driverId))
                    sendEffect(Effect.NavigateBack)
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to delete driver"))
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp <= 0) return ""
        return try {
            val days = timestamp / (24 * 60 * 60 * 1000)
            val years = (days / 365.25).toInt() + 1970
            val remainingDays = (days % 365.25).toInt()
            val months = (remainingDays / 30) + 1
            val dayOfMonth = (remainingDays % 30) + 1
            val monthStr = months.coerceIn(1, 12).toString().padStart(2, '0')
            val dayStr = dayOfMonth.coerceIn(1, 28).toString().padStart(2, '0')
            "$years-$monthStr-$dayStr"
        } catch (_: Exception) {
            ""
        }
    }

    private fun parseDateToTimestamp(dateString: String): Long {
        if (dateString.isBlank()) return 0L
        return try {
            val parts = dateString.split("-")
            if (parts.size == 3) {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()
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
}

