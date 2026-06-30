package com.ijs.driver.presentation.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDatePicker
import com.indusjs.datetimepicker.DateTimeUtils
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.CaretakerSectionCard
import com.indusjs.uicomponents.components.FleetFilterChip
import com.indusjs.uicomponents.components.DropdownOption
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetDropdown
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetPasswordField
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.team.presentation.toCaretakerInfo
import com.ijs.team.presentation.toCaretakerInfoList
import com.ijs.driver.domain.entity.LicenseType
import com.ijs.driver.presentation.driverLicenseTypeLong
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.DrawableResource
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
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CreateDriverContract.Effect.ShowSnackbar -> {
                    pendingSnackbar = effect.message
                }
                is CreateDriverContract.Effect.NavigateBack -> onNavigateBack()
                is CreateDriverContract.Effect.DriverCreated -> onDriverCreated(effect.driverId)
                is CreateDriverContract.Effect.ShowError -> {
                    pendingSnackbar = effect.message
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = FleetTokens.Elevation.Dialog
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(FleetTokens.Spacing.L),
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                ) {
                    FleetButton(
                        text = stringResource(Res.string.cancel),
                        onClick = { viewModel.sendIntent(CreateDriverContract.Intent.Cancel) },
                        variant = ButtonVariant.SECONDARY,
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f)
                    )

                    FleetButton(
                        text = if (state.isSaving) stringResource(Res.string.action_saving)
                        else stringResource(Res.string.drivers_add),
                        onClick = { viewModel.sendIntent(CreateDriverContract.Intent.SubmitDriver) },
                        variant = ButtonVariant.PRIMARY,
                        enabled = state.canSubmit,
                        isLoading = state.isSaving,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val breakpoint = rememberFleetBreakpoint()
            // Compact = full-width phone form; Medium/Expanded = centered, constrained
            // column so the form doesn't stretch edge-to-edge on tablet / web.
            val formWidthModifier = when (breakpoint) {
                FleetBreakpoint.Compact -> Modifier.fillMaxWidth()
                else -> Modifier.widthIn(max = FleetTokens.Width.MaxContent)
            }
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = FleetTokens.Spacing.ScreenHorizontal),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = formWidthModifier,
                    verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
                ) {
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

                    // ===== Basic Information =====
                    FleetTitledSectionCard(
                        title = stringResource(Res.string.driver_edit_basic_info),
                        subtitle = stringResource(Res.string.driver_create_section_basic_subtitle),
                        iconRes = Res.drawable.ic_profile
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                            ) {
                                FleetInputField(
                                    value = state.firstName,
                                    onValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateFirstName(it)) },
                                    fieldType = FieldType.DEFAULT,
                                    modifier = Modifier.weight(1f),
                                    label = stringResource(Res.string.driver_create_first_name),
                                    placeholder = stringResource(Res.string.driver_create_first_name_placeholder),
                                    isError = state.firstNameError != null,
                                    errorMessage = state.firstNameError?.resolve()
                                )

                                FleetInputField(
                                    value = state.lastName,
                                    onValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateLastName(it)) },
                                    fieldType = FieldType.DEFAULT,
                                    modifier = Modifier.weight(1f),
                                    label = stringResource(Res.string.driver_create_last_name),
                                    placeholder = stringResource(Res.string.driver_create_last_name_placeholder),
                                    isError = state.lastNameError != null,
                                    errorMessage = state.lastNameError?.resolve()
                                )
                            }

                            FleetInputField(
                                value = state.mobile,
                                onValueChange = {
                                    viewModel.sendIntent(
                                        CreateDriverContract.Intent.UpdateMobile(filterDigitsOnly(it, 10))
                                    )
                                },
                                fieldType = FieldType.PHONE,
                                modifier = Modifier.fillMaxWidth(),
                                label = stringResource(Res.string.driver_label_mobile_required),
                                placeholder = stringResource(Res.string.driver_placeholder_mobile_10),
                                isError = state.mobileError != null,
                                errorMessage = state.mobileError?.resolve()
                            )

                            FleetPasswordField(
                                value = state.password,
                                onValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdatePassword(it)) },
                                label = stringResource(Res.string.driver_create_password_label),
                                placeholder = stringResource(Res.string.driver_create_password_placeholder),
                                isError = state.passwordError != null,
                                errorMessage = state.passwordError?.resolve(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            FleetInputField(
                                value = state.email,
                                onValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateEmail(it)) },
                                fieldType = FieldType.EMAIL,
                                modifier = Modifier.fillMaxWidth(),
                                label = stringResource(Res.string.driver_label_email_required),
                                placeholder = stringResource(Res.string.driver_placeholder_email_example),
                                isError = state.emailError != null,
                                errorMessage = state.emailError?.resolve()
                            )
                        }
                    }

                    // ===== License Details =====
                    FleetTitledSectionCard(
                        title = stringResource(Res.string.driver_edit_license_details),
                        subtitle = stringResource(Res.string.driver_create_license_section_subtitle),
                        iconRes = Res.drawable.ic_profile
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
                            FleetInputField(
                                value = state.licenseNumber,
                                onValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateLicenseNumber(it)) },
                                fieldType = FieldType.DEFAULT,
                                modifier = Modifier.fillMaxWidth(),
                                label = stringResource(Res.string.driver_create_license),
                                placeholder = stringResource(Res.string.driver_create_license_placeholder),
                                leadingIcon = { CreateFieldLeadingIcon(Res.drawable.ic_profile) },
                                isError = state.licenseNumberError != null,
                                errorMessage = state.licenseNumberError?.resolve()
                            )

                            LicenseTypeSelector(
                                selectedType = state.licenseType,
                                onTypeSelected = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateLicenseType(it)) }
                            )

                            FleetDatePicker(
                                date = state.licenseExpiry,
                                onDateChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateLicenseExpiry(it)) },
                                label = stringResource(Res.string.driver_label_license_expiry_required),
                                minDate = DateTimeUtils.getCurrentDate(),  // License expiry must be in future
                                isError = state.licenseExpiryError != null,
                                errorMessage = state.licenseExpiryError?.resolve()
                            )
                        }
                    }

                    // ===== Personal Details =====
                    // Calculate max DOB date (18 years ago) for age validation
                    val maxDobDate = remember { DateTimeUtils.getDateFromToday(-18 * 365) }

                    FleetTitledSectionCard(
                        title = stringResource(Res.string.driver_edit_personal_details),
                        subtitle = stringResource(Res.string.driver_create_personal_section_subtitle),
                        iconRes = Res.drawable.ic_edit
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
                            // Date of Birth + Blood Group, side by side (dual).
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                            ) {
                                FleetDatePicker(
                                    date = state.dateOfBirth,
                                    onDateChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateDateOfBirth(it)) },
                                    label = stringResource(Res.string.driver_overview_dob),
                                    maxDate = maxDobDate,  // Driver must be at least 18 years old
                                    initialDisplayDate = maxDobDate,  // Show 18 years ago when picker opens
                                    modifier = Modifier.weight(1f)
                                )

                                FleetDropdown(
                                    label = stringResource(Res.string.driver_edit_blood_group),
                                    options = state.bloodGroupOptions.map { DropdownOption(id = it, label = it) },
                                    selectedOptionId = state.bloodGroup.takeIf { it.isNotBlank() },
                                    onOptionSelected = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateBloodGroup(it)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            FleetInputField(
                                value = state.address,
                                onValueChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateAddress(it)) },
                                fieldType = FieldType.ADDRESS,
                                modifier = Modifier.fillMaxWidth(),
                                label = stringResource(Res.string.driver_create_address),
                                placeholder = stringResource(Res.string.driver_create_address_placeholder),
                                leadingIcon = { CreateFieldLeadingIcon(Res.drawable.ic_map) }
                            )

                            FleetInputField(
                                value = state.emergencyContact,
                                onValueChange = {
                                    viewModel.sendIntent(
                                        CreateDriverContract.Intent.UpdateEmergencyContact(filterDigitsOnly(it, 10))
                                    )
                                },
                                fieldType = FieldType.PHONE,
                                modifier = Modifier.fillMaxWidth(),
                                label = stringResource(Res.string.driver_overview_emergency_contact),
                                placeholder = stringResource(Res.string.driver_placeholder_mobile_10),
                                leadingIcon = { CreateFieldLeadingIcon(Res.drawable.ic_phone) }
                            )

                            FleetDatePicker(
                                date = state.joiningDate,
                                onDateChange = { viewModel.sendIntent(CreateDriverContract.Intent.UpdateJoiningDate(it)) },
                                label = stringResource(Res.string.driver_overview_joining_date)
                            )
                        }
                    }

                    // ===== Caretaker Assignment (already a card) =====
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

                    // Bottom spacing so the last card clears the bottom action bar.
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXXL))
                }
            }

            // Loading overlay
            if (state.isSaving) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(FleetTokens.Radius.XL),
                        shadowElevation = FleetTokens.Elevation.Modal,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(FleetTokens.Spacing.XL),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
                            Text(stringResource(Res.string.driver_create_creating))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateFieldLeadingIcon(iconRes: DrawableResource) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(FleetTokens.IconSize.M)
    )
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
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
        ) {
            LicenseType.entries.forEach { type ->
                FleetFilterChip(
                    selected = selectedType == type,
                    label = driverLicenseTypeLong(type),
                    onClick = { onTypeSelected(type) }
                )
            }
        }
    }
}

