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
import com.indusjs.uicomponents.components.CaretakerSectionCard
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.ijs.team.presentation.toCaretakerInfo
import com.ijs.team.presentation.toCaretakerInfoList
import com.ijs.driver.domain.entity.LicenseType
import com.ijs.driver.presentation.driverLicenseTypeLong
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
                title = { Text(stringResource(Res.string.driver_create_title)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.sendIntent(CreateDriverContract.Intent.Cancel) }) {
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
                        Text(stringResource(Res.string.cancel))
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
                        Text(
                            if (state.isSaving) stringResource(Res.string.action_saving)
                            else stringResource(Res.string.drivers_add)
                        )
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
                    title = stringResource(Res.string.driver_edit_basic_info),
                    subtitle = stringResource(Res.string.driver_create_section_basic_subtitle),
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
                        label = { Text(stringResource(Res.string.driver_create_first_name)) },
                        placeholder = { Text(stringResource(Res.string.driver_create_first_name_placeholder)) },
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
                        label = { Text(stringResource(Res.string.driver_create_last_name)) },
                        placeholder = { Text(stringResource(Res.string.driver_create_last_name_placeholder)) },
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
                FleetInputField(
                    value = state.mobile,
                    onValueChange = {
                        viewModel.sendIntent(
                            CreateDriverContract.Intent.UpdateMobile(filterDigitsOnly(it, 10))
                        )
                    },
                    fieldType = FieldType.PHONE,
                    label = stringResource(Res.string.driver_label_mobile_required),
                    placeholder = stringResource(Res.string.driver_placeholder_mobile_10),
                    isError = state.mobileError != null,
                    errorMessage = state.mobileError
                )
            }

            item {
                FleetInputField(
                    value = state.email,
                    onValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateEmail(it)) },
                    fieldType = FieldType.EMAIL,
                    label = stringResource(Res.string.driver_label_email_optional),
                    placeholder = stringResource(Res.string.driver_placeholder_email_example),
                    isError = state.emailError != null,
                    errorMessage = state.emailError
                )
            }

            // License Section
            item {
                SectionHeader(
                    title = stringResource(Res.string.driver_edit_license_details),
                    subtitle = stringResource(Res.string.driver_create_license_section_subtitle),
                    icon = "🪪"
                )
            }

            item {
                OutlinedTextField(
                    value = state.licenseNumber,
                    onValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateLicenseNumber(it)) },
                    label = { Text(stringResource(Res.string.driver_create_license)) },
                    placeholder = { Text(stringResource(Res.string.driver_create_license_placeholder)) },
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
                    label = stringResource(Res.string.driver_label_license_expiry_required),
                    minDate = DateTimeUtils.getCurrentDate(),  // License expiry must be in future
                    isError = state.licenseExpiryError != null,
                    errorMessage = state.licenseExpiryError
                )
            }

            // Personal Details Section
            item {
                SectionHeader(
                    title = stringResource(Res.string.driver_edit_personal_details),
                    subtitle = stringResource(Res.string.driver_create_personal_section_subtitle),
                    icon = "📋"
                )
            }

            item {
                // Calculate max DOB date (18 years ago) for age validation
                val maxDobDate = remember { DateTimeUtils.getDateFromToday(-18 * 365) }

                FleetDatePicker(
                    date = state.dateOfBirth,
                    onDateChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateDateOfBirth(it)) },
                    label = stringResource(Res.string.driver_overview_dob),
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
                    label = { Text(stringResource(Res.string.driver_create_address)) },
                    placeholder = { Text(stringResource(Res.string.driver_create_address_placeholder)) },
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
                FleetInputField(
                    value = state.emergencyContact,
                    onValueChange = {
                        viewModel.sendIntent(
                            CreateDriverContract.Intent.UpdateEmergencyContact(filterDigitsOnly(it, 10))
                        )
                    },
                    fieldType = FieldType.PHONE,
                    label = stringResource(Res.string.driver_overview_emergency_contact),
                    placeholder = stringResource(Res.string.driver_placeholder_mobile_10),
                    leadingIcon = { Text("🆘", modifier = Modifier.padding(start = 12.dp)) }
                )
            }

            item {
                FleetDatePicker(
                    date = state.joiningDate,
                    onDateChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateJoiningDate(it)) },
                    label = stringResource(Res.string.driver_overview_joining_date)
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
                        Text(stringResource(Res.string.driver_create_creating))
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
            text = stringResource(Res.string.driver_edit_license_type),
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
                    label = { Text(driverLicenseTypeLong(type)) },
                    leadingIcon = if (selectedType == type) {
                        { Text("✓") }
                    } else null
                )
            }
        }
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
            text = stringResource(Res.string.driver_edit_blood_group),
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
