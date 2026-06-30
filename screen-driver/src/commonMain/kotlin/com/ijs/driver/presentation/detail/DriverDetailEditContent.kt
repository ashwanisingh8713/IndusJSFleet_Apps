package com.ijs.driver.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ijs.driver.domain.entity.LicenseType
import com.ijs.driver.presentation.driverLicenseTypeShort
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.indusjs.uicomponents.components.CaretakerSectionCard
import com.indusjs.uicomponents.components.DateVisualTransformation
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetFilterChip
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetSectionHeader
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.indusjs.uicomponents.components.convertDdMmYyyyToIso
import com.indusjs.uicomponents.components.convertIsoToDdMmYyyyRaw
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.team.presentation.toCaretakerInfo
import com.ijs.team.presentation.toCaretakerInfoList

@Composable
internal fun EditModeContent(
    state: DriverDetailContract.State,
    viewModel: DriverDetailViewModel
) {
    val dateVisualTransformation = remember { DateVisualTransformation() }
    Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)) {
        // Basic Info Section
        FleetSectionCard {
            EditSectionHeader(iconRes = Res.drawable.ic_profile, title = stringResource(Res.string.driver_edit_basic_info))
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
            ) {
                FleetInputField(
                    value = state.firstName,
                    onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateFirstName(it)) },
                    fieldType = FieldType.DEFAULT,
                    label = stringResource(Res.string.driver_create_first_name),
                    placeholder = stringResource(Res.string.driver_create_first_name_placeholder),
                    isError = state.firstNameError != null,
                    errorMessage = state.firstNameError?.resolve(),
                    modifier = Modifier.weight(1f)
                )

                FleetInputField(
                    value = state.lastName,
                    onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateLastName(it)) },
                    fieldType = FieldType.DEFAULT,
                    label = stringResource(Res.string.driver_create_last_name),
                    placeholder = stringResource(Res.string.driver_create_last_name_placeholder),
                    isError = state.lastNameError != null,
                    errorMessage = state.lastNameError?.resolve(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

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

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

            FleetInputField(
                value = state.email,
                onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateEmail(it)) },
                fieldType = FieldType.EMAIL,
                label = stringResource(Res.string.driver_overview_email),
                isError = state.emailError != null,
                errorMessage = state.emailError?.resolve()
            )
        }

        // License Section
        FleetSectionCard {
            EditSectionHeader(iconRes = Res.drawable.ic_profile, title = stringResource(Res.string.driver_edit_license_details))
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            FleetInputField(
                value = state.licenseNumber,
                onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateLicenseNumber(it)) },
                fieldType = FieldType.DEFAULT,
                label = stringResource(Res.string.driver_edit_license_number),
                leadingIcon = { EditFieldLeadingIcon(Res.drawable.ic_profile) }
            )

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

            // License Type
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
                        selected = state.licenseType == type,
                        label = driverLicenseTypeShort(type),
                        onClick = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateLicenseType(type)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

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
                leadingIcon = { EditFieldLeadingIcon(Res.drawable.ic_calendar) }
            )
        }

        // Personal Details Section
        FleetSectionCard {
            EditSectionHeader(iconRes = Res.drawable.ic_edit, title = stringResource(Res.string.driver_edit_personal_details))
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

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
                leadingIcon = { EditFieldLeadingIcon(Res.drawable.ic_calendar) }
            )

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

            // Blood Group
            Text(
                text = stringResource(Res.string.driver_edit_blood_group),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S),
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
            ) {
                state.bloodGroupOptions.forEach { group ->
                    FleetFilterChip(
                        selected = state.bloodGroup == group,
                        label = group,
                        onClick = {
                            viewModel.sendIntent(DriverDetailContract.Intent.UpdateBloodGroup(
                                if (state.bloodGroup == group) "" else group
                            ))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

            FleetInputField(
                value = state.address,
                onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateAddress(it)) },
                fieldType = FieldType.ADDRESS,
                label = stringResource(Res.string.driver_edit_address),
                leadingIcon = { EditFieldLeadingIcon(Res.drawable.ic_map) }
            )

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

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
                leadingIcon = { EditFieldLeadingIcon(Res.drawable.ic_phone) }
            )

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

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
                leadingIcon = { EditFieldLeadingIcon(Res.drawable.ic_calendar) }
            )
        }

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
    iconRes: DrawableResource,
    title: String
) {
    FleetSectionHeader(title = title, iconRes = iconRes)
}

@Composable
private fun EditFieldLeadingIcon(iconRes: DrawableResource) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(FleetTokens.IconSize.M)
    )
}
