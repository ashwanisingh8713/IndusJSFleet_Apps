package com.ijs.customer.presentation.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
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
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val breakpoint = rememberFleetBreakpoint()
        // Compact = full-width phone form; Medium/Expanded = centered, constrained
        // column so the form doesn't stretch edge-to-edge on tablet / web.
        val formWidthModifier = when (breakpoint) {
            FleetBreakpoint.Compact -> Modifier.fillMaxWidth()
            else -> Modifier.widthIn(max = FleetTokens.Width.MaxContent)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(FleetTokens.Spacing.M),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = formWidthModifier,
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
            ) {
                // Error message
                state.error?.let { ErrorCard(it.resolve()) }

                // Company Details Section
                CompanyDetailsSection(state, onIntent)

                // Contact Section
                ContactDetailsSection(state, onIntent)

                // Business Details Section
                BusinessDetailsSection(state, onIntent)

                // Status Toggle
                StatusToggleSection(state, onIntent)

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXXL))
            }
        }
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
                onClick = onCancel,
                variant = ButtonVariant.SECONDARY,
                modifier = Modifier.weight(1f)
            )

            FleetButton(
                text = if (isSaving) {
                    stringResource(Res.string.action_updating)
                } else {
                    stringResource(Res.string.customer_update)
                },
                onClick = onSave,
                enabled = canSave,
                isLoading = isSaving,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CompanyDetailsSection(state: State, onIntent: (Intent) -> Unit) {
    FleetTitledSectionCard(
        title = stringResource(Res.string.customer_section_company_card)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            // Company / org name: required non-blank (NOT name-rule — company names
            // legitimately contain digits/&). Validated in the ViewModel.
            FleetInputField(
                value = state.companyName,
                onValueChange = { onIntent(Intent.UpdateCompanyName(it)) },
                label = stringResource(Res.string.customer_label_company_name),
                isError = state.companyNameError != null,
                errorMessage = state.companyNameError?.resolve()
            )

            FleetInputField(
                value = state.personName,
                onValueChange = { onIntent(Intent.UpdatePersonName(it)) },
                label = stringResource(Res.string.customer_label_contact_person),
                isError = state.personNameError != null,
                errorMessage = state.personNameError?.resolve()
            )
        }
    }
}

@Composable
private fun ContactDetailsSection(state: State, onIntent: (Intent) -> Unit) {
    FleetTitledSectionCard(
        title = stringResource(Res.string.customer_section_contact_details)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            FleetInputField(
                value = state.primaryContact,
                onValueChange = { onIntent(Intent.UpdatePrimaryContact(filterDigitsOnly(it, 10))) },
                fieldType = FieldType.PHONE,
                label = stringResource(Res.string.customer_label_primary_mobile),
                isError = state.primaryContactError != null,
                errorMessage = state.primaryContactError?.resolve()
            )

            FleetInputField(
                value = state.secondaryContact,
                onValueChange = { onIntent(Intent.UpdateSecondaryContact(filterDigitsOnly(it, 10))) },
                fieldType = FieldType.PHONE,
                label = stringResource(Res.string.customer_label_secondary_contact),
                isError = state.secondaryContactError != null,
                errorMessage = state.secondaryContactError?.resolve()
            )

            FleetInputField(
                value = state.email,
                onValueChange = { onIntent(Intent.UpdateEmail(it)) },
                fieldType = FieldType.EMAIL,
                label = stringResource(Res.string.customer_label_email),
                isError = state.emailError != null,
                errorMessage = state.emailError?.resolve()
            )
        }
    }
}

@Composable
private fun BusinessDetailsSection(state: State, onIntent: (Intent) -> Unit) {
    FleetTitledSectionCard(
        title = stringResource(Res.string.customer_section_business)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            FleetInputField(
                value = state.gstNumber,
                onValueChange = { onIntent(Intent.UpdateGstNumber(it)) },
                label = stringResource(Res.string.customer_label_gst),
                placeholder = stringResource(Res.string.customer_placeholder_gst),
                isError = state.gstNumberError != null,
                errorMessage = state.gstNumberError?.resolve()
            )

            FleetInputField(
                value = state.companyAddress,
                onValueChange = { onIntent(Intent.UpdateCompanyAddress(it)) },
                fieldType = FieldType.ADDRESS,
                label = stringResource(Res.string.customer_label_address)
            )

            FleetInputField(
                value = state.notes,
                onValueChange = { onIntent(Intent.UpdateNotes(it)) },
                fieldType = FieldType.NOTES,
                label = stringResource(Res.string.customer_label_notes)
            )
        }
    }
}

@Composable
private fun StatusToggleSection(state: State, onIntent: (Intent) -> Unit) {
    FleetSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
