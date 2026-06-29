package com.ijs.vehicle.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import com.indusjs.datetimepicker.FleetDatePicker
import com.indusjs.datetimepicker.DateTimeUtils
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.DropdownOption
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetAccentIconChip
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetDropdown
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetStepIndicator
import com.indusjs.uicomponents.components.StepInfo
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.indusjs.uicomponents.components.CaretakerSectionCard
import com.indusjs.uicomponents.components.FleetSectionHeader
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.team.presentation.toCaretakerInfo
import com.ijs.team.presentation.toCaretakerInfoList
import com.ijs.vehicle.domain.entity.DocumentType
import com.ijs.vehicle.domain.entity.VehicleDocument
import com.ijs.vehicle.domain.entity.VehicleType
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Add/Register Vehicle Screen with document upload functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    viewModel: AddVehicleViewModel,
    onNavigateBack: () -> Unit,
    onVehicleRegistered: (String) -> Unit = {},
    onNavigateToCreateTeamMember: () -> Unit = {},
    onRequestFilePicker: (DocumentType, (String, ByteArray, String) -> Unit) -> Unit = { _, _ -> }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    // State for document upload dialog
    var documentToUpload by remember { mutableStateOf<DocumentType?>(null) }

    // Show document upload dialog
    documentToUpload?.let { docType ->
        DocumentUploadDialog(
            documentType = docType,
            onDismiss = { documentToUpload = null },
            onSelectFile = {
                documentToUpload = null
                onRequestFilePicker(docType) { fileName, bytes, mimeType ->
                    viewModel.sendIntent(
                        AddVehicleContract.Intent.UploadDocument(
                            docType, fileName, bytes, mimeType
                        )
                    )
                }
            }
        )
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddVehicleContract.Effect.ShowSnackbar -> {
                    pendingSnackbar = effect.message
                }
                is AddVehicleContract.Effect.NavigateBack -> onNavigateBack()
                is AddVehicleContract.Effect.VehicleRegistered -> onVehicleRegistered(effect.vehicleId)
                is AddVehicleContract.Effect.ShowDocumentPicker -> {
                    // Show the upload info dialog first
                    documentToUpload = effect.type
                }
                is AddVehicleContract.Effect.NavigateToCreateTeamMember -> {
                    onNavigateToCreateTeamMember()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.vehicle_register_title)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.sendIntent(AddVehicleContract.Intent.Cancel) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomActionBar(
                currentStep = state.currentStep,
                canProceed = if (state.currentStep == 0) state.isBasicInfoValid else state.canSubmit,
                isSaving = state.isSaving,
                onPrevious = { viewModel.sendIntent(AddVehicleContract.Intent.PreviousStep) },
                onNext = { viewModel.sendIntent(AddVehicleContract.Intent.NextStep) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Step indicator (canonical design-system wizard indicator)
            FleetStepIndicator(
                currentStep = state.currentStep,
                steps = listOf(
                    StepInfo(stringResource(Res.string.vehicle_register_step_basic)),
                    StepInfo(stringResource(Res.string.vehicle_register_step_documents))
                )
            )

            // Content based on current step
            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    0 -> BasicInfoStep(
                        state = state,
                        onIntent = { viewModel.sendIntent(it) }
                    )
                    1 -> DocumentsStep(
                        state = state,
                        onIntent = { viewModel.sendIntent(it) }
                    )
                }
            }
        }

        // Loading overlay
        if (state.isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                FleetSectionCard(
                    modifier = Modifier
                        .padding(FleetTokens.Spacing.XXL)
                        .widthIn(max = FleetTokens.Width.MaxContent),
                    elevation = FleetTokens.Elevation.Modal,
                    contentPadding = FleetTokens.Spacing.XL
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
                        Text(
                            text = stringResource(Res.string.action_registering),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BasicInfoStep(
    state: AddVehicleContract.State,
    onIntent: (AddVehicleContract.Intent) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val breakpoint = rememberFleetBreakpoint()
        // Compact = full-width phone form; Medium/Expanded = centered, constrained
        // column so the form doesn't stretch edge-to-edge on tablet / web.
        val formWidthModifier = when (breakpoint) {
            FleetBreakpoint.Compact -> Modifier.fillMaxWidth()
            else -> Modifier.widthIn(max = FleetTokens.Width.MaxContent)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(FleetTokens.Spacing.L),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
        ) {
            item {
                SectionHeader(
                    title = stringResource(Res.string.vehicle_section_info),
                    iconRes = Res.drawable.ic_car
                )
            }

            item {
                FleetInputField(
                    value = state.registrationNumber,
                    onValueChange = { onIntent(AddVehicleContract.Intent.UpdateRegistrationNumber(it)) },
                    label = stringResource(Res.string.vehicle_label_registration),
                    placeholder = stringResource(Res.string.vehicle_placeholder_registration),
                    isError = state.registrationNumberError != null,
                    errorMessage = state.registrationNumberError?.resolve(),
                    modifier = formWidthModifier
                )
            }

            item {
                Row(
                    modifier = formWidthModifier,
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                ) {
                    FleetInputField(
                        value = state.make,
                        onValueChange = { onIntent(AddVehicleContract.Intent.UpdateMake(it)) },
                        label = stringResource(Res.string.vehicle_label_make),
                        placeholder = stringResource(Res.string.vehicle_placeholder_make),
                        isError = state.makeError != null,
                        errorMessage = state.makeError?.resolve(),
                        modifier = Modifier.weight(1f)
                    )

                    FleetInputField(
                        value = state.model,
                        onValueChange = { onIntent(AddVehicleContract.Intent.UpdateModel(it)) },
                        label = stringResource(Res.string.vehicle_label_model),
                        placeholder = stringResource(Res.string.vehicle_placeholder_model),
                        isError = state.modelError != null,
                        errorMessage = state.modelError?.resolve(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = formWidthModifier,
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                ) {
                    FleetInputField(
                        value = state.year,
                        onValueChange = { onIntent(AddVehicleContract.Intent.UpdateYear(it)) },
                        fieldType = FieldType.NUMBER,
                        label = stringResource(Res.string.vehicle_label_year),
                        placeholder = stringResource(Res.string.vehicle_placeholder_year),
                        isError = state.yearError != null,
                        errorMessage = state.yearError?.resolve(),
                        modifier = Modifier.weight(1f)
                    )

                    FleetInputField(
                        value = state.color,
                        onValueChange = { onIntent(AddVehicleContract.Intent.UpdateColor(it)) },
                        label = stringResource(Res.string.vehicle_label_color),
                        placeholder = stringResource(Res.string.vehicle_placeholder_color),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                VehicleTypeFuelRow(
                    state = state,
                    onTypeSelected = { onIntent(AddVehicleContract.Intent.UpdateVehicleType(it)) },
                    onFuelSelected = { onIntent(AddVehicleContract.Intent.UpdateFuelType(it)) },
                    modifier = formWidthModifier
                )
            }

            item {
                SectionHeader(
                    title = stringResource(Res.string.vehicle_section_owner),
                    iconRes = Res.drawable.ic_profile
                )
            }

            item {
                FleetInputField(
                    value = state.ownerName,
                    onValueChange = { onIntent(AddVehicleContract.Intent.UpdateOwnerName(it)) },
                    label = stringResource(Res.string.vehicle_label_owner_name),
                    isError = state.ownerNameError != null,
                    errorMessage = state.ownerNameError?.resolve(),
                    modifier = formWidthModifier
                )
            }

            item {
                FleetInputField(
                    value = state.ownerContact,
                    onValueChange = {
                        onIntent(
                            AddVehicleContract.Intent.UpdateOwnerContact(filterDigitsOnly(it, 10))
                        )
                    },
                    fieldType = FieldType.PHONE,
                    label = stringResource(Res.string.vehicle_label_owner_contact),
                    placeholder = stringResource(Res.string.vehicle_placeholder_owner_contact),
                    isError = state.ownerContactError != null,
                    errorMessage = state.ownerContactError?.resolve(),
                    modifier = formWidthModifier
                )
            }

            // Caretaker Assignment Section
            item {
                CaretakerSectionCard(
                    selectedCaretaker = state.selectedCaretaker?.toCaretakerInfo(),
                    caretakers = state.caretakers.toCaretakerInfoList(),
                    onCaretakerSelected = { caretakerInfo ->
                        val dto = state.caretakers.find { it.id.toString() == caretakerInfo?.id }
                        onIntent(AddVehicleContract.Intent.SelectCaretaker(dto))
                    },
                    onRefresh = { onIntent(AddVehicleContract.Intent.RefreshCaretakers) },
                    onCreateTeamMember = { onIntent(AddVehicleContract.Intent.NavigateToCreateTeamMember) },
                    isLoading = state.isLoadingCaretakers,
                    modifier = formWidthModifier
                )
            }

            item { Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXXL)) }
        }
    }
}

@Composable
private fun DocumentsStep(
    state: AddVehicleContract.State,
    onIntent: (AddVehicleContract.Intent) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val breakpoint = rememberFleetBreakpoint()
        val contentWidthModifier = when (breakpoint) {
            FleetBreakpoint.Compact -> Modifier.fillMaxWidth()
            else -> Modifier.widthIn(max = FleetTokens.Width.MaxContent)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(FleetTokens.Spacing.L),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            item {
                SectionHeader(
                    title = stringResource(Res.string.vehicle_documents_section_optional),
                    iconRes = Res.drawable.ic_folder
                )
            }

            item {
                Text(
                    text = stringResource(Res.string.vehicle_documents_upload_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = contentWidthModifier
                )
            }

            // All documents are now optional
            items(
                listOf(
                    DocumentType.REGISTRATION_CERTIFICATE,
                    DocumentType.INSURANCE,
                    DocumentType.PUC_CERTIFICATE,
                    DocumentType.FITNESS_CERTIFICATE,
                    DocumentType.ROAD_TAX,
                    DocumentType.PERMIT
                )
            ) { docType ->
                val uploadedDoc = state.documents.find { it.type == docType }
                val expiryDateRaw = state.documentExpiryDates[docType] ?: ""
                DocumentUploadCard(
                    documentType = docType,
                    uploadedDocument = uploadedDoc,
                    isRequired = false,
                    isUploading = state.uploadingDocument == docType,
                    uploadProgress = if (state.uploadingDocument == docType) state.uploadProgress else 0f,
                    expiryDateRaw = expiryDateRaw,
                    onUploadClick = { onIntent(AddVehicleContract.Intent.SelectDocument(docType)) },
                    onRemoveClick = { uploadedDoc?.let { onIntent(AddVehicleContract.Intent.RemoveDocument(it.id)) } },
                    onExpiryDateChange = { onIntent(AddVehicleContract.Intent.UpdateDocumentExpiryDate(docType, it)) },
                    modifier = contentWidthModifier
                )
            }

            item {
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

                // Summary card
                FleetSectionCard(
                    modifier = contentWidthModifier,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    border = null,
                    contentPadding = FleetTokens.Spacing.L
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FleetAccentIconChip(
                            accent = MaterialTheme.colorScheme.primary,
                            chipSize = FleetTokens.IconSize.XL,
                            iconSize = FleetTokens.IconSize.M,
                            iconRes = Res.drawable.ic_folder
                        )
                        Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))
                        Column {
                            Text(
                                text = if (state.documents.isNotEmpty()) {
                                    stringResource(Res.string.vehicle_documents_ready)
                                } else {
                                    stringResource(Res.string.vehicle_documents_none_uploaded)
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(Res.string.vehicle_documents_count_uploaded, state.documents.size),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXXL)) }
        }
    }
}

@Composable
private fun DocumentUploadCard(
    documentType: DocumentType,
    uploadedDocument: VehicleDocument?,
    isRequired: Boolean,
    isUploading: Boolean,
    uploadProgress: Float,
    expiryDateRaw: String,
    onUploadClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onExpiryDateChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FleetSectionCard(
        modifier = modifier,
        containerColor = if (uploadedDocument != null)
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = FleetTokens.Height.Divider,
            color = when {
                uploadedDocument != null -> MaterialTheme.colorScheme.primary
                isRequired -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            }
        ),
        contentPadding = FleetTokens.Spacing.M
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // The whole row is the upload affordance while empty (the subtitle reads
                // "Tap to upload"); disabled once a file is attached or mid-upload.
                .clickable(
                    enabled = uploadedDocument == null && !isUploading,
                    onClick = onUploadClick
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document icon (design-system accent chip)
            FleetAccentIconChip(
                accent = if (uploadedDocument != null)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                chipSize = FleetTokens.IconSize.L,
                iconSize = FleetTokens.IconSize.M,
                iconRes = getDocumentIcon(documentType)
            )

            Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))

            // Document info - compact layout
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = documentTypeDisplayName(documentType),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                if (uploadedDocument != null) {
                    Text(
                        text = "${uploadedDocument.fileName} • ${formatFileSizeDisplay(uploadedDocument.fileSize)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                } else if (isUploading) {
                    LinearProgressIndicator(
                        progress = { uploadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = FleetTokens.Spacing.XS)
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.vehicle_tap_to_upload),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Compact, FIXED-size trailing action (44dp). A labelled FleetButton here would, as an
            // unweighted Row child, be measured against the full remaining width → Compact →
            // fillMaxWidth → it eats the row and starves the weighted name column. An icon button is
            // fixed-size and locale-proof, and mirrors the remove button below.
            if (uploadedDocument != null) {
                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.size(FleetTokens.Height.MinTouchTarget)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = stringResource(Res.string.delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(FleetTokens.IconSize.M)
                    )
                }
            } else if (!isUploading) {
                IconButton(
                    onClick = onUploadClick,
                    modifier = Modifier.size(FleetTokens.Height.MinTouchTarget)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_upload),
                        contentDescription = stringResource(Res.string.vehicle_docs_upload),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(FleetTokens.IconSize.M)
                    )
                }
            }
        }

        // Expiry Date Field - compact inline layout (for documents that require expiry)
        if (documentType != DocumentType.REGISTRATION_CERTIFICATE && documentType != DocumentType.OTHER) {
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
            FleetDatePicker(
                date = expiryDateRaw,
                onDateChange = onExpiryDateChange,
                label = stringResource(Res.string.vehicle_expiry_date),
                minDate = DateTimeUtils.getCurrentDate()  // Expiry must be in future
            )
        }
    }
}

