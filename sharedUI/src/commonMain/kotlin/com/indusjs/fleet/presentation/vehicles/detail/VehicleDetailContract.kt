package com.indusjs.fleet.presentation.vehicles.detail

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.vehicle.DocumentsSummary
import com.indusjs.fleet.domain.entity.vehicle.RouteInfo
import com.indusjs.fleet.domain.entity.vehicle.TripsSummary
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.entity.vehicle.VehicleDocumentsData
import com.indusjs.fleet.domain.entity.vehicle.VehicleStats
import com.indusjs.fleet.domain.entity.vehicle.VehicleStatus
import com.indusjs.fleet.domain.entity.vehicle.VehicleTripItem
import com.indusjs.fleet.domain.entity.vehicle.VehicleTripsData
import com.indusjs.fleet.domain.entity.vehicle.VehicleType

/**
 * MVI Contract for the Vehicle Detail screen.
 */
object VehicleDetailContract {

    /**
     * Fuel type options.
     */
    val fuelTypes = listOf("Diesel", "Petrol", "CNG", "Electric", "Hybrid")

    /**
     * UI State for the Vehicle Detail screen.
     */
    data class State(
        // Vehicle data
        val vehicle: Vehicle? = null,
        val vehicleId: String = "",

        // Edit mode
        val isEditMode: Boolean = false,

        // Editable fields
        val registrationNumber: String = "",
        val make: String = "",
        val model: String = "",
        val year: String = "",
        val vehicleType: VehicleType = VehicleType.CAR,
        val fuelType: String = "Diesel",
        val color: String = "",
        val capacity: String = "",

        // Validation errors
        val makeError: String? = null,
        val modelError: String? = null,
        val yearError: String? = null,

        // Form state
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,

        // Available options
        val vehicleTypes: List<VehicleType> = VehicleType.entries,
        val fuelTypeOptions: List<String> = fuelTypes,
        val statusOptions: List<VehicleStatus> = VehicleStatus.entries,

        // ==================== Tab Data ====================

        // Overview Tab
        val stats: VehicleStats = VehicleStats(),
        val documentsSummary: DocumentsSummary = DocumentsSummary(),

        // Trips Tab
        val tripsData: VehicleTripsData? = null,
        val tripsList: List<VehicleTripItem> = emptyList(),
        val tripsSummary: TripsSummary = TripsSummary(),
        val isLoadingTrips: Boolean = false,
        val tripsError: String? = null,
        val tripsPage: Int = 1,
        val hasMoreTrips: Boolean = false,

        // Route & Stops Tab
        val routeInfo: RouteInfo? = null,
        val isLoadingRoute: Boolean = false,
        val routeError: String? = null,

        // Documents Tab
        val documentsData: VehicleDocumentsData? = null,
        val isLoadingDocuments: Boolean = false,
        val documentsError: String? = null,

        // Document Upload
        val isUploadDialogVisible: Boolean = false,
        val selectedDocumentType: String? = null,
        val selectedDocumentTypeName: String? = null,
        val isUploading: Boolean = false,
        val uploadError: String? = null,

        // Current selected tab
        val selectedTab: Int = 0
    ) : UiState {

        val isFormValid: Boolean
            get() = make.isNotBlank() &&
                    model.isNotBlank() &&
                    year.isNotBlank() &&
                    year.toIntOrNull() != null &&
                    makeError == null &&
                    modelError == null &&
                    yearError == null

        val canSave: Boolean
            get() = isFormValid && !isSaving && isEditMode
    }

    /**
     * User intents for the Vehicle Detail screen.
     */
    sealed interface Intent : UiIntent {
        // Load vehicle
        data class LoadVehicle(val vehicleId: String) : Intent

        // Edit mode
        data object EnterEditMode : Intent
        data object ExitEditMode : Intent

        // Field updates (in edit mode)
        data class UpdateMake(val value: String) : Intent
        data class UpdateModel(val value: String) : Intent
        data class UpdateYear(val value: String) : Intent
        data class UpdateVehicleType(val type: VehicleType) : Intent
        data class UpdateFuelType(val value: String) : Intent
        data class UpdateColor(val value: String) : Intent
        data class UpdateCapacity(val value: String) : Intent

        // Save changes
        data object SaveChanges : Intent

        // Delete vehicle
        data object DeleteVehicle : Intent
        data object ConfirmDelete : Intent

        // Navigation
        data object NavigateBack : Intent

        // Error handling
        data object ClearError : Intent
        data object Refresh : Intent

        // ==================== Tab-specific Intents ====================

        // Tab selection
        data class SelectTab(val tabIndex: Int) : Intent

        // Trips Tab
        data object LoadTrips : Intent
        data object LoadMoreTrips : Intent
        data object RefreshTrips : Intent

        // Route Tab
        data object LoadRoute : Intent
        data object RefreshRoute : Intent

        // Documents Tab
        data object LoadDocuments : Intent
        data object RefreshDocuments : Intent

        // Document Upload
        data class ShowUploadDialog(val documentType: String, val documentTypeName: String) : Intent
        data object HideUploadDialog : Intent
        data class UploadDocument(
            val documentType: String,
            val documentName: String,
            val fileBytes: ByteArray,
            val fileName: String,
            val mimeType: String,
            val documentNumber: String? = null,
            val expiryDate: String? = null
        ) : Intent

        // Document Preview/Download
        data class PreviewDocument(val documentId: String, val documentName: String, val fileUrl: String?) : Intent
        data class DownloadDocument(val documentId: String, val documentName: String, val fileUrl: String?) : Intent
        data class ReplaceDocument(val documentType: String, val documentTypeName: String) : Intent
    }

    /**
     * Side effects for the Vehicle Detail screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data class ShowError(val message: String) : Effect
        data object NavigateBack : Effect
        data object ShowDeleteConfirmation : Effect
        data class VehicleDeleted(val vehicleId: String) : Effect
        data object VehicleUpdated : Effect
        data object DocumentUploaded : Effect
        data object OpenFilePicker : Effect
        data class OpenDocumentPreview(val documentId: String, val documentName: String, val fileUrl: String) : Effect
        data class DownloadDocumentFile(val documentId: String, val documentName: String, val fileUrl: String) : Effect
        data class DocumentDownloaded(val documentName: String, val fileBytes: ByteArray, val mimeType: String) : Effect
        data object DocumentDownloading : Effect
    }
}
