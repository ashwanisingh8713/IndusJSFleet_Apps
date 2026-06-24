package com.ijs.driver.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ijs.driver.domain.entity.LicenseType
import com.ijs.driver.presentation.driverLicenseTypeShort
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.indusjs.uicomponents.components.CaretakerSectionCard
import com.indusjs.uicomponents.components.DateVisualTransformation
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetSectionHeader
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.indusjs.uicomponents.components.convertDdMmYyyyToIso
import com.indusjs.uicomponents.components.convertIsoToDdMmYyyyRaw
import com.ijs.team.presentation.toCaretakerInfo
import com.ijs.team.presentation.toCaretakerInfoList

@Composable
internal fun EditModeContent(
    state: DriverDetailContract.State,
    viewModel: DriverDetailViewModel
) {
    val dateVisualTransformation = remember { DateVisualTransformation() }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Basic Info Section
        EditSectionHeader(icon = "👤", title = stringResource(Res.string.driver_edit_basic_info))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.firstName,
                onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateFirstName(it)) },
                label = { Text(stringResource(Res.string.driver_create_first_name)) },
                isError = state.firstNameError != null,
                supportingText = state.firstNameError?.let { { Text(it.resolve()) } },
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
                label = { Text(stringResource(Res.string.driver_create_last_name)) },
                isError = state.lastNameError != null,
                supportingText = state.lastNameError?.let { { Text(it.resolve()) } },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        FleetInputField(
            value = state.mobile,
            onValueChange = {
                viewModel.sendIntent(
                    DriverDetailContract.Intent.UpdateMobile(filterDigitsOnly(it, 10))
                )
            },
            fieldType = FieldType.PHONE,
            label = stringResource(Res.string.driver_label_mobile_required),
            placeholder = stringResource(Res.string.driver_placeholder_mobile_10),
            isError = state.mobileError != null,
            errorMessage = state.mobileError?.resolve()
        )

        FleetInputField(
            value = state.email,
            onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateEmail(it)) },
            fieldType = FieldType.EMAIL,
            label = stringResource(Res.string.driver_overview_email),
            isError = state.emailError != null,
            errorMessage = state.emailError?.resolve()
        )

        HorizontalDivider()

        // License Section
        EditSectionHeader(icon = "🪪", title = stringResource(Res.string.driver_edit_license_details))

        OutlinedTextField(
            value = state.licenseNumber,
            onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateLicenseNumber(it)) },
            label = { Text(stringResource(Res.string.driver_edit_license_number)) },
            leadingIcon = { Text("🪪", modifier = Modifier.padding(start = 12.dp)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // License Type
        Column {
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
                        selected = state.licenseType == type,
                        onClick = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateLicenseType(type)) },
                        label = { Text(driverLicenseTypeShort(type)) }
                    )
                }
            }
        }

        FleetInputField(
            value = convertIsoToDdMmYyyyRaw(state.licenseExpiry),
            onValueChange = {
                val filtered = filterDigitsOnly(it, 8)
                val isoFormatted = if (filtered.length == 8) convertDdMmYyyyToIso(filtered) else filtered
                viewModel.sendIntent(DriverDetailContract.Intent.UpdateLicenseExpiry(isoFormatted))
            },
            fieldType = FieldType.NUMBER,
            label = stringResource(Res.string.driver_label_license_expiry),
            placeholder = stringResource(Res.string.placeholder_dd_mm_yyyy),
            visualTransformation = dateVisualTransformation,
            leadingIcon = { Text("📅", modifier = Modifier.padding(start = 12.dp)) }
        )

        HorizontalDivider()

        // Personal Details Section
        EditSectionHeader(icon = "📋", title = stringResource(Res.string.driver_edit_personal_details))

        FleetInputField(
            value = convertIsoToDdMmYyyyRaw(state.dateOfBirth),
            onValueChange = {
                val filtered = filterDigitsOnly(it, 8)
                val isoFormatted = if (filtered.length == 8) convertDdMmYyyyToIso(filtered) else filtered
                viewModel.sendIntent(DriverDetailContract.Intent.UpdateDateOfBirth(isoFormatted))
            },
            fieldType = FieldType.NUMBER,
            label = stringResource(Res.string.driver_overview_dob),
            placeholder = stringResource(Res.string.placeholder_dd_mm_yyyy),
            visualTransformation = dateVisualTransformation,
            leadingIcon = { Text("🎂", modifier = Modifier.padding(start = 12.dp)) }
        )

        // Blood Group
        Column {
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
            label = { Text(stringResource(Res.string.driver_edit_address)) },
            leadingIcon = { Text("🏠", modifier = Modifier.padding(start = 12.dp)) },
            singleLine = false,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        FleetInputField(
            value = state.emergencyContact,
            onValueChange = {
                viewModel.sendIntent(
                    DriverDetailContract.Intent.UpdateEmergencyContact(filterDigitsOnly(it, 10))
                )
            },
            fieldType = FieldType.PHONE,
            label = stringResource(Res.string.driver_overview_emergency_contact),
            placeholder = stringResource(Res.string.driver_placeholder_mobile_10),
            leadingIcon = { Text("🆘", modifier = Modifier.padding(start = 12.dp)) }
        )

        FleetInputField(
            value = convertIsoToDdMmYyyyRaw(state.joiningDate),
            onValueChange = {
                val filtered = filterDigitsOnly(it, 8)
                val isoFormatted = if (filtered.length == 8) convertDdMmYyyyToIso(filtered) else filtered
                viewModel.sendIntent(DriverDetailContract.Intent.UpdateJoiningDate(isoFormatted))
            },
            fieldType = FieldType.NUMBER,
            label = stringResource(Res.string.driver_overview_joining_date),
            placeholder = stringResource(Res.string.placeholder_dd_mm_yyyy),
            visualTransformation = dateVisualTransformation,
            leadingIcon = { Text("📅", modifier = Modifier.padding(start = 12.dp)) }
        )

        HorizontalDivider()

        // Caretaker Assignment Section
        CaretakerSectionCard(
            selectedCaretaker = state.selectedCaretaker?.toCaretakerInfo(),
            caretakers = state.caretakers.toCaretakerInfoList(),
            onCaretakerSelected = { caretakerInfo ->
                val dto = state.caretakers.find { it.id.toString() == caretakerInfo?.id }
                viewModel.sendIntent(DriverDetailContract.Intent.SelectCaretaker(dto))
            },
            onRefresh = { viewModel.sendIntent(DriverDetailContract.Intent.RefreshCaretakers) },
            isLoading = state.isLoadingCaretakers
        )
    }
}


@Composable
internal fun EditSectionHeader(
    icon: String,
    title: String
) {
    FleetSectionHeader(title = title, emoji = icon)
}
