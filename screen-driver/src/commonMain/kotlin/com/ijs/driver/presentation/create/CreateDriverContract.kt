package com.ijs.driver.presentation.create

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.ijs.team.data.model.TeamMemberDto
import com.ijs.driver.domain.entity.LicenseType

/**
 * MVI Contract for the Create Driver screen.
 */
object CreateDriverContract {

    /**
     * Blood group options.
     */
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    /**
     * UI State for the Create Driver screen.
     */
    data class State(
        // Required fields
        val firstName: String = "",
        val lastName: String = "",
        val mobile: String = "",
        val licenseNumber: String = "",

        // Optional fields
        val email: String = "",
        val licenseType: LicenseType = LicenseType.LMV,
        val licenseExpiry: String = "", // YYYY-MM-DD format
        val dateOfBirth: String = "", // YYYY-MM-DD format
        val address: String = "",
        val emergencyContact: String = "",
        val bloodGroup: String = "",
        val joiningDate: String = "", // YYYY-MM-DD format

        // Validation errors
        val firstNameError: String? = null,
        val lastNameError: String? = null,
        val mobileError: String? = null,
        val licenseNumberError: String? = null,
        val licenseExpiryError: String? = null,
        val emailError: String? = null,

        // Form state
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,

        // Available options
        val licenseTypes: List<LicenseType> = LicenseType.entries,
        val bloodGroupOptions: List<String> = bloodGroups,

        // Caretaker assignment (optional)
        val caretakers: List<TeamMemberDto> = emptyList(),
        val selectedCaretaker: TeamMemberDto? = null,
        val showCaretakerDropdown: Boolean = false,
        val isLoadingCaretakers: Boolean = false
    ) : UiState {

        val isFormValid: Boolean
            get() = firstName.isNotBlank() &&
                    lastName.isNotBlank() &&
                    mobile.isNotBlank() &&
                    licenseNumber.isNotBlank() &&
                    licenseExpiry.isNotBlank() &&
                    firstNameError == null &&
                    lastNameError == null &&
                    mobileError == null &&
                    licenseNumberError == null &&
                    licenseExpiryError == null &&
                    emailError == null

        val canSubmit: Boolean
            get() = isFormValid && !isSaving
    }

    /**
     * User intents for the Create Driver screen.
     */
    sealed interface Intent : UiIntent {
        // Required field updates
        data class UpdateFirstName(val value: String) : Intent
        data class UpdateLastName(val value: String) : Intent
        data class UpdateMobile(val value: String) : Intent
        data class UpdateLicenseNumber(val value: String) : Intent

        // Optional field updates
        data class UpdateEmail(val value: String) : Intent
        data class UpdateLicenseType(val type: LicenseType) : Intent
        data class UpdateLicenseExpiry(val value: String) : Intent
        data class UpdateDateOfBirth(val value: String) : Intent
        data class UpdateAddress(val value: String) : Intent
        data class UpdateEmergencyContact(val value: String) : Intent
        data class UpdateBloodGroup(val value: String) : Intent
        data class UpdateJoiningDate(val value: String) : Intent

        // Caretaker management
        data object LoadCaretakers : Intent
        data object RefreshCaretakers : Intent
        data object ToggleCaretakerDropdown : Intent
        data class SelectCaretaker(val caretaker: TeamMemberDto?) : Intent

        // Form actions
        data object ValidateForm : Intent
        data object SubmitDriver : Intent
        data object Cancel : Intent
        data object ClearError : Intent
    }

    /**
     * Side effects for the Create Driver screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data object NavigateBack : Effect
        data class DriverCreated(val driverId: String) : Effect
        data class ShowError(val message: String) : Effect
    }
}
