package com.ijs.driver.presentation.create

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
import com.indusjs.datetimepicker.FleetDatePicker
import com.indusjs.datetimepicker.DateTimeUtils
import com.indusjs.uicomponents.components.FleetEmailField
import com.indusjs.uicomponents.components.FleetMobileField
import com.indusjs.uicomponents.components.CaretakerSectionCard
import com.ijs.team.presentation.toCaretakerInfo
import com.ijs.team.presentation.toCaretakerInfoList
import com.ijs.driver.domain.entity.LicenseType
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
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
                FleetMobileField(
                    rawValue = state.mobile,
                    onRawValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateMobile(it)) },
                    label = "Mobile Number *",
                    placeholder = "Enter 10-digit mobile",
                    isError = state.mobileError != null,
                    errorMessage = state.mobileError
                )
            }

            item {
                FleetEmailField(
                    value = state.email,
                    onValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateEmail(it)) },
                    label = "Email (Optional)",
                    placeholder = "e.g., raj.kumar@example.com",
                    isError = state.emailError != null,
                    errorMessage = state.emailError
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
                FleetDatePicker(
                    date = state.licenseExpiry,
                    onDateChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateLicenseExpiry(it)) },
                    label = "License Expiry Date *",
                    minDate = DateTimeUtils.getCurrentDate(),  // License expiry must be in future
                    isError = state.licenseExpiryError != null,
                    errorMessage = state.licenseExpiryError
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
                // Calculate max DOB date (18 years ago) for age validation
                val maxDobDate = remember { DateTimeUtils.getDateFromToday(-18 * 365) }

                FleetDatePicker(
                    date = state.dateOfBirth,
                    onDateChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateDateOfBirth(it)) },
                    label = "Date of Birth",
                    maxDate = maxDobDate,  // Driver must be at least 18 years old
                    initialDisplayDate = maxDobDate  // Show 18 years ago when picker opens
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
                FleetMobileField(
                    rawValue = state.emergencyContact,
                    onRawValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateEmergencyContact(it)) },
                    label = "Emergency Contact",
                    placeholder = "Enter 10-digit mobile",
                    leadingEmoji = "🆘"
                )
            }

            item {
                FleetDatePicker(
                    date = state.joiningDate,
                    onDateChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateJoiningDate(it)) },
                    label = "Joining Date"
                )
            }

            // Caretaker Assignment Section
            item {
                CaretakerSectionCard(
                    selectedCaretaker = state.selectedCaretaker?.toCaretakerInfo(),
                    caretakers = state.caretakers.toCaretakerInfoList(),
                    onCaretakerSelected = { caretakerInfo ->
                        val dto = state.caretakers.find { it.id.toString() == caretakerInfo?.id }
                        viewModel.sendIntent(CreateDriverContract.Intent.SelectCaretaker(dto))
                    },
                    onRefresh = { viewModel.sendIntent(CreateDriverContract.Intent.RefreshCaretakers) },
                    isLoading = state.isLoadingCaretakers
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