/**
 * Vehicle Type + Fuel Type — two side-by-side [FleetDropdown]s ("dual part"), mirroring the
 * Make / Model row.
 *
 * - **Vehicle Type** options are the full [VehicleType] enum (always 6), labelled via the config
 *   (localized, Hindi-aware) with a capitalized-name fallback so it renders even before the config
 *   asset loads.
 * - **Fuel Type** options come from [AddVehicleContract.State.fuelTypes], which the ViewModel keeps
 *   filtered to the selected vehicle type (e.g. Truck ⇒ only Diesel). Labels localized via
 *   `fuelLabelFor`; the value passed back is always the stable fuel string.
 *
 * Both honor `RowScope.weight` (the caller modifier sits on FleetDropdown's `BoxWithConstraints`
 * root), so they split the row evenly and each fills its half-width slot.
 */
@Composable
private fun VehicleTypeFuelRow(
    state: AddVehicleContract.State,
    onTypeSelected: (VehicleType) -> Unit,
    onFuelSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isHindi = Locale.current.language == "hi"
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
    ) {
        FleetDropdown(
            label = stringResource(Res.string.vehicle_type_required_label),
            options = VehicleType.entries.map { type ->
                DropdownOption(id = type, label = state.vehicleTypeLabelFor(type, isHindi))
            },
            selectedOptionId = state.vehicleType,
            onOptionSelected = onTypeSelected,
            modifier = Modifier.weight(1f)
        )

        FleetDropdown(
            label = stringResource(Res.string.vehicle_edit_fuel_type),
            options = state.fuelTypes.map { fuel ->
                DropdownOption(id = fuel, label = state.fuelLabelFor(fuel, isHindi))
            },
            selectedOptionId = state.fuelType.takeIf { it.isNotBlank() },
            onOptionSelected = onFuelSelected,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SectionHeader(title: String, iconRes: DrawableResource? = null) {
    FleetSectionHeader(title = title, iconRes = iconRes)
}

@Composable
private fun BottomActionBar(
    currentStep: Int,
    canProceed: Boolean,
    isSaving: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        shadowElevation = FleetTokens.Elevation.Dialog,
        color = MaterialTheme.colorScheme.surface
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val breakpoint = rememberFleetBreakpoint()
            val barWidthModifier = when (breakpoint) {
                FleetBreakpoint.Compact -> Modifier.fillMaxWidth()
                else -> Modifier.widthIn(max = FleetTokens.Width.MaxContent)
            }
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .then(barWidthModifier)
                    .padding(FleetTokens.Spacing.L),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
            ) {
                FleetButton(
                    text = if (currentStep == 0) stringResource(Res.string.cancel)
                    else stringResource(Res.string.previous),
                    onClick = onPrevious,
                    variant = ButtonVariant.SECONDARY,
                    modifier = Modifier.weight(1f)
                )

                FleetButton(
                    text = if (currentStep == 1) stringResource(Res.string.vehicle_register_button)
                    else stringResource(Res.string.next),
                    onClick = onNext,
                    enabled = canProceed && !isSaving,
                    isLoading = isSaving,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun documentTypeDisplayName(type: DocumentType): String = type.localizedDisplayName()

@Composable
private fun formatFileSizeDisplay(bytes: Long): String = when {
    bytes < 1024 -> stringResource(Res.string.vehicle_file_size_b, bytes.toInt())
    bytes < 1024 * 1024 -> stringResource(Res.string.vehicle_file_size_kb, (bytes / 1024).toInt())
    else -> stringResource(Res.string.vehicle_file_size_mb, (bytes / (1024 * 1024)).toInt())
}

/**
 * Get the API tag for a document type.
 * These tags are automatically assigned by the server.
 */
private fun getDocumentTag(type: DocumentType): String {
    return when (type) {
        DocumentType.REGISTRATION_CERTIFICATE -> "RC"
        DocumentType.INSURANCE -> "INS"
        DocumentType.PUC_CERTIFICATE -> "PUC"
        DocumentType.FITNESS_CERTIFICATE -> "FC"
        DocumentType.ROAD_TAX -> "RT"
        DocumentType.PERMIT -> "PERMIT"
        DocumentType.DRIVER_LICENSE -> "DL"
        DocumentType.OTHER -> "OTHER"
    }
}

private fun getDocumentIcon(type: DocumentType): DrawableResource {
    return when (type) {
        DocumentType.REGISTRATION_CERTIFICATE -> Res.drawable.ic_edit
        DocumentType.INSURANCE -> Res.drawable.ic_folder
        DocumentType.PUC_CERTIFICATE -> Res.drawable.ic_folder
        DocumentType.FITNESS_CERTIFICATE -> Res.drawable.ic_check_circle
        DocumentType.ROAD_TAX -> Res.drawable.ic_cost
        DocumentType.PERMIT -> Res.drawable.ic_edit
        DocumentType.DRIVER_LICENSE -> Res.drawable.ic_profile
        DocumentType.OTHER -> Res.drawable.ic_folder
    }
}

/**
 * Dialog shown before file picker to inform user about supported formats.
 */
@Composable
private fun DocumentUploadDialog(
    documentType: DocumentType,
    onDismiss: () -> Unit,
    onSelectFile: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(FleetTokens.Radius.XL),
        icon = {
            FleetAccentIconChip(
                accent = MaterialTheme.colorScheme.primary,
                chipSize = FleetTokens.IconSize.XL,
                iconSize = FleetTokens.IconSize.Default,
                iconRes = getDocumentIcon(documentType)
            )
        },
        title = {
            Text(
                text = stringResource(Res.string.vehicle_upload_title, documentTypeDisplayName(documentType)),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
            ) {
                Text(
                    text = stringResource(Res.string.vehicle_upload_select_file_prompt),
                    style = MaterialTheme.typography.bodyMedium
                )

                // Supported formats
                FleetSectionCard(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    border = null,
                    contentPadding = FleetTokens.Spacing.M
                ) {
                    Text(
                        text = stringResource(Res.string.vehicle_upload_supported_formats),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                    ) {
                        FormatChip("PDF")
                        FormatChip("JPEG")
                        FormatChip("PNG")
                    }
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                    Text(
                        text = stringResource(Res.string.vehicle_upload_max_size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tag info
                Text(
                    text = stringResource(Res.string.vehicle_upload_tag, getDocumentTag(documentType)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            FleetButton(
                text = stringResource(Res.string.vehicle_btn_select_file),
                onClick = onSelectFile
            )
        },
        dismissButton = {
            FleetButton(
                text = stringResource(Res.string.cancel),
                onClick = onDismiss,
                variant = ButtonVariant.GHOST
            )
        }
    )
}

@Composable
private fun FormatChip(format: String) {
    Surface(
        shape = RoundedCornerShape(FleetTokens.Radius.S),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = format,
            modifier = Modifier.padding(
                horizontal = FleetTokens.Spacing.S,
                vertical = FleetTokens.Spacing.XS
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}


