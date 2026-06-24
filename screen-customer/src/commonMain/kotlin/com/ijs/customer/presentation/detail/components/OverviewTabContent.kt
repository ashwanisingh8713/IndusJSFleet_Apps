package com.ijs.customer.presentation.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.components.ClickablePhoneRow
import com.indusjs.uicomponents.components.FleetInlineErrorBanner
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.customer.domain.entity.Customer
import com.ijs.customer.presentation.detail.CustomerDetailContract.Intent
import com.ijs.customer.presentation.detail.CustomerDetailContract.State
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Overview Tab Content - shows customer info, contacts, and business details.
 */
@Composable
fun OverviewTabContent(
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
        state.error?.let { error ->
            ErrorCard(error.resolve())
        }

        CustomerHeroCard(state.customer!!, state.isSaving)
        CustomerContactCard(state.customer!!)
        CustomerBusinessCard(state.customer!!)
    }
}

@Composable
internal fun ErrorCard(error: String) {
    FleetInlineErrorBanner(message = error)
}

@Composable
private fun CustomerHeroCard(
    customer: Customer,
    isSaving: Boolean
) {
    FleetSectionCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        border = null,
        elevation = FleetTokens.Elevation.None
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = customer.companyName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                CustomerStatusChip(customer.isActive, isSaving)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👤", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = customer.personName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
internal fun CustomerStatusChip(
    isActive: Boolean,
    isSaving: Boolean
) {
    val bgColor = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val textColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        if (isSaving) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
        } else {
            Text(
                text = if (isActive) {
                    stringResource(Res.string.customer_status_bullet_active)
                } else {
                    stringResource(Res.string.customer_status_bullet_inactive)
                },
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CustomerContactCard(customer: Customer) {
    FleetTitledSectionCard(
        title = stringResource(Res.string.customer_section_contact_details)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ClickablePhoneRow(
                label = stringResource(Res.string.customer_phone_label_primary),
                phoneNumber = customer.primaryContact
            )

            customer.secondaryContact?.takeIf { it.isNotBlank() }?.let { secondary ->
                ClickablePhoneRow(
                    label = stringResource(Res.string.customer_phone_label_secondary),
                    phoneNumber = secondary
                )
            }

            customer.email?.takeIf { it.isNotBlank() }?.let { email ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📧", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerBusinessCard(customer: Customer) {
    FleetTitledSectionCard(
        title = stringResource(Res.string.customer_section_business)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            customer.gstNumber?.takeIf { it.isNotBlank() }?.let { gst ->
                DetailRow(stringResource(Res.string.customer_label_gst), gst)
            }

            customer.companyAddress?.takeIf { it.isNotBlank() }?.let { address ->
                DetailRow(stringResource(Res.string.customer_label_address), address)
            }

            customer.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                DetailRow(stringResource(Res.string.customer_label_notes), notes)
            }

            if (customer.gstNumber.isNullOrBlank() && customer.companyAddress.isNullOrBlank() && customer.notes.isNullOrBlank()) {
                Text(
                    text = stringResource(Res.string.customer_no_extra_business),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

