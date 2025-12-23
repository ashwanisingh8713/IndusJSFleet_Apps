package com.indusjs.fleet.presentation.vehicles

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.entity.vehicle.DocumentStatus
import com.indusjs.fleet.domain.entity.vehicle.DocumentType
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.entity.vehicle.VehicleDocument
import com.indusjs.fleet.domain.entity.vehicle.VehicleStatus
import com.indusjs.fleet.domain.entity.vehicle.VehicleType
import com.indusjs.fleet.domain.usecase.vehicle.CreateVehicleUseCase
import com.indusjs.fleet.presentation.vehicles.AddVehicleContract.Effect
import com.indusjs.fleet.presentation.vehicles.AddVehicleContract.Intent
import com.indusjs.fleet.presentation.vehicles.AddVehicleContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Add/Register Vehicle screen implementing MVI pattern.
 *
 * Dependencies are injected via Metro DI through the VehiclesFeatureGraph.
 */
@Inject
class AddVehicleViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val createVehicleUseCase: CreateVehicleUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            // Form field updates
            is Intent.UpdateRegistrationNumber -> updateRegistrationNumber(intent.value)
            is Intent.UpdateMake -> updateMake(intent.value)
            is Intent.UpdateModel -> updateModel(intent.value)
            is Intent.UpdateYear -> updateYear(intent.value)
            is Intent.UpdateVehicleType -> updateState { copy(vehicleType = intent.type) }
            is Intent.UpdateChassisNumber -> updateState { copy(chassisNumber = intent.value) }
            is Intent.UpdateEngineNumber -> updateState { copy(engineNumber = intent.value) }
            is Intent.UpdateFuelType -> updateState { copy(fuelType = intent.value) }
            is Intent.UpdateColor -> updateState { copy(color = intent.value) }
            is Intent.UpdateSeatingCapacity -> updateState { copy(seatingCapacity = intent.value) }
            is Intent.UpdateOwnerName -> updateState { copy(ownerName = intent.value) }
            is Intent.UpdateOwnerContact -> updateState { copy(ownerContact = intent.value) }

            // Navigation
            is Intent.NextStep -> nextStep()
            is Intent.PreviousStep -> previousStep()
            is Intent.GoToStep -> updateState { copy(currentStep = intent.step) }

            // Document management
            is Intent.SelectDocument -> sendEffect(Effect.ShowDocumentPicker(intent.type))
            is Intent.UploadDocument -> uploadDocument(intent.type, intent.fileName, intent.fileBytes, intent.mimeType)
            is Intent.RemoveDocument -> removeDocument(intent.documentId)
            is Intent.UpdateDocumentExpiry -> updateDocumentExpiry(intent.documentId, intent.expiryDate)

            // Form actions
            is Intent.ValidateBasicInfo -> validateBasicInfo()
            is Intent.SubmitVehicle -> submitVehicle()
            is Intent.Cancel -> sendEffect(Effect.NavigateBack)
            is Intent.ClearError -> updateState { copy(error = null) }
        }
    }

    private fun updateRegistrationNumber(value: String) {
        val upperValue = value.uppercase()
        val error = validateRegistrationNumber(upperValue)
        updateState { copy(registrationNumber = upperValue, registrationNumberError = error) }
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
            value.toInt() < 1990 -> "Year must be 1990 or later"
            value.toInt() > 2026 -> "Year cannot be in the future"
            else -> null
        }
        updateState { copy(year = value, yearError = error) }
    }

    private fun validateRegistrationNumber(value: String): String? {
        // Indian vehicle registration format: SS DD XX YYYY
        // SS = State code (2 letters): MH, DL, KA, TN, UP, GJ, RJ, etc.
        // DD = District code (1-2 digits): 01-99
        // XX = Series (1-4 letters): A, AB, ABC, ABCD (optional in some cases)
        // YYYY = Number (1-4 digits): 1-9999
        // Examples: MH12AB1234, DL1CAB1234, KA01MG1234, TN38X1234, UP80A1234

        val indianRegex = Regex("^[A-Z]{2}[0-9]{1,2}[A-Z]{1,4}[0-9]{1,4}$")

        return when {
            value.isBlank() -> "Registration number is required"
            value.length < 6 -> "Registration number is too short"
            value.length > 13 -> "Registration number is too long"
            !indianRegex.matches(value) ->
                "Invalid Indian format (e.g., MH12AB1234, DL1C1234, KA01MG1234)"
            else -> null
        }
    }

    private fun validateBasicInfo(): Boolean {
        val regError = validateRegistrationNumber(currentState.registrationNumber)
        val makeError = if (currentState.make.isBlank()) "Make is required" else null
        val modelError = if (currentState.model.isBlank()) "Model is required" else null
        val yearError = when {
            currentState.year.isBlank() -> "Year is required"
            currentState.year.toIntOrNull() == null -> "Invalid year"
            else -> null
        }

        updateState {
            copy(
                registrationNumberError = regError,
                makeError = makeError,
                modelError = modelError,
                yearError = yearError
            )
        }

        return regError == null && makeError == null && modelError == null && yearError == null
    }

    private fun nextStep() {
        when (currentState.currentStep) {
            0 -> {
                if (validateBasicInfo()) {
                    updateState { copy(currentStep = 1) }
                } else {
                    sendEffect(Effect.ShowSnackbar("Please fill all required fields correctly"))
                }
            }
            1 -> {
                // Already at last step, submit
                sendIntent(Intent.SubmitVehicle)
            }
        }
    }

    private fun previousStep() {
        if (currentState.currentStep > 0) {
            updateState { copy(currentStep = currentState.currentStep - 1) }
        } else {
            sendEffect(Effect.NavigateBack)
        }
    }

    private suspend fun uploadDocument(
        type: DocumentType,
        fileName: String,
        fileBytes: ByteArray,
        mimeType: String
    ) {
        updateState { copy(uploadingDocument = type, uploadProgress = 0f) }

        withContext(dispatcherProvider.io) {
            try {
                // Simulate upload progress
                for (progress in 1..10) {
                    delay(100)
                    updateState { copy(uploadProgress = progress / 10f) }
                }

                val currentTime = 1734700000000L // Mock timestamp
                val documentId = "doc_${currentTime}_${type.ordinal}"
                val document = VehicleDocument(
                    id = documentId,
                    vehicleId = "", // Will be set on save
                    type = type,
                    name = getDocumentTypeName(type),
                    fileName = fileName,
                    fileSize = fileBytes.size.toLong(),
                    mimeType = mimeType,
                    uploadDate = currentTime,
                    status = DocumentStatus.PENDING
                )

                updateState {
                    copy(
                        documents = documents.filter { it.type != type } + document,
                        uploadingDocument = null,
                        uploadProgress = 0f
                    )
                }

                sendEffect(Effect.ShowSnackbar("${getDocumentTypeName(type)} uploaded successfully"))
            } catch (e: Exception) {
                updateState { copy(uploadingDocument = null, uploadProgress = 0f) }
                sendEffect(Effect.ShowSnackbar("Failed to upload document: ${e.message}"))
            }
        }
    }

    private fun removeDocument(documentId: String) {
        updateState {
            copy(documents = documents.filter { it.id != documentId })
        }
        sendEffect(Effect.ShowSnackbar("Document removed"))
    }

    private fun updateDocumentExpiry(documentId: String, expiryDate: Long) {
        updateState {
            copy(
                documents = documents.map { doc ->
                    if (doc.id == documentId) doc.copy(expiryDate = expiryDate) else doc
                }
            )
        }
    }

    private suspend fun submitVehicle() {
        if (!currentState.isBasicInfoValid) {
            sendEffect(Effect.ShowSnackbar("Please fill all required fields correctly"))
            return
        }

        if (!currentState.hasRequiredDocuments) {
            sendEffect(Effect.ShowSnackbar("Please upload Registration Certificate and Insurance documents"))
            return
        }

        updateState { copy(isSaving = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                // Create Vehicle entity from form state
                val vehicle = Vehicle(
                    id = "", // Will be assigned by backend
                    registrationNumber = currentState.registrationNumber,
                    make = currentState.make,
                    model = currentState.model,
                    year = currentState.year.toIntOrNull() ?: 0,
                    type = currentState.vehicleType,
                    status = VehicleStatus.ACTIVE,
                    fuelLevel = 0,
                    mileage = 0.0
                )

                val result = createVehicleUseCase(vehicle)

                when (result) {
                    is Result.Success -> {
                        updateState { copy(isSaving = false) }
                        sendEffect(Effect.ShowSnackbar("Vehicle registered successfully!"))
                        sendEffect(Effect.VehicleRegistered(result.data.id))
                        sendEffect(Effect.NavigateBack)
                    }
                    is Result.Error -> {
                        updateState {
                            copy(
                                isSaving = false,
                                error = result.message ?: "Failed to register vehicle"
                            )
                        }
                        sendEffect(Effect.ShowSnackbar("Failed to register vehicle: ${result.message}"))
                    }
                    is Result.Loading -> { /* Not applicable for suspend function */ }
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isSaving = false,
                        error = e.message ?: "Failed to register vehicle"
                    )
                }
                sendEffect(Effect.ShowSnackbar("Failed to register vehicle: ${e.message}"))
            }
        }
    }

    private fun getDocumentTypeName(type: DocumentType): String {
        return when (type) {
            DocumentType.REGISTRATION_CERTIFICATE -> "Registration Certificate (RC)"
            DocumentType.INSURANCE -> "Insurance"
            DocumentType.PUC_CERTIFICATE -> "PUC Certificate"
            DocumentType.FITNESS_CERTIFICATE -> "Fitness Certificate"
            DocumentType.ROAD_TAX -> "Road Tax"
            DocumentType.PERMIT -> "Permit"
            DocumentType.DRIVER_LICENSE -> "Driver License"
            DocumentType.OTHER -> "Other Document"
        }
    }
}

