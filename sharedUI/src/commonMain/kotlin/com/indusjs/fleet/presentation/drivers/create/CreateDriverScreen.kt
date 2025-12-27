package com.indusjs.fleet.presentation.drivers.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.FleetDateField
import com.indusjs.fleet.core.ui.convertIsoToDdMmYyyyRaw
import com.indusjs.fleet.core.ui.convertDdMmYyyyToIso
import com.indusjs.fleet.domain.entity.driver.LicenseType
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

/**
 * Create Driver Screen composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDriverScreen(
    viewModel: CreateDriverViewModel,
    onNavigateBack: () -> Unit = {},
    onDriverCreated: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CreateDriverContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is CreateDriverContract.Effect.NavigateBack -> onNavigateBack()
                is CreateDriverContract.Effect.DriverCreated -> onDriverCreated(effect.driverId)
                is CreateDriverContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Driver") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.sendIntent(CreateDriverContract.Intent.Cancel) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
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
                        onClick = { viewModel.sendIntent(CreateDriverContract.Intent.Cancel) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { viewModel.sendIntent(CreateDriverContract.Intent.SubmitDriver) },
                        modifier = Modifier.weight(1f),
                        enabled = state.canSubmit
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (state.isSaving) "Saving..." else "Add Driver")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Required Fields Section
            item {
                SectionHeader(
                    title = "Basic Information",
                    subtitle = "Required fields",
                    icon = "👤"
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.firstName,
                        onValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateFirstName(it)) },
                        label = { Text("First Name *") },
                        placeholder = { Text("e.g., Raj") },
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
                        onValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateLastName(it)) },
                        label = { Text("Last Name *") },
                        placeholder = { Text("e.g., Kumar") },
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
            }

            item {
                OutlinedTextField(
                    value = state.mobile,
                    onValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateMobile(it)) },
                    label = { Text("Mobile Number *") },
                    placeholder = { Text("e.g., 9876543210") },
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
            }

            item {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateEmail(it)) },
                    label = { Text("Email (Optional)") },
                    placeholder = { Text("e.g., raj.kumar@example.com") },
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
            }

            // License Section
            item {
                SectionHeader(
                    title = "License Details",
                    subtitle = "Driver's license information",
                    icon = "🪪"
                )
            }

            item {
                OutlinedTextField(
                    value = state.licenseNumber,
                    onValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateLicenseNumber(it)) },
                    label = { Text("License Number *") },
                    placeholder = { Text("e.g., DL-1234567890") },
                    leadingIcon = { Text("🪪", modifier = Modifier.padding(start = 12.dp)) },
                    isError = state.licenseNumberError != null,
                    supportingText = state.licenseNumberError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                LicenseTypeSelector(
                    selectedType = state.licenseType,
                    onTypeSelected = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateLicenseType(it)) }
                )
            }

            item {
                FleetDateField(
                    rawValue = convertIsoToDdMmYyyyRaw(state.licenseExpiry),
                    onRawValueChange = {
                        val isoFormatted = if (it.length == 8) convertDdMmYyyyToIso(it) else it
                        viewModel.sendIntent(CreateDriverContract.Intent.UpdateLicenseExpiry(isoFormatted))
                    },
                    label = "License Expiry Date",
                    leadingEmoji = "📅"
                )
            }

            // Personal Details Section
            item {
                SectionHeader(
                    title = "Personal Details",
                    subtitle = "Optional information",
                    icon = "📋"
                )
            }

            item {
                FleetDateField(
                    rawValue = convertIsoToDdMmYyyyRaw(state.dateOfBirth),
                    onRawValueChange = {
                        val isoFormatted = if (it.length == 8) convertDdMmYyyyToIso(it) else it
                        viewModel.sendIntent(CreateDriverContract.Intent.UpdateDateOfBirth(isoFormatted))
                    },
                    label = "Date of Birth",
                    leadingEmoji = "🎂"
                )
            }

            item {
                BloodGroupSelector(
                    selectedBloodGroup = state.bloodGroup,
                    bloodGroups = state.bloodGroupOptions,
                    onBloodGroupSelected = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateBloodGroup(it)) }
                )
            }

            item {
                OutlinedTextField(
                    value = state.address,
                    onValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateAddress(it)) },
                    label = { Text("Address") },
                    placeholder = { Text("e.g., 123 Main St, New Delhi") },
                    leadingIcon = { Text("🏠", modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = state.emergencyContact,
                    onValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateEmergencyContact(it)) },
                    label = { Text("Emergency Contact") },
                    placeholder = { Text("e.g., 9876543211") },
                    leadingIcon = { Text("🆘", modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                FleetDateField(
                    rawValue = convertIsoToDdMmYyyyRaw(state.joiningDate),
                    onRawValueChange = {
                        val isoFormatted = if (it.length == 8) convertDdMmYyyyToIso(it) else it
                        viewModel.sendIntent(CreateDriverContract.Intent.UpdateJoiningDate(isoFormatted))
                    },
                    label = "Joining Date",
                    leadingEmoji = "📆"
                )
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Loading overlay
        if (state.isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
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
                        Text("Creating Driver...")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
    icon: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            subtitle?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LicenseTypeSelector(
    selectedType: LicenseType,
    onTypeSelected: (LicenseType) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(getLicenseTypeLabel(type)) },
                    leadingIcon = if (selectedType == type) {
                        { Text("✓") }
                    } else null
                )
            }
        }
    }
}

private fun getLicenseTypeLabel(type: LicenseType): String {
    return when (type) {
        LicenseType.LMV -> "LMV (Light Motor)"
        LicenseType.HMV -> "HMV (Heavy Motor)"
        LicenseType.MCWG -> "MCWG (Motorcycle)"
        LicenseType.MCWOG -> "MCWOG (Scooter)"
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BloodGroupSelector(
    selectedBloodGroup: String,
    bloodGroups: List<String>,
    onBloodGroupSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
            bloodGroups.forEach { group ->
                FilterChip(
                    selected = selectedBloodGroup == group,
                    onClick = {
                        onBloodGroupSelected(if (selectedBloodGroup == group) "" else group)
                    },
                    label = { Text(group) }
                )
            }
        }
    }
}
