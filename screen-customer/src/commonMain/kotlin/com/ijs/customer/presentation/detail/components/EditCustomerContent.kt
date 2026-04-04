package com.ijs.customer.presentation.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.ijs.customer.presentation.detail.CustomerDetailContract.Intent
import com.ijs.customer.presentation.detail.CustomerDetailContract.State
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Edit mode content for customer detail screen.
 * Shows editable fields for company, contact, business details, and status toggle.
 */
@Composable
fun EditCustomerContent(
    state: State,
    onIntent: (Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Error message
        state.error?.let { ErrorCard(it) }

        // Company Details Section
        CompanyDetailsSection(state, onIntent)

        // Contact Section
        ContactDetailsSection(state, onIntent)

        // Business Details Section
        BusinessDetailsSection(state, onIntent)

        // Status Toggle
        StatusToggleSection(state, onIntent)

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun EditModeBottomBar(
    canSave: Boolean,
    isSaving: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
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
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(Res.string.cancel), fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onSave,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = canSave,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.action_updating))
                } else {
                    Text(stringResource(Res.string.customer_update), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CompanyDetailsSection(state: State, onIntent: (Intent) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(Res.string.customer_section_company_card),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            OutlinedTextField(
                value = state.companyName,
                onValueChange = { onIntent(Intent.UpdateCompanyName(it)) },
                label = { Text(stringResource(Res.string.customer_label_company_name)) },
                isError = state.companyNameError != null,
                supportingText = state.companyNameError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = state.personName,
                onValueChange = { onIntent(Intent.UpdatePersonName(it)) },
                label = { Text(stringResource(Res.string.customer_label_contact_person)) },
                isError = state.personNameError != null,
                supportingText = state.personNameError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

@Composable
private fun ContactDetailsSection(state: State, onIntent: (Intent) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(Res.string.customer_section_contact_details),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            FleetInputField(
                value = state.primaryContact,
                onValueChange = { onIntent(Intent.UpdatePrimaryContact(filterDigitsOnly(it, 10))) },
                fieldType = FieldType.PHONE,
                label = stringResource(Res.string.customer_label_primary_mobile),
                isError = state.primaryContactError != null,
                errorMessage = state.primaryContactError
            )

            FleetInputField(
                value = state.secondaryContact,
                onValueChange = { onIntent(Intent.UpdateSecondaryContact(filterDigitsOnly(it, 10))) },
                fieldType = FieldType.PHONE,
                label = stringResource(Res.string.customer_label_secondary_contact),
                isError = state.secondaryContactError != null,
                errorMessage = state.secondaryContactError
            )

            FleetInputField(
                value = state.email,
                onValueChange = { onIntent(Intent.UpdateEmail(it)) },
                fieldType = FieldType.EMAIL,
                label = stringResource(Res.string.customer_label_email),
                isError = state.emailError != null,
                errorMessage = state.emailError
            )
        }
    }
}

@Composable
private fun BusinessDetailsSection(state: State, onIntent: (Intent) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(Res.string.customer_section_business),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            OutlinedTextField(
                value = state.gstNumber,
                onValueChange = { onIntent(Intent.UpdateGstNumber(it)) },
                label = { Text(stringResource(Res.string.customer_label_gst)) },
                placeholder = { Text(stringResource(Res.string.customer_placeholder_gst)) },
                isError = state.gstNumberError != null,
                supportingText = state.gstNumberError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = state.companyAddress,
                onValueChange = { onIntent(Intent.UpdateCompanyAddress(it)) },
                label = { Text(stringResource(Res.string.customer_label_address)) },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = state.notes,
                onValueChange = { onIntent(Intent.UpdateNotes(it)) },
                label = { Text(stringResource(Res.string.customer_label_notes)) },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

@Composable
private fun StatusToggleSection(state: State, onIntent: (Intent) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.customer_status_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (state.customer?.isActive == true) {
                        stringResource(Res.string.customer_status_subtitle_active)
                    } else {
                        stringResource(Res.string.customer_status_subtitle_inactive)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = state.customer?.isActive ?: true,
                onCheckedChange = { onIntent(Intent.ToggleStatus) }
            )
        }
    }
}

