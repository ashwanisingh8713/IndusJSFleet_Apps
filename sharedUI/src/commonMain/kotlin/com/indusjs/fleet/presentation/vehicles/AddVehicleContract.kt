package com.indusjs.fleet.presentation.vehicles

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.vehicle.DocumentType
import com.indusjs.fleet.domain.entity.vehicle.VehicleDocument
import com.indusjs.fleet.domain.entity.vehicle.VehicleType

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
        val vehicleType: VehicleType = VehicleType.CAR,
        val chassisNumber: String = "",
        val engineNumber: String = "",
        val fuelType: String = "Diesel",
        val color: String = "",
        val seatingCapacity: String = "",
        val ownerName: String = "",
        val ownerContact: String = "",

        // Documents
        val documents: List<VehicleDocument> = emptyList(),
        val uploadingDocument: DocumentType? = null,
        val uploadProgress: Float = 0f,

        // Validation
        val registrationNumberError: String? = null,
        val makeError: String? = null,
        val modelError: String? = null,
        val yearError: String? = null,

        // Form state
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,
        val currentStep: Int = 0, // 0 = Basic Info, 1 = Documents

        // Available options
        val vehicleTypes: List<VehicleType> = VehicleType.entries,
        val fuelTypes: List<String> = listOf("Diesel", "Petrol", "CNG", "Electric", "Hybrid"),
        val documentTypes: List<DocumentType> = DocumentType.entries
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
                    yearError == null

        val hasRequiredDocuments: Boolean
            get() = documents.any { it.type == DocumentType.REGISTRATION_CERTIFICATE } &&
                    documents.any { it.type == DocumentType.INSURANCE }

        val canSubmit: Boolean
            get() = isBasicInfoValid && hasRequiredDocuments && !isSaving
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
        data class UpdateSeatingCapacity(val value: String) : Intent
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
        data class ShowSnackbar(val message: String) : Effect
        data object NavigateBack : Effect
        data class NavigateToVehicleDetail(val vehicleId: String) : Effect
        data object RequestFilePicker : Effect
        data class ShowDocumentPicker(val type: DocumentType) : Effect
        data class VehicleRegistered(val vehicleId: String) : Effect
    }
}

