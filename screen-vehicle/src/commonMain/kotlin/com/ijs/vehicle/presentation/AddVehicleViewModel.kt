package com.ijs.vehicle.presentation

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.error.result.Result
import com.indusjs.uicomponents.components.UiText
import com.ijs.team.data.model.TeamMemberDto
import com.ijs.vehicle.domain.entity.DocumentStatus
import com.ijs.vehicle.domain.entity.DocumentType
import com.ijs.vehicle.domain.entity.Vehicle
import com.ijs.vehicle.domain.entity.VehicleDocument
import com.ijs.vehicle.domain.entity.VehicleStatus
import com.ijs.team.domain.repository.TeamRepository
import com.ijs.vehicle.domain.usecase.CreateVehicleWithDocumentsUseCase
import com.ijs.vehicle.presentation.AddVehicleContract.Effect
import com.ijs.vehicle.presentation.AddVehicleContract.Intent
import com.ijs.vehicle.presentation.AddVehicleContract.State
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.document_ready_for_upload
import indusjsfleet.ijs_ui_components_lib.generated.resources.document_removed
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_add_document
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_fill_required_fields
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_make_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_mobile_invalid_inline
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_model_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_owner_name_invalid
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_reg_number_format
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_reg_number_long
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_reg_number_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_reg_number_short
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_register_vehicle
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_year_future
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_year_invalid
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_year_min_1990
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_year_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.success_vehicle_registered
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * ViewModel for the Add/Register Vehicle screen implementing MVI pattern.
 *
 * Dependencies are provided via DefaultViewModelProvider.
 */
