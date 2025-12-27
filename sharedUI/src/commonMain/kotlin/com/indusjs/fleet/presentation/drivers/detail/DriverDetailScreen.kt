package com.indusjs.fleet.presentation.drivers.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.FleetDateField
import com.indusjs.fleet.core.ui.FleetStatusBadge
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.core.ui.convertDdMmYyyyToIso
import com.indusjs.fleet.core.ui.convertIsoToDdMmYyyyRaw
import com.indusjs.fleet.domain.entity.driver.Driver
import com.indusjs.fleet.domain.entity.driver.DriverStatus
import com.indusjs.fleet.domain.entity.driver.LicenseType
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

/**
 * Driver Detail Screen composable with Edit functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDetailScreen(
    viewModel: DriverDetailViewModel,
    driverId: String,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    // Load driver on first composition
    LaunchedEffect(driverId) {
        viewModel.sendIntent(DriverDetailContract.Intent.LoadDriver(driverId))
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DriverDetailContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is DriverDetailContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is DriverDetailContract.Effect.NavigateBack -> onNavigateBack()
                is DriverDetailContract.Effect.ShowDeleteConfirmation -> {
                    showDeleteDialog = true
                }
                is DriverDetailContract.Effect.DriverDeleted -> {
                    // Already navigating back
                }
                is DriverDetailContract.Effect.DriverUpdated -> {
                    // Refresh handled in ViewModel
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Driver") },
            text = { Text("Are you sure you want to delete ${state.driver?.fullName}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.sendIntent(DriverDetailContract.Intent.ConfirmDelete)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Status change dialog
    if (showStatusDialog) {
        StatusChangeDialog(
            currentStatus = state.driver?.status ?: DriverStatus.ACTIVE,
            onStatusSelected = { status ->
                showStatusDialog = false
                viewModel.sendIntent(DriverDetailContract.Intent.UpdateStatus(status))
            },
            onDismiss = { showStatusDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Edit Driver" else "Driver Details") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.isEditMode) {
                                viewModel.sendIntent(DriverDetailContract.Intent.ExitEditMode)
                            } else {
                                viewModel.sendIntent(DriverDetailContract.Intent.NavigateBack)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(if (state.isEditMode) Res.drawable.ic_close else Res.drawable.ic_arrow_back),
                            contentDescription = if (state.isEditMode) "Cancel" else "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    if (!state.isEditMode && state.driver != null) {
                        IconButton(onClick = { viewModel.sendIntent(DriverDetailContract.Intent.EnterEditMode) }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_edit),
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
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
            if (state.isEditMode) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.sendIntent(DriverDetailContract.Intent.ExitEditMode) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = { viewModel.sendIntent(DriverDetailContract.Intent.SaveChanges) },
                            modifier = Modifier.weight(1f),
                            enabled = state.canSave
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (state.isSaving) "Saving..." else "Save Changes")
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                LoadingContent(message = "Loading driver details...")
            }
            state.error != null && state.driver == null -> {
                ErrorContent(
                    error = state.error!!,
                    onRetry = { viewModel.sendIntent(DriverDetailContract.Intent.Refresh) }
                )
            }
            state.driver != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.isEditMode) {
                        // Edit Mode
                        item { EditModeContent(state, viewModel) }
                    } else {
                        // View Mode
                        item {
                            DriverHeader(driver = state.driver!!)
                        }

                        item { ContactSection(driver = state.driver!!) }

                        item { LicenseSection(driver = state.driver!!) }

                        item { PersonalSection(driver = state.driver!!) }

                        item { MetadataSection(driver = state.driver!!) }

                        // Delete button
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { viewModel.sendIntent(DriverDetailContract.Intent.DeleteDriver) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("🗑️ Delete Driver")
                            }
                        }
                    }

                    // Bottom spacing
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // Saving overlay
        if (state.isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Saving...")
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverHeader(
    driver: Driver
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${driver.firstName.firstOrNull() ?: ""}${driver.lastName.firstOrNull() ?: ""}".uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name
            Text(
                text = driver.fullName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Mobile
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📱",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = driver.mobile,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Badge
            FleetStatusBadge(
                status = getStatusDisplayName(driver.status),
                color = getStatusColor(driver.status)
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickStatItem(
                    icon = "⭐",
                    value = "${driver.rating}",
                    label = "Rating"
                )

                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                QuickStatItem(
                    icon = "🛣️",
                    value = "${driver.totalTrips}",
                    label = "Trips"
                )

                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                QuickStatItem(
                    icon = "🪪",
                    value = driver.licenseType.name,
                    label = "License"
                )
            }
        }
    }
}

@Composable
private fun QuickStatItem(
    icon: String,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContactSection(driver: Driver) {
    SectionCard(title = "📞 Contact Information") {
        InfoRow(label = "Mobile", value = driver.mobile)
        if (driver.email.isNotBlank()) {
            InfoRow(label = "Email", value = driver.email)
        }
        driver.emergencyContact?.let { InfoRow(label = "Emergency Contact", value = it) }
        driver.address?.let { InfoRow(label = "Address", value = it) }
    }
}

@Composable
private fun LicenseSection(driver: Driver) {
    SectionCard(title = "🪪 License Details") {
        InfoRow(label = "License Number", value = driver.licenseNumber)
        InfoRow(label = "License Type", value = getLicenseTypeLabel(driver.licenseType))
        if (driver.licenseExpiry > 0) {
            InfoRow(label = "Expiry Date", value = formatDate(driver.licenseExpiry))
        }
    }
}

@Composable
private fun PersonalSection(driver: Driver) {
    val hasPersonalInfo = driver.dateOfBirth != null || driver.bloodGroup != null || driver.joiningDate != null

    if (hasPersonalInfo) {
        SectionCard(title = "👤 Personal Details") {
            driver.dateOfBirth?.let { InfoRow(label = "Date of Birth", value = formatDate(it)) }
            driver.bloodGroup?.let { InfoRow(label = "Blood Group", value = it) }
            driver.joiningDate?.let { InfoRow(label = "Joining Date", value = formatDate(it)) }
        }
    }
}

@Composable
private fun MetadataSection(driver: Driver) {
    SectionCard(title = "ℹ️ Additional Info") {
        InfoRow(label = "Driver ID", value = "#${driver.id}")
        driver.owner?.let { owner ->
            val ownerName = "${owner.firstName ?: ""} ${owner.lastName ?: ""}".trim()
            if (ownerName.isNotBlank()) {
                InfoRow(label = "Added by", value = ownerName)
            }
        }
        driver.createdAt?.let { InfoRow(label = "Created on", value = formatDate(it)) }
        driver.updatedAt?.let { InfoRow(label = "Last Updated", value = formatDate(it)) }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditModeContent(
    state: DriverDetailContract.State,
    viewModel: DriverDetailViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Basic Info Section
        EditSectionHeader(icon = "👤", title = "Basic Information")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.firstName,
                onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateFirstName(it)) },
                label = { Text("First Name *") },
                isError = state.firstNameError != null,
                supportingText = state.firstNameError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.lastName,
                onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateLastName(it)) },
                label = { Text("Last Name *") },
                isError = state.lastNameError != null,
                supportingText = state.lastNameError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = state.mobile,
            onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateMobile(it)) },
            label = { Text("Mobile Number *") },
            leadingIcon = { Text("📱", modifier = Modifier.padding(start = 12.dp)) },
            isError = state.mobileError != null,
            supportingText = state.mobileError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.email,
            onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateEmail(it)) },
            label = { Text("Email") },
            leadingIcon = { Text("📧", modifier = Modifier.padding(start = 12.dp)) },
            isError = state.emailError != null,
            supportingText = state.emailError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        // License Section
        EditSectionHeader(icon = "🪪", title = "License Details")

        // License number (read-only)
        OutlinedTextField(
            value = state.licenseNumber,
            onValueChange = { },
            label = { Text("License Number") },
            leadingIcon = { Text("🪪", modifier = Modifier.padding(start = 12.dp)) },
            enabled = false,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // License Type
        Column {
            Text(
                text = "License Type",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LicenseType.entries.forEach { type ->
                    FilterChip(
                        selected = state.licenseType == type,
                        onClick = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateLicenseType(type)) },
                        label = { Text(getLicenseTypeLabel(type)) }
                    )
                }
            }
        }

        FleetDateField(
            rawValue = convertIsoToDdMmYyyyRaw(state.licenseExpiry),
            onRawValueChange = {
                val isoFormatted = if (it.length == 8) convertDdMmYyyyToIso(it) else it
                viewModel.sendIntent(DriverDetailContract.Intent.UpdateLicenseExpiry(isoFormatted))
            },
            label = "License Expiry Date",
            leadingEmoji = "📅"
        )

        HorizontalDivider()

        // Personal Details Section
        EditSectionHeader(icon = "📋", title = "Personal Details")

        FleetDateField(
            rawValue = convertIsoToDdMmYyyyRaw(state.dateOfBirth),
            onRawValueChange = {
                val isoFormatted = if (it.length == 8) convertDdMmYyyyToIso(it) else it
                viewModel.sendIntent(DriverDetailContract.Intent.UpdateDateOfBirth(isoFormatted))
            },
            label = "Date of Birth",
            leadingEmoji = "🎂"
        )

        // Blood Group
        Column {
            Text(
                text = "Blood Group",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.bloodGroupOptions.forEach { group ->
                    FilterChip(
                        selected = state.bloodGroup == group,
                        onClick = {
                            viewModel.sendIntent(DriverDetailContract.Intent.UpdateBloodGroup(
                                if (state.bloodGroup == group) "" else group
                            ))
                        },
                        label = { Text(group) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = state.address,
            onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateAddress(it)) },
            label = { Text("Address") },
            leadingIcon = { Text("🏠", modifier = Modifier.padding(start = 12.dp)) },
            singleLine = false,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.emergencyContact,
            onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateEmergencyContact(it)) },
            label = { Text("Emergency Contact") },
            leadingIcon = { Text("🆘", modifier = Modifier.padding(start = 12.dp)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StatusChangeDialog(
    currentStatus: DriverStatus,
    onStatusSelected: (DriverStatus) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Status") },
        text = {
            Column {
                DriverStatus.entries.forEach { status ->
                    Surface(
                        onClick = { onStatusSelected(status) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (status == currentStatus)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = getStatusDisplayName(status),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            if (status == currentStatus) {
                                Text("✓", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    if (status != DriverStatus.entries.last()) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditSectionHeader(
    icon: String,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Helper functions
@Composable
private fun getStatusColor(status: DriverStatus) = when (status) {
    DriverStatus.ACTIVE -> MaterialTheme.colorScheme.primary
    DriverStatus.INACTIVE -> MaterialTheme.colorScheme.error
    DriverStatus.ON_TRIP -> MaterialTheme.colorScheme.tertiary
    DriverStatus.ON_LEAVE -> MaterialTheme.colorScheme.secondary
    DriverStatus.SUSPENDED -> MaterialTheme.colorScheme.error
}
private fun getStatusDisplayName(status: DriverStatus): String = when (status) {
    DriverStatus.ACTIVE -> "Active"
    DriverStatus.INACTIVE -> "Inactive"
    DriverStatus.ON_TRIP -> "On Trip"
    DriverStatus.ON_LEAVE -> "On Leave"
    DriverStatus.SUSPENDED -> "Suspended"
}

private fun getLicenseTypeLabel(type: LicenseType): String = when (type) {
    LicenseType.LMV -> "LMV"
    LicenseType.HMV -> "HMV"
    LicenseType.MCWG -> "MCWG"
    LicenseType.MCWOG -> "MCWOG"
}

private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0) return "N/A"
    return try {
        val days = timestamp / (24 * 60 * 60 * 1000)
        val years = (days / 365.25).toInt() + 1970
        val remainingDays = (days % 365.25).toInt()
        val months = (remainingDays / 30) + 1
        val dayOfMonth = (remainingDays % 30) + 1
        val monthStr = months.coerceIn(1, 12).toString().padStart(2, '0')
        val dayStr = dayOfMonth.coerceIn(1, 28).toString().padStart(2, '0')
        "$dayStr-$monthStr-$years" // DD-MM-YYYY format
    } catch (_: Exception) {
        "N/A"
    }
}

