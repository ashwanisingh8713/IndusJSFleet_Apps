package com.indusjs.fleet.feature.vehicles.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.feature.vehicles.domain.entity.DocumentType
import com.indusjs.fleet.feature.vehicles.domain.entity.VehicleDocument
import com.indusjs.fleet.feature.vehicles.domain.entity.VehicleType

/**
 * Add/Register Vehicle Screen with document upload functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    viewModel: AddVehicleViewModel,
    onNavigateBack: () -> Unit,
    onVehicleRegistered: (String) -> Unit = {},
    onRequestFilePicker: (DocumentType, (String, ByteArray, String) -> Unit) -> Unit = { _, _ -> }
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                    onRequestFilePicker(effect.type) { fileName, bytes, mimeType ->
                        viewModel.sendIntent(
                            AddVehicleContract.Intent.UploadDocument(
                                effect.type, fileName, bytes, mimeType
                            )
                        )
                    }
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register Vehicle") },
                navigationIcon = {
                    TextButton(onClick = { viewModel.sendIntent(AddVehicleContract.Intent.Cancel) }) {
                        Text("← Back", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
                steps = listOf("Basic Info", "Documents"),
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
                        Text("Registering Vehicle...")
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
            SectionHeader(title = "🚗 Vehicle Information")
        }

        item {
            OutlinedTextField(
                value = state.registrationNumber,
                onValueChange = { onIntent(AddVehicleContract.Intent.UpdateRegistrationNumber(it)) },
                label = { Text("Registration Number *") },
                placeholder = { Text("e.g., MH12AB1234") },//KA051HK0712
                isError = state.registrationNumberError != null,
                supportingText = state.registrationNumberError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.make,
                    onValueChange = { onIntent(AddVehicleContract.Intent.UpdateMake(it)) },
                    label = { Text("Make *") },
                    placeholder = { Text("e.g., Tata") },
                    isError = state.makeError != null,
                    supportingText = state.makeError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.model,
                    onValueChange = { onIntent(AddVehicleContract.Intent.UpdateModel(it)) },
                    label = { Text("Model *") },
                    placeholder = { Text("e.g., Prima") },
                    isError = state.modelError != null,
                    supportingText = state.modelError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.year,
                    onValueChange = { onIntent(AddVehicleContract.Intent.UpdateYear(it)) },
                    label = { Text("Year *") },
                    placeholder = { Text("e.g., 2023") },
                    isError = state.yearError != null,
                    supportingText = state.yearError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.color,
                    onValueChange = { onIntent(AddVehicleContract.Intent.UpdateColor(it)) },
                    label = { Text("Color") },
                    placeholder = { Text("e.g., White") },
                    singleLine = true,
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
            SectionHeader(title = "⚙️ Technical Details")
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.chassisNumber,
                    onValueChange = { onIntent(AddVehicleContract.Intent.UpdateChassisNumber(it)) },
                    label = { Text("Chassis Number") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.engineNumber,
                    onValueChange = { onIntent(AddVehicleContract.Intent.UpdateEngineNumber(it)) },
                    label = { Text("Engine Number") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            OutlinedTextField(
                value = state.seatingCapacity,
                onValueChange = { onIntent(AddVehicleContract.Intent.UpdateSeatingCapacity(it)) },
                label = { Text("Seating Capacity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            SectionHeader(title = "👤 Owner Information")
        }

        item {
            OutlinedTextField(
                value = state.ownerName,
                onValueChange = { onIntent(AddVehicleContract.Intent.UpdateOwnerName(it)) },
                label = { Text("Owner Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = state.ownerContact,
                onValueChange = { onIntent(AddVehicleContract.Intent.UpdateOwnerContact(it)) },
                label = { Text("Owner Contact") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
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
            SectionHeader(title = "📄 Required Documents")
        }

        item {
            Text(
                text = "Please upload the following mandatory documents:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Required documents
        items(
            listOf(
                DocumentType.REGISTRATION_CERTIFICATE,
                DocumentType.INSURANCE
            )
        ) { docType ->
            val uploadedDoc = state.documents.find { it.type == docType }
            DocumentUploadCard(
                documentType = docType,
                uploadedDocument = uploadedDoc,
                isRequired = true,
                isUploading = state.uploadingDocument == docType,
                uploadProgress = if (state.uploadingDocument == docType) state.uploadProgress else 0f,
                onUploadClick = { onIntent(AddVehicleContract.Intent.SelectDocument(docType)) },
                onRemoveClick = { uploadedDoc?.let { onIntent(AddVehicleContract.Intent.RemoveDocument(it.id)) } }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "📁 Optional Documents")
        }

        // Optional documents
        items(
            listOf(
                DocumentType.PUC_CERTIFICATE,
                DocumentType.FITNESS_CERTIFICATE,
                DocumentType.ROAD_TAX,
                DocumentType.PERMIT
            )
        ) { docType ->
            val uploadedDoc = state.documents.find { it.type == docType }
            DocumentUploadCard(
                documentType = docType,
                uploadedDocument = uploadedDoc,
                isRequired = false,
                isUploading = state.uploadingDocument == docType,
                uploadProgress = if (state.uploadingDocument == docType) state.uploadProgress else 0f,
                onUploadClick = { onIntent(AddVehicleContract.Intent.SelectDocument(docType)) },
                onRemoveClick = { uploadedDoc?.let { onIntent(AddVehicleContract.Intent.RemoveDocument(it.id)) } }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.hasRequiredDocuments)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.hasRequiredDocuments) "✅" else "⚠️",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (state.hasRequiredDocuments)
                                "All required documents uploaded"
                            else
                                "Missing required documents",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.documents.size} document(s) uploaded",
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
    onUploadClick: () -> Unit,
    onRemoveClick: () -> Unit
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
                else -> MaterialTheme.colorScheme.outline
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document icon (emoji)
            Box(
                modifier = Modifier
                    .size(48.dp)
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
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Document info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = getDocumentTypeName(documentType),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    if (isRequired) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "*",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (uploadedDocument != null) {
                    Text(
                        text = uploadedDocument.fileName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatFileSize(uploadedDocument.fileSize),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (isUploading) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { uploadProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Uploading... ${(uploadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        text = "Tap to upload",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action button
            if (uploadedDocument != null) {
                TextButton(onClick = onRemoveClick) {
                    Text("✕ Remove", color = MaterialTheme.colorScheme.error)
                }
            } else if (!isUploading) {
                FilledTonalButton(onClick = onUploadClick) {
                    Text("+ Upload")
                }
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
            text = "Vehicle Type *",
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
            text = "Fuel Type",
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
                Text(if (currentStep == 0) "← Cancel" else "← Previous")
            }

            Button(
                onClick = onNext,
                enabled = canProceed && !isSaving,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (currentStep == 1) "Register Vehicle ✓" else "Next →")
            }
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

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

