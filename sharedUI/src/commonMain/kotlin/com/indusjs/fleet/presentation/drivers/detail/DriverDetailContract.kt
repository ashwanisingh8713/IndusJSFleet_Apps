package com.indusjs.fleet.presentation.drivers.detail

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.data.model.history.HistoryItemDto
import com.indusjs.fleet.data.model.team.TeamMemberDto
import com.indusjs.fleet.domain.entity.driver.Driver
import com.indusjs.fleet.domain.entity.driver.DriverStatus
import com.indusjs.fleet.domain.entity.driver.LicenseType

/**
 * MVI Contract for the Driver Detail screen.
 */
object DriverDetailContract {

    /**
     * Blood group options.
     */
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    /**
     * UI State for the Driver Detail screen.
     */
    data class State(
        // Driver data
        val driver: Driver? = null,
        val driverId: String = "",

        // Edit mode
        val isEditMode: Boolean = false,

        // Editable fields
        val firstName: String = "",
        val lastName: String = "",
        val email: String = "",
        val mobile: String = "",
        val licenseNumber: String = "",
        val licenseType: LicenseType = LicenseType.LMV,
        val licenseExpiry: String = "",
        val dateOfBirth: String = "",
        val address: String = "",
        val emergencyContact: String = "",
        val bloodGroup: String = "",

        // Validation errors
        val firstNameError: String? = null,
        val lastNameError: String? = null,
        val mobileError: String? = null,
        val emailError: String? = null,

        // Form state
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,

        // Available options
        val licenseTypes: List<LicenseType> = LicenseType.entries,
        val bloodGroupOptions: List<String> = bloodGroups,
        val statusOptions: List<DriverStatus> = DriverStatus.entries,

        // Caretaker assignment
        val caretakers: List<TeamMemberDto> = emptyList(),
        val selectedCaretaker: TeamMemberDto? = null,
        val showCaretakerDropdown: Boolean = false,
        val isLoadingCaretakers: Boolean = false,

        // History Tab
        val historyItems: List<HistoryItemDto> = emptyList(),
        val isLoadingHistory: Boolean = false,
        val historyError: String? = null,
        val historyPage: Int = 1,
        val hasMoreHistory: Boolean = false,
        val historyTotalCount: Int = 0,

        // Current selected tab (0 = Overview, 1 = History)
        val selectedTab: Int = 0
    ) : UiState {

        val isFormValid: Boolean
            get() = firstName.isNotBlank() &&
                    lastName.isNotBlank() &&
                    mobile.isNotBlank() &&
                    firstNameError == null &&
                    lastNameError == null &&
                    mobileError == null &&
                    emailError == null

        val canSave: Boolean
            get() = isFormValid && !isSaving && isEditMode
    }

    /**
     * User intents for the Driver Detail screen.
     */
    sealed interface Intent : UiIntent {
        // Load driver
        data class LoadDriver(val driverId: String) : Intent

        // Edit mode
        data object EnterEditMode : Intent
        data object ExitEditMode : Intent

        // Field updates (in edit mode)
        data class UpdateFirstName(val value: String) : Intent
        data class UpdateLastName(val value: String) : Intent
        data class UpdateEmail(val value: String) : Intent
        data class UpdateMobile(val value: String) : Intent
        data class UpdateLicenseType(val type: LicenseType) : Intent
        data class UpdateLicenseExpiry(val value: String) : Intent
        data class UpdateDateOfBirth(val value: String) : Intent
        data class UpdateAddress(val value: String) : Intent
        data class UpdateEmergencyContact(val value: String) : Intent
        data class UpdateBloodGroup(val value: String) : Intent

        // Status update (quick action)
        data class UpdateStatus(val status: DriverStatus) : Intent

        // Toggle active
        data object ToggleActive : Intent

        // Caretaker assignment
        data object LoadCaretakers : Intent
        data object RefreshCaretakers : Intent
        data object ToggleCaretakerDropdown : Intent
        data class SelectCaretaker(val caretaker: TeamMemberDto?) : Intent

        // Save changes
        data object SaveChanges : Intent

        // Delete driver
        data object DeleteDriver : Intent
        data object ConfirmDelete : Intent

        // Navigation
        data object NavigateBack : Intent

        // Error handling
        data object ClearError : Intent
        data object Refresh : Intent

        // Tab selection
        data class SelectTab(val tabIndex: Int) : Intent

        // History Tab
        data object LoadHistory : Intent
        data object LoadMoreHistory : Intent
        data object RefreshHistory : Intent
    }

    /**
     * Side effects for the Driver Detail screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data class ShowError(val message: String) : Effect
        data object NavigateBack : Effect
        data object ShowDeleteConfirmation : Effect
        data class DriverDeleted(val driverId: String) : Effect
        data object DriverUpdated : Effect
    }
}

