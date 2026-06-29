package com.ijs.vehicle.presentation

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText
import com.ijs.team.data.model.TeamMemberDto
import com.ijs.vehicle.domain.entity.DocumentType
import com.ijs.vehicle.domain.entity.FuelTypeLabel
import com.ijs.vehicle.domain.entity.VehicleDocument
import com.ijs.vehicle.domain.entity.VehicleType
import com.ijs.vehicle.domain.entity.VehicleTypeOption

/**
 * MVI Contract for the Add/Register Vehicle screen.
 */
object AddVehicleContract {

    /**
     * UI State for the Add Vehicle screen.
     */
    data class State(
        // Form fields
        val registrationNumber: String = "",
        val make: String = "",
        val model: String = "",
        val year: String = "",
        val vehicleType: VehicleType = VehicleType.TRUCK,
        val chassisNumber: String = "",
        val engineNumber: String = "",
        val fuelType: String = "Diesel",
        val color: String = "",
        val ownerName: String = "",
        val ownerContact: String = "",

        // Documents
        val documents: List<VehicleDocument> = emptyList(),
        val uploadingDocument: DocumentType? = null,
        val uploadProgress: Float = 0f,
        val documentExpiryDates: Map<DocumentType, String> = emptyMap(), // Raw digits for each doc type

        // Validation
        val registrationNumberError: UiText? = null,
        val makeError: UiText? = null,
        val modelError: UiText? = null,
        val yearError: UiText? = null,
        val ownerNameError: UiText? = null,
        val ownerContactError: UiText? = null,

        // Form state
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val error: UiText? = null,
        val currentStep: Int = 0, // 0 = Basic Info, 1 = Documents

        // Available options
        val vehicleTypes: List<VehicleType> = VehicleType.entries,
        // Config-driven Vehicle Type → Fuel Type map, loaded from the bundled `vehicle_types.json`
        // asset (mirrors the cargo-material config). Empty until loaded; the UI falls back to the
        // VehicleType enum / full fuel list so the form always works.
        val vehicleTypeOptions: List<VehicleTypeOption> = emptyList(),
        // Display labels (incl. Hindi) for every fuel value; used to localize the fuel chips.
        val fuelTypeLabels: List<FuelTypeLabel> = emptyList(),
        // Fuel options shown for the CURRENTLY-selected vehicle type. Derived from the config on
        // every Vehicle Type change. Defaults to the default type's (TRUCK) single fuel so the
        // dropdown is already correct on first render, before the config asset loads.
        val fuelTypes: List<String> = listOf("Diesel"),
        val documentTypes: List<DocumentType> = DocumentType.entries,

        // Caretaker assignment (optional)
        val caretakers: List<TeamMemberDto> = emptyList(),
        val selectedCaretaker: TeamMemberDto? = null,
        val showCaretakerDropdown: Boolean = false,
        val isLoadingCaretakers: Boolean = false
    ) : UiState {
        val isBasicInfoValid: Boolean
            get() = registrationNumber.isNotBlank() &&
                    make.isNotBlank() &&
                    model.isNotBlank() &&
                    year.isNotBlank() &&
                    year.toIntOrNull() != null &&
                    registrationNumberError == null &&
                    makeError == null &&
                    modelError == null &&
                    yearError == null &&
                    ownerNameError == null &&
                    ownerContactError == null


        val canSubmit: Boolean
            get() = isBasicInfoValid && !isSaving

        /**
         * Resolve a [VehicleType]'s display label. Picks the Hindi label when [hindi] is set (and
         * present), else the English label from the config, falling back to the capitalized enum
         * name when the config isn't loaded yet.
         */
        fun vehicleTypeLabelFor(type: VehicleType, hindi: Boolean = false): String {
            val option = vehicleTypeOptions.firstOrNull { it.id == type.name.lowercase() }
            val name = if (hindi) option?.labelHi?.takeIf { it.isNotBlank() } ?: option?.label
                       else option?.label
            return name ?: type.name.lowercase().replaceFirstChar { it.uppercaseChar() }
        }

        /**
         * Resolve a fuel VALUE to its display label. Picks the Hindi label when [hindi] is set
         * (falling back to the English label when blank), else the English label. Falls back to the
         * raw [value] when no label entry exists. Never changes the value itself.
         */
        fun fuelLabelFor(value: String, hindi: Boolean = false): String =
            fuelTypeLabels.firstOrNull { it.value == value }?.let {
                if (hindi) it.labelHi.ifBlank { it.label } else it.label
            } ?: value
    }

    /**
     * User intents for the Add Vehicle screen.
     */
    sealed interface Intent : UiIntent {
        // Form field updates
        data class UpdateRegistrationNumber(val value: String) : Intent
        data class UpdateMake(val value: String) : Intent
        data class UpdateModel(val value: String) : Intent
        data class UpdateYear(val value: String) : Intent
        data class UpdateVehicleType(val type: VehicleType) : Intent
        data class UpdateChassisNumber(val value: String) : Intent
        data class UpdateEngineNumber(val value: String) : Intent
        data class UpdateFuelType(val value: String) : Intent
        data class UpdateColor(val value: String) : Intent
        data class UpdateOwnerName(val value: String) : Intent
        data class UpdateOwnerContact(val value: String) : Intent

        // Navigation between steps
        data object NextStep : Intent
        data object PreviousStep : Intent
        data class GoToStep(val step: Int) : Intent

        // Document management
        data class SelectDocument(val type: DocumentType) : Intent
        data class UploadDocument(
            val type: DocumentType,
            val fileName: String,
            val fileBytes: ByteArray,
            val mimeType: String
        ) : Intent
        data class RemoveDocument(val documentId: String) : Intent
        data class UpdateDocumentExpiry(val documentId: String, val expiryDate: Long) : Intent
        data class UpdateDocumentExpiryDate(val type: DocumentType, val rawDigits: String) : Intent

        // Caretaker management
        data object LoadCaretakers : Intent
        data object RefreshCaretakers : Intent
        data object ToggleCaretakerDropdown : Intent
        data class SelectCaretaker(val caretaker: TeamMemberDto?) : Intent
        data object NavigateToCreateTeamMember : Intent

        // Form actions
        data object ValidateBasicInfo : Intent
        data object SubmitVehicle : Intent
        data object Cancel : Intent
        data object ClearError : Intent
    }

    /**
     * Side effects for the Add Vehicle screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: UiText) : Effect
        data object NavigateBack : Effect
        data class ShowDocumentPicker(val type: DocumentType) : Effect
        data class VehicleRegistered(val vehicleId: String) : Effect
        data object NavigateToCreateTeamMember : Effect
    }
}