@Inject
class AddVehicleViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val createVehicleWithDocumentsUseCase: CreateVehicleWithDocumentsUseCase,
    private val teamRepository: TeamRepository
) : MviViewModel<State, Intent, Effect>(State()) {

    companion object {
        private var documentIdCounter = 0L
    }

    init {
        // Load caretakers (supervisors + managers) on init
        sendIntent(Intent.LoadCaretakers)
    }

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
            is Intent.UpdateOwnerName -> updateOwnerName(intent.value)
            is Intent.UpdateOwnerContact -> updateOwnerContact(intent.value)

            // Navigation
            is Intent.NextStep -> nextStep()
            is Intent.PreviousStep -> previousStep()
            is Intent.GoToStep -> updateState { copy(currentStep = intent.step) }

            // Document management
            is Intent.SelectDocument -> sendEffect(Effect.ShowDocumentPicker(intent.type))
            is Intent.UploadDocument -> uploadDocument(intent.type, intent.fileName, intent.fileBytes, intent.mimeType)
            is Intent.RemoveDocument -> removeDocument(intent.documentId)
            is Intent.UpdateDocumentExpiry -> updateDocumentExpiry(intent.documentId, intent.expiryDate)
            is Intent.UpdateDocumentExpiryDate -> updateDocumentExpiryDate(intent.type, intent.rawDigits)

            // Caretaker management
            is Intent.LoadCaretakers -> loadCaretakers()
            is Intent.RefreshCaretakers -> refreshCaretakers()
            is Intent.ToggleCaretakerDropdown -> updateState { copy(showCaretakerDropdown = !showCaretakerDropdown) }
            is Intent.SelectCaretaker -> updateState { copy(selectedCaretaker = intent.caretaker, showCaretakerDropdown = false) }
            is Intent.NavigateToCreateTeamMember -> sendEffect(Effect.NavigateToCreateTeamMember)

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
        val error = if (value.isBlank()) UiText.StringRes(Res.string.error_make_required) else null
        updateState { copy(make = value, makeError = error) }
    }

    private fun updateModel(value: String) {
        val error = if (value.isBlank()) UiText.StringRes(Res.string.error_model_required) else null
        updateState { copy(model = value, modelError = error) }
    }

    private fun updateYear(value: String) {
        val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
        val error: UiText? = when {
            value.isBlank() -> UiText.StringRes(Res.string.error_year_required)
            value.toIntOrNull() == null -> UiText.StringRes(Res.string.error_year_invalid)
            value.toInt() < 1990 -> UiText.StringRes(Res.string.error_year_min_1990)
            value.toInt() > currentYear + 1 -> UiText.StringRes(Res.string.error_year_future)
            else -> null
        }
        updateState { copy(year = value, yearError = error) }
    }

    private fun updateOwnerName(value: String) {
        // Owner name is optional; only flag a format error when something is entered.
        val error: UiText? = if (value.isNotBlank() && !ValidationUtils.isValidName(value)) {
            UiText.StringRes(Res.string.error_owner_name_invalid)
        } else {
            null
        }
        updateState { copy(ownerName = value, ownerNameError = error) }
    }

    private fun updateOwnerContact(value: String) {
        // Owner contact is optional; only flag a format error when something is entered.
        val error: UiText? = if (value.isNotBlank() && !ValidationUtils.isValidMobile(value)) {
            UiText.StringRes(Res.string.error_mobile_invalid_inline)
        } else {
            null
        }
        updateState { copy(ownerContact = value, ownerContactError = error) }
    }

    private fun validateRegistrationNumber(value: String): UiText? {
        // Indian vehicle registration format: SS DD XX YYYY
        // SS = State code (2 letters): MH, DL, KA, TN, UP, GJ, RJ, etc.
        // DD = District code (1-2 digits): 01-99
        // XX = Series (1-4 letters): A, AB, ABC, ABCD (optional in some cases)
        // YYYY = Number (1-4 digits): 1-9999
        // Examples: MH12AB1234, DL1CAB1234, KA01MG1234, TN38X1234, UP80A1234

        val indianRegex = Regex("^[A-Z]{2}[0-9]{1,2}[A-Z]{1,4}[0-9]{1,4}$")

        return when {
            value.isBlank() -> UiText.StringRes(Res.string.error_reg_number_required)
            value.length < 6 -> UiText.StringRes(Res.string.error_reg_number_short)
            value.length > 13 -> UiText.StringRes(Res.string.error_reg_number_long)
            !indianRegex.matches(value) ->
                UiText.StringRes(Res.string.error_reg_number_format)
            else -> null
        }
    }

    private fun validateBasicInfo(): Boolean {
        val regError = validateRegistrationNumber(currentState.registrationNumber)
        val makeError: UiText? = if (currentState.make.isBlank()) UiText.StringRes(Res.string.error_make_required) else null
        val modelError: UiText? = if (currentState.model.isBlank()) UiText.StringRes(Res.string.error_model_required) else null
        val yearError: UiText? = when {
            currentState.year.isBlank() -> UiText.StringRes(Res.string.error_year_required)
            currentState.year.toIntOrNull() == null -> UiText.StringRes(Res.string.error_year_invalid)
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
                    sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.error_fill_required_fields)))
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
                // Simulate upload progress (actual upload happens on submit)
                for (progress in 1..10) {
                    delay(50)
                    updateState { copy(uploadProgress = progress / 10f) }
                }

                // Generate unique ID for the document
                val currentTime = documentIdCounter++
                val documentId = "doc_${currentTime}_${type.ordinal}"

                // Get expiry date from documentExpiryDates if available
                val expiryDateRaw = currentState.documentExpiryDates[type]
                val expiryTimestamp = expiryDateRaw?.let { parseDateToTimestamp(it) }

                val document = VehicleDocument(
                    id = documentId,
                    vehicleId = "", // Will be set on save
                    type = type,
                    name = getDocumentTypeName(type),
                    fileName = fileName,
                    fileSize = fileBytes.size.toLong(),
                    mimeType = mimeType,
                    uploadDate = 0L, // Will be set by server
                    expiryDate = expiryTimestamp,
                    status = DocumentStatus.PENDING,
                    fileBytes = fileBytes // Store bytes for upload
                )

                updateState {
                    copy(
                        documents = documents.filter { it.type != type } + document,
                        uploadingDocument = null,
                        uploadProgress = 0f
                    )
                }

                sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.document_ready_for_upload, args = listOf(getDocumentTypeName(type)))))
            } catch (e: Exception) {
                updateState { copy(uploadingDocument = null, uploadProgress = 0f) }
                sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.error_add_document, args = listOf(e.message.toString()))))
            }
        }
    }

    private fun removeDocument(documentId: String) {
        updateState {
            copy(documents = documents.filter { it.id != documentId })
        }
        sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.document_removed)))
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

    private fun updateDocumentExpiryDate(type: DocumentType, rawDigits: String) {
        updateState {
            copy(
                documentExpiryDates = documentExpiryDates + (type to rawDigits)
            )
        }
    }

    /**
     * Parse date string (DDMMYYYY raw digits) to timestamp.
     * Returns null if date is invalid.
     */
    private fun parseDateToTimestamp(rawDigits: String): Long? {
        if (rawDigits.length != 8) return null
        return try {
            val day = rawDigits.substring(0, 2).toIntOrNull() ?: return null
            val month = rawDigits.substring(2, 4).toIntOrNull() ?: return null
            val year = rawDigits.substring(4, 8).toIntOrNull() ?: return null

            if (day < 1 || day > 31 || month < 1 || month > 12 || year < 2000) return null

            // Create LocalDate and convert to epoch millis
            val localDate = kotlinx.datetime.LocalDate(year, month, day)
            localDate.toEpochDays() * 24L * 60L * 60L * 1000L
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun submitVehicle() {
        if (!currentState.isBasicInfoValid) {
            sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.error_fill_required_fields)))
            return
        }

        // Documents are optional - no validation required

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
                    fuelType = currentState.fuelType,
                    color = currentState.color.ifBlank { "white" },
                    capacity = currentState.seatingCapacity.toIntOrNull() ?: 4,
                    fuelLevel = 0,
                    mileage = 0.0
                )

                // Use the with-documents API endpoint
                val result = createVehicleWithDocumentsUseCase(vehicle, currentState.documents)

                when (result) {
                    is Result.Success -> {
                        updateState { copy(isSaving = false) }
                        sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.success_vehicle_registered)))
                        sendEffect(Effect.VehicleRegistered(result.data.id))
                        sendEffect(Effect.NavigateBack)
                    }
                    is Result.Error -> {
                        updateState { copy(isSaving = false) }
                        sendEffect(Effect.ShowSnackbar(
                            result.message?.let { UiText.Raw(it) }
                                ?: UiText.StringRes(Res.string.error_register_vehicle)
                        ))
                    }
                    is Result.Loading -> { /* Not applicable for suspend function */ }
                }
            } catch (e: Exception) {
                updateState { copy(isSaving = false) }
                sendEffect(Effect.ShowSnackbar(
                    e.message?.let { UiText.Raw(it) }
                        ?: UiText.StringRes(Res.string.error_register_vehicle)
                ))
            }
        }
    }

    private fun getDocumentTypeName(type: DocumentType): String {
        return when (type) {
            DocumentType.REGISTRATION_CERTIFICATE -> "Registration Certificate [RC]"
            DocumentType.INSURANCE -> "Insurance [INS]"
            DocumentType.PUC_CERTIFICATE -> "PUC Certificate [PUC]"
            DocumentType.FITNESS_CERTIFICATE -> "Fitness Certificate [FC]"
            DocumentType.ROAD_TAX -> "Road Tax [RT]"
            DocumentType.PERMIT -> "Permit [PERMIT]"
            DocumentType.DRIVER_LICENSE -> "Driver License"
            DocumentType.OTHER -> "Other Document"
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

