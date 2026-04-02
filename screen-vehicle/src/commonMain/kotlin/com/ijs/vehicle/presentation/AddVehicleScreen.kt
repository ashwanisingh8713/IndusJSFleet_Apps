package com.ijs.vehicle.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.datetimepicker.FleetDatePicker
import com.indusjs.datetimepicker.DateTimeUtils
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.indusjs.uicomponents.components.CaretakerSectionCard
import com.ijs.team.presentation.toCaretakerInfo
import com.ijs.team.presentation.toCaretakerInfoList
import com.ijs.vehicle.domain.entity.DocumentType
import com.ijs.vehicle.domain.entity.VehicleDocument
import com.ijs.vehicle.domain.entity.VehicleType
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
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
                    snackbarHostState.showSnackbar(effect.message)
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
                            modifier = Modifier.size(24.dp)
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
            // Step indicator
            StepIndicator(
                currentStep = state.currentStep,
                steps = listOf(
                    stringResource(Res.string.vehicle_register_step_basic),
                    stringResource(Res.string.vehicle_register_step_documents)
                ),
                onStepClick = { step ->
                    if (step < state.currentStep || (step == 1 && state.isBasicInfoValid)) {
                        viewModel.sendIntent(AddVehicleContract.Intent.GoToStep(step))
                    }
                }
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
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(Res.string.action_registering))
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    currentStep: Int,
    steps: List<String>,
    onStepClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        steps.forEachIndexed { index, stepName ->
            val isCompleted = index < currentStep
            val isCurrent = index == currentStep

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onStepClick(index) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = when {
                                isCompleted -> MaterialTheme.colorScheme.primary
                                isCurrent -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCompleted) "✓" else "${index + 1}",
                        color = if (isCurrent || isCompleted) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stepName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 8.dp)
                        .align(Alignment.CenterVertically)
                        .background(
                            if (index < currentStep) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                )
            }
        }
    }
}

