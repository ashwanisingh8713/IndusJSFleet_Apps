package com.ijs.driver.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.components.DateInputField
import com.ijs.driver.domain.entity.Driver
import com.ijs.driver.domain.entity.DriverStatus
import com.ijs.driver.domain.entity.LicenseType
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import com.indusjs.uicomponents.components.FleetDateField
import com.indusjs.uicomponents.components.FleetEmailField
import com.indusjs.uicomponents.components.FleetMobileField
import com.indusjs.uicomponents.components.CaretakerSectionCard
import com.indusjs.uicomponents.components.convertDdMmYyyyToIso
import com.indusjs.uicomponents.components.convertIsoToDdMmYyyyRaw
import com.ijs.team.presentation.toCaretakerInfo
import com.ijs.team.presentation.toCaretakerInfoList

@Composable
internal fun EditModeContent(
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

        FleetMobileField(
            rawValue = state.mobile,
            onRawValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateMobile(it)) },
            label = "Mobile Number *",
            placeholder = "Enter 10-digit mobile",
            isError = state.mobileError != null,
            errorMessage = state.mobileError
        )

        FleetEmailField(
            value = state.email,
            onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateEmail(it)) },
            label = "Email",
            isError = state.emailError != null,
            errorMessage = state.emailError
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

        FleetMobileField(
            rawValue = state.emergencyContact,
            onRawValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateEmergencyContact(it)) },
            label = "Emergency Contact",
            placeholder = "Enter 10-digit mobile",
            leadingEmoji = "🆘"
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

