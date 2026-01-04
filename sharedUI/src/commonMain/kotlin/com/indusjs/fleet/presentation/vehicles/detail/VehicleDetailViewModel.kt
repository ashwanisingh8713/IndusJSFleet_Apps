package com.indusjs.fleet.presentation.vehicles.detail

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.entity.driver.Driver
import com.indusjs.fleet.domain.entity.vehicle.VehicleType
import com.indusjs.fleet.domain.repository.vehicle.VehicleRepository
import com.indusjs.fleet.domain.usecase.driver.GetDriversUseCase
import com.indusjs.fleet.domain.usecase.vehicle.DeleteVehicleUseCase
import com.indusjs.fleet.domain.usecase.vehicle.GetVehicleByIdUseCase
import com.indusjs.fleet.domain.usecase.vehicle.UpdateVehicleUseCase
import com.indusjs.fleet.presentation.vehicles.detail.VehicleDetailContract.Effect
import com.indusjs.fleet.presentation.vehicles.detail.VehicleDetailContract.Intent
import com.indusjs.fleet.presentation.vehicles.detail.VehicleDetailContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Vehicle Detail screen implementing MVI pattern.
 */
@Inject
class VehicleDetailViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val getVehicleByIdUseCase: GetVehicleByIdUseCase,
    private val updateVehicleUseCase: UpdateVehicleUseCase,
    private val deleteVehicleUseCase: DeleteVehicleUseCase,
    private val vehicleRepository: VehicleRepository,
    private val getDriversUseCase: GetDriversUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadVehicle -> loadVehicle(intent.vehicleId)
            is Intent.Refresh -> currentState.vehicle?.let { loadVehicle(it.id) }

            // Edit mode
            is Intent.EnterEditMode -> enterEditMode()
            is Intent.ExitEditMode -> exitEditMode()

            // Field updates
            is Intent.UpdateMake -> updateMake(intent.value)
            is Intent.UpdateModel -> updateModel(intent.value)
            is Intent.UpdateYear -> updateYear(intent.value)
            is Intent.UpdateVehicleType -> updateState { copy(vehicleType = intent.type) }
            is Intent.UpdateFuelType -> updateState { copy(fuelType = intent.value) }
            is Intent.UpdateColor -> updateState { copy(color = intent.value) }
            is Intent.UpdateCapacity -> updateState { copy(capacity = intent.value) }
            is Intent.UpdateMileage -> updateState { copy(mileage = intent.value) }

            // Driver assignment
            is Intent.ToggleDriverDropdown -> updateState { copy(showDriverDropdown = !showDriverDropdown) }
            is Intent.SelectDriver -> selectDriver(intent.driver)

            // Actions
            is Intent.SaveChanges -> saveChanges()
            is Intent.DeleteVehicle -> sendEffect(Effect.ShowDeleteConfirmation)
            is Intent.ConfirmDelete -> confirmDelete()

            // Navigation & errors
            is Intent.NavigateBack -> sendEffect(Effect.NavigateBack)
            is Intent.ClearError -> updateState { copy(error = null) }

            // Tab-specific intents
            is Intent.SelectTab -> selectTab(intent.tabIndex)
            is Intent.LoadTrips -> loadTrips()
            is Intent.LoadMoreTrips -> loadMoreTrips()
            is Intent.RefreshTrips -> refreshTrips()
            is Intent.LoadRoute -> loadRoute()
            is Intent.RefreshRoute -> refreshRoute()
            is Intent.LoadDocuments -> loadDocuments()
            is Intent.RefreshDocuments -> refreshDocuments()

            // Document upload intents
            is Intent.ShowUploadDialog -> showUploadDialog(intent.documentType, intent.documentTypeName)
            is Intent.HideUploadDialog -> hideUploadDialog()
            is Intent.UploadDocument -> uploadDocument(intent)

            // Document preview/download intents
            is Intent.PreviewDocument -> previewDocument(intent)
            is Intent.DownloadDocument -> downloadDocument(intent)
            is Intent.ReplaceDocument -> showUploadDialog(intent.documentType, intent.documentTypeName)
        }
    }

    private suspend fun loadVehicle(vehicleId: String) {
        updateState { copy(isLoading = true, error = null, vehicleId = vehicleId) }

        withContext(dispatcherProvider.io) {
            when (val result = getVehicleByIdUseCase(vehicleId)) {
                is Result.Success -> {
                    val vehicle = result.data
                    updateState {
                        copy(
                            isLoading = false,
                            vehicle = vehicle,
                            // Populate editable fields
                            registrationNumber = vehicle.registrationNumber,
                            make = vehicle.make,
                            model = vehicle.model,
                            year = vehicle.year.toString(),
                            vehicleType = vehicle.type,
                            fuelType = vehicle.fuelType.replaceFirstChar { it.uppercaseChar() },
                            color = vehicle.color.replaceFirstChar { it.uppercaseChar() },
                            capacity = vehicle.capacity.toString(),
                            mileage = if (vehicle.mileage > 0) vehicle.mileage.toString() else ""
                        )
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoading = false,
                            error = result.message ?: "Failed to load vehicle"
                        )
                    }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to load vehicle"))
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    private fun enterEditMode() {
        updateState { copy(isEditMode = true, isLoadingDrivers = true) }
        // Load available drivers
        kotlinx.coroutines.CoroutineScope(dispatcherProvider.main).launch {
            loadDriversForEdit()
        }
    }

    private suspend fun loadDriversForEdit() {
        withContext(dispatcherProvider.io) {
            getDriversUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val vehicle = currentState.vehicle
                        // Find the currently assigned driver
                        val currentDriver = if (vehicle?.assignedDriverId != null) {
                            result.data.find { it.id == vehicle.assignedDriverId }
                        } else null

                        updateState {
                            copy(
                                drivers = result.data,
                                selectedDriver = currentDriver,
                                isLoadingDrivers = false
                            )
                        }
                    }
                    is Result.Error -> {
                        updateState { copy(isLoadingDrivers = false) }
                    }
                    is Result.Loading -> { /* Already handled */ }
                }
            }
        }
    }

    private fun selectDriver(driver: Driver?) {
        updateState { copy(selectedDriver = driver, showDriverDropdown = false) }
    }

    private fun exitEditMode() {
        // Reset to original values
        val vehicle = currentState.vehicle
        if (vehicle != null) {
            updateState {
                copy(
                    isEditMode = false,
                    make = vehicle.make,
                    model = vehicle.model,
                    year = vehicle.year.toString(),
                    vehicleType = vehicle.type,
                    fuelType = vehicle.fuelType.replaceFirstChar { it.uppercaseChar() },
                    color = vehicle.color.replaceFirstChar { it.uppercaseChar() },
                    capacity = vehicle.capacity.toString(),
                    mileage = if (vehicle.mileage > 0) vehicle.mileage.toString() else "",
                    // Reset driver selection
                    selectedDriver = null,
                    drivers = emptyList(),
                    showDriverDropdown = false,
                    // Clear errors
                    makeError = null,
                    modelError = null,
                    yearError = null
                )
            }
        } else {
            updateState { copy(isEditMode = false) }
        }
    }

    private fun updateMake(value: String) {
        val error = if (value.isBlank()) "Make is required" else null
        updateState { copy(make = value, makeError = error) }
    }

    private fun updateModel(value: String) {
        val error = if (value.isBlank()) "Model is required" else null
        updateState { copy(model = value, modelError = error) }
    }

    private fun updateYear(value: String) {
        val error = when {
            value.isBlank() -> "Year is required"
            value.toIntOrNull() == null -> "Invalid year"
            value.toInt() < 1900 || value.toInt() > 2030 -> "Year must be between 1900 and 2030"
            else -> null
        }
        updateState { copy(year = value, yearError = error) }
    }

    private suspend fun saveChanges() {
        if (!validateForm()) {
            sendEffect(Effect.ShowSnackbar("Please fill all required fields correctly"))
            return
        }

        val currentVehicle = currentState.vehicle ?: return
        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            val updatedVehicle = currentVehicle.copy(
                make = currentState.make.trim(),
                model = currentState.model.trim(),
                year = currentState.year.toIntOrNull() ?: currentVehicle.year,
                type = currentState.vehicleType,
                fuelType = currentState.fuelType.lowercase(),
                color = currentState.color.lowercase(),
                capacity = currentState.capacity.toIntOrNull() ?: currentVehicle.capacity,
                mileage = currentState.mileage.toDoubleOrNull() ?: currentVehicle.mileage,
                assignedDriverId = currentState.selectedDriver?.id
            )

            when (val result = updateVehicleUseCase(updatedVehicle)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isSaving = false,
                            isEditMode = false,
                            vehicle = result.data,
                            make = result.data.make,
                            model = result.data.model,
                            year = result.data.year.toString(),
                            vehicleType = result.data.type,
                            fuelType = result.data.fuelType.replaceFirstChar { it.uppercaseChar() },
                            color = result.data.color.replaceFirstChar { it.uppercaseChar() },
                            capacity = result.data.capacity.toString()
                        )
                    }
                    sendEffect(Effect.ShowSnackbar("Vehicle updated successfully"))
                    sendEffect(Effect.VehicleUpdated)
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to update vehicle"))
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    private fun validateForm(): Boolean {
        val makeError = if (currentState.make.isBlank()) "Make is required" else null
        val modelError = if (currentState.model.isBlank()) "Model is required" else null
        val yearError = when {
            currentState.year.isBlank() -> "Year is required"
            currentState.year.toIntOrNull() == null -> "Invalid year"
            else -> null
        }

        updateState {
            copy(
                makeError = makeError,
                modelError = modelError,
                yearError = yearError
            )
        }

        return makeError == null && modelError == null && yearError == null
    }

    private suspend fun confirmDelete() {
        val vehicleId = currentState.vehicle?.id ?: return
        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            when (val result = deleteVehicleUseCase(vehicleId)) {
                is Result.Success -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowSnackbar("Vehicle deleted successfully"))
                    sendEffect(Effect.VehicleDeleted(vehicleId))
                    sendEffect(Effect.NavigateBack)
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to delete vehicle"))
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    // ==================== Tab-specific Functions ====================

    private fun selectTab(tabIndex: Int) {
        updateState { copy(selectedTab = tabIndex) }

        // Load data for the selected tab if not already loaded
        val vehicleId = currentState.vehicleId
        if (vehicleId.isNotEmpty()) {
            when (tabIndex) {
                1 -> if (currentState.tripsList.isEmpty() && !currentState.isLoadingTrips) {
                    sendIntent(Intent.LoadTrips)
                }
                2 -> if (currentState.routeInfo == null && !currentState.isLoadingRoute) {
                    sendIntent(Intent.LoadRoute)
                }
                3 -> if (currentState.documentsData == null && !currentState.isLoadingDocuments) {
                    sendIntent(Intent.LoadDocuments)
                }
            }
        }
    }

    private suspend fun loadTrips() {
        val vehicleId = currentState.vehicleId
        if (vehicleId.isEmpty()) return

        updateState { copy(isLoadingTrips = true, tripsError = null) }

        withContext(dispatcherProvider.io) {
            when (val result = vehicleRepository.getVehicleTrips(vehicleId, 1, 10, null)) {
                is Result.Success -> {
                    val data = result.data
                    updateState {
                        copy(
                            isLoadingTrips = false,
                            tripsData = data,
                            tripsList = data.trips,
                            tripsSummary = data.summary,
                            tripsPage = data.page,
                            hasMoreTrips = data.hasMore
                        )
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoadingTrips = false,
                            tripsError = result.message ?: "Failed to load trips"
                        )
                    }
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    private suspend fun loadMoreTrips() {
        if (!currentState.hasMoreTrips || currentState.isLoadingTrips) return

        val vehicleId = currentState.vehicleId
        val nextPage = currentState.tripsPage + 1

        updateState { copy(isLoadingTrips = true) }

        withContext(dispatcherProvider.io) {
            when (val result = vehicleRepository.getVehicleTrips(vehicleId, nextPage, 10, null)) {
                is Result.Success -> {
                    val data = result.data
                    updateState {
                        copy(
                            isLoadingTrips = false,
                            tripsList = tripsList + data.trips,
                            tripsPage = data.page,
                            hasMoreTrips = data.hasMore
                        )
                    }
                }
                is Result.Error -> {
                    updateState { copy(isLoadingTrips = false) }
                    sendEffect(Effect.ShowSnackbar(result.message ?: "Failed to load more trips"))
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    private suspend fun refreshTrips() {
        updateState { copy(tripsList = emptyList(), tripsPage = 1, hasMoreTrips = false) }
        loadTrips()
    }

    private suspend fun loadRoute() {
        val vehicleId = currentState.vehicleId
        if (vehicleId.isEmpty()) return

        updateState { copy(isLoadingRoute = true, routeError = null) }

        withContext(dispatcherProvider.io) {
            when (val result = vehicleRepository.getVehicleRoute(vehicleId)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isLoadingRoute = false,
                            routeInfo = result.data
                        )
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoadingRoute = false,
                            routeError = result.message ?: "Failed to load route"
                        )
                    }
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    private suspend fun refreshRoute() {
        updateState { copy(routeInfo = null) }
        loadRoute()
    }

    private suspend fun loadDocuments() {
        val vehicleId = currentState.vehicleId
        if (vehicleId.isEmpty()) return

        updateState { copy(isLoadingDocuments = true, documentsError = null) }

        withContext(dispatcherProvider.io) {
            when (val result = vehicleRepository.getVehicleDocumentsDetail(vehicleId)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isLoadingDocuments = false,
                            documentsData = result.data,
                            documentsSummary = result.data.summary
                        )
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoadingDocuments = false,
                            documentsError = result.message ?: "Failed to load documents"
                        )
                    }
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    private suspend fun refreshDocuments() {
        updateState { copy(documentsData = null) }
        loadDocuments()
    }

    // ==================== Document Upload Functions ====================

    private fun showUploadDialog(documentType: String, documentTypeName: String) {
        updateState {
            copy(
                isUploadDialogVisible = true,
                selectedDocumentType = documentType,
                selectedDocumentTypeName = documentTypeName,
                uploadError = null
            )
        }
        sendEffect(Effect.OpenFilePicker)
    }

    private fun hideUploadDialog() {
        updateState {
            copy(
                isUploadDialogVisible = false,
                selectedDocumentType = null,
                selectedDocumentTypeName = null,
                uploadError = null
            )
        }
    }

    private suspend fun uploadDocument(intent: Intent.UploadDocument) {
        val vehicleId = currentState.vehicleId
        if (vehicleId.isEmpty()) return

        updateState { copy(isUploading = true, uploadError = null) }

        withContext(dispatcherProvider.io) {
            when (val result = vehicleRepository.uploadDocument(
                vehicleId = vehicleId,
                documentType = intent.documentType,
                documentName = intent.documentName,
                fileBytes = intent.fileBytes,
                fileName = intent.fileName,
                mimeType = intent.mimeType,
                documentNumber = intent.documentNumber,
                expiryDate = intent.expiryDate
            )) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isUploading = false,
                            isUploadDialogVisible = false,
                            selectedDocumentType = null,
                            selectedDocumentTypeName = null
                        )
                    }
                    sendEffect(Effect.ShowSnackbar("Document uploaded successfully"))
                    sendEffect(Effect.DocumentUploaded)
                    // Refresh documents list
                    loadDocuments()
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isUploading = false,
                            uploadError = result.message ?: "Failed to upload document"
                        )
                    }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to upload document"))
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    // ==================== Document Preview/Download Functions ====================

    private suspend fun previewDocument(intent: Intent.PreviewDocument) {
        val documentId = intent.documentId
        if (documentId.isBlank()) {
            sendEffect(Effect.ShowError("Document ID not available for preview"))
            return
        }

        // Get vehicle registration number for filename prefix
        val vehicleRegNumber = currentState.registrationNumber.ifBlank {
            currentState.vehicle?.registrationNumber ?: ""
        }
        val documentNameWithPrefix = if (vehicleRegNumber.isNotBlank()) {
            "${vehicleRegNumber}_${intent.documentName}"
        } else {
            intent.documentName
        }

        // Download the document and then open for preview
        sendEffect(Effect.DocumentDownloading)
        withContext(dispatcherProvider.io) {
            when (val result = vehicleRepository.downloadDocument(documentId)) {
                is Result.Success -> {
                    sendEffect(Effect.DocumentDownloaded(
                        documentName = documentNameWithPrefix,
                        fileBytes = result.data,
                        mimeType = "application/pdf" // Default to PDF, backend should provide actual type
                    ))
                }
                is Result.Error -> {
                    sendEffect(Effect.ShowError(result.message ?: "Failed to download document for preview"))
                }
                is Result.Loading -> { /* Ignored */ }
            }
        }
    }

    private suspend fun downloadDocument(intent: Intent.DownloadDocument) {
        val documentId = intent.documentId
        if (documentId.isBlank()) {
            sendEffect(Effect.ShowError("Document ID not available for download"))
            return
        }

        // Get vehicle registration number for filename prefix
        val vehicleRegNumber = currentState.registrationNumber.ifBlank {
            currentState.vehicle?.registrationNumber ?: ""
        }
        val documentNameWithPrefix = if (vehicleRegNumber.isNotBlank()) {
            "${vehicleRegNumber}_${intent.documentName}"
        } else {
            intent.documentName
        }

        // Download the document with authentication
        sendEffect(Effect.DocumentDownloading)
        withContext(dispatcherProvider.io) {
            when (val result = vehicleRepository.downloadDocument(documentId)) {
                is Result.Success -> {
                    sendEffect(Effect.DocumentDownloaded(
                        documentName = documentNameWithPrefix,
                        fileBytes = result.data,
                        mimeType = "application/pdf" // Default to PDF
                    ))
                }
                is Result.Error -> {
                    sendEffect(Effect.ShowError(result.message ?: "Failed to download document"))
                }
                is Result.Loading -> { /* Ignored */ }
            }
        }
    }
}