@Composable
private fun BasicInfoStep(
    state: AddVehicleContract.State,
    onIntent: (AddVehicleContract.Intent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(title = stringResource(Res.string.vehicle_section_info))
        }

        item {
            FleetInputField(
                value = state.registrationNumber,
                onValueChange = { onIntent(AddVehicleContract.Intent.UpdateRegistrationNumber(it)) },
                label = stringResource(Res.string.vehicle_label_registration),
                placeholder = stringResource(Res.string.vehicle_placeholder_registration),
                isError = state.registrationNumberError != null,
                errorMessage = state.registrationNumberError,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FleetInputField(
                    value = state.make,
                    onValueChange = { onIntent(AddVehicleContract.Intent.UpdateMake(it)) },
                    label = stringResource(Res.string.vehicle_label_make),
                    placeholder = stringResource(Res.string.vehicle_placeholder_make),
                    isError = state.makeError != null,
                    errorMessage = state.makeError,
                    modifier = Modifier.weight(1f)
                )

                FleetInputField(
                    value = state.model,
                    onValueChange = { onIntent(AddVehicleContract.Intent.UpdateModel(it)) },
                    label = stringResource(Res.string.vehicle_label_model),
                    placeholder = stringResource(Res.string.vehicle_placeholder_model),
                    isError = state.modelError != null,
                    errorMessage = state.modelError,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FleetInputField(
                    value = state.year,
                    onValueChange = { onIntent(AddVehicleContract.Intent.UpdateYear(it)) },
                    fieldType = FieldType.NUMBER,
                    label = stringResource(Res.string.vehicle_label_year),
                    placeholder = stringResource(Res.string.vehicle_placeholder_year),
                    isError = state.yearError != null,
                    errorMessage = state.yearError,
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
            VehicleTypeSelector(
                selectedType = state.vehicleType,
                onTypeSelected = { onIntent(AddVehicleContract.Intent.UpdateVehicleType(it)) }
            )
        }

        item {
            FuelTypeSelector(
                selectedFuel = state.fuelType,
                fuelTypes = state.fuelTypes,
                onFuelSelected = { onIntent(AddVehicleContract.Intent.UpdateFuelType(it)) }
            )
        }


        item {
            SectionHeader(title = stringResource(Res.string.vehicle_section_owner))
        }

        item {
            FleetInputField(
                value = state.ownerName,
                onValueChange = { onIntent(AddVehicleContract.Intent.UpdateOwnerName(it)) },
                label = stringResource(Res.string.vehicle_label_owner_name),
                modifier = Modifier.fillMaxWidth()
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
                placeholder = stringResource(Res.string.vehicle_placeholder_owner_contact)
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
                isLoading = state.isLoadingCaretakers
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun DocumentsStep(
    state: AddVehicleContract.State,
    onIntent: (AddVehicleContract.Intent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(title = stringResource(Res.string.vehicle_documents_section_optional))
        }

        item {
            Text(
                text = stringResource(Res.string.vehicle_documents_upload_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                onExpiryDateChange = { onIntent(AddVehicleContract.Intent.UpdateDocumentExpiryDate(docType, it)) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📁",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(12.dp))
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

        item { Spacer(modifier = Modifier.height(80.dp)) }
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
    onExpiryDateChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (uploadedDocument != null)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                uploadedDocument != null -> MaterialTheme.colorScheme.primary
                isRequired -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Document icon (emoji) - smaller size
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (uploadedDocument != null)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getDocumentEmoji(documentType),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

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
                                .padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.vehicle_tap_to_upload),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Compact action button
                if (uploadedDocument != null) {
                    IconButton(
                        onClick = onRemoveClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("✕", color = MaterialTheme.colorScheme.error)
                    }
                } else if (!isUploading) {
                    FilledTonalButton(
                        onClick = onUploadClick,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text(stringResource(Res.string.vehicle_docs_upload), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Expiry Date Field - compact inline layout (for documents that require expiry)
            if (documentType != DocumentType.REGISTRATION_CERTIFICATE && documentType != DocumentType.OTHER) {
                Spacer(modifier = Modifier.height(8.dp))
                FleetDatePicker(
                    date = expiryDateRaw,
                    onDateChange = onExpiryDateChange,
                    label = stringResource(Res.string.vehicle_expiry_date),
                    minDate = DateTimeUtils.getCurrentDate()  // Expiry must be in future
                )
            }
        }
    }
}

@Composable
private fun VehicleTypeSelector(
    selectedType: VehicleType,
    onTypeSelected: (VehicleType) -> Unit
) {
    Column {
        Text(
            text = stringResource(Res.string.vehicle_type_required_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VehicleType.entries.forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }
    }
}

@Composable
private fun FuelTypeSelector(
    selectedFuel: String,
    fuelTypes: List<String>,
    onFuelSelected: (String) -> Unit
) {
    Column {
        Text(
            text = stringResource(Res.string.vehicle_edit_fuel_type),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            fuelTypes.forEach { fuel ->
                FilterChip(
                    selected = selectedFuel == fuel,
                    onClick = { onFuelSelected(fuel) },
                    label = { Text(fuel) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
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
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    if (currentStep == 0) stringResource(Res.string.cancel)
                    else stringResource(Res.string.previous)
                )
            }

            Button(
                onClick = onNext,
                enabled = canProceed && !isSaving,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    if (currentStep == 1) stringResource(Res.string.vehicle_register_button)
                    else stringResource(Res.string.next)
                )
            }
        }
    }
}

@Composable
private fun documentTypeDisplayName(type: DocumentType): String = when (type) {
    DocumentType.REGISTRATION_CERTIFICATE -> stringResource(Res.string.vehicle_doc_type_rc)
    DocumentType.INSURANCE -> stringResource(Res.string.vehicle_doc_type_insurance)
    DocumentType.PUC_CERTIFICATE -> stringResource(Res.string.vehicle_doc_type_puc)
    DocumentType.FITNESS_CERTIFICATE -> stringResource(Res.string.vehicle_doc_type_fitness)
    DocumentType.ROAD_TAX -> stringResource(Res.string.vehicle_doc_type_road_tax)
    DocumentType.PERMIT -> stringResource(Res.string.vehicle_doc_type_permit)
    DocumentType.DRIVER_LICENSE -> stringResource(Res.string.vehicle_doc_type_driver_license)
    DocumentType.OTHER -> stringResource(Res.string.vehicle_doc_type_other)
}

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

private fun getDocumentEmoji(type: DocumentType): String {
    return when (type) {
        DocumentType.REGISTRATION_CERTIFICATE -> "📋"
        DocumentType.INSURANCE -> "🛡️"
        DocumentType.PUC_CERTIFICATE -> "🌿"
        DocumentType.FITNESS_CERTIFICATE -> "✅"
        DocumentType.ROAD_TAX -> "💰"
        DocumentType.PERMIT -> "📝"
        DocumentType.DRIVER_LICENSE -> "🪪"
        DocumentType.OTHER -> "📄"
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
        icon = {
            Text(
                text = getDocumentEmoji(documentType),
                style = MaterialTheme.typography.displaySmall
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.vehicle_upload_select_file_prompt),
                    style = MaterialTheme.typography.bodyMedium
                )

                // Supported formats
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.vehicle_upload_supported_formats),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FormatChip("PDF")
                            FormatChip("JPEG")
                            FormatChip("PNG")
                        }
                        Text(
                            text = stringResource(Res.string.vehicle_upload_max_size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
            Button(onClick = onSelectFile) {
                Text(stringResource(Res.string.vehicle_btn_select_file))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}

@Composable
private fun FormatChip(format: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = format,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}


