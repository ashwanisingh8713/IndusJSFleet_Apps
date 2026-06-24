package com.indusjs.uicomponents.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.model.shared.SelectableCustomer
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Reusable Customer Details Section Card.
 * Refactored to use [SelectableCustomer] instead of Customer entity.
 */
@Composable
fun CustomerDetailsSection(
    selectedCustomer: SelectableCustomer?,
    customerName: String = "",
    customerContact: String = "",
    isRefreshing: Boolean = false,
    hasCustomers: Boolean = true,
    validationError: String? = null,
    // When true, the section title shows a required "*" (e.g. create-trip, where
    // the backend requires customer_id).
    isRequired: Boolean = false,
    onSelectClick: () -> Unit,
    onClearClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onAddNewClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👤 ${stringResource(Res.string.customer_section_details)}${if (isRequired) " *" else ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = onRefreshClick,
                    enabled = !isRefreshing,
                    modifier = Modifier.size(36.dp)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = stringResource(Res.string.customer_refresh),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                thickness = 1.dp
            )

            CustomerDetailsSectionContent(
                selectedCustomer = selectedCustomer,
                customerName = customerName,
                customerContact = customerContact,
                hasCustomers = hasCustomers,
                validationError = validationError,
                onSelectClick = onSelectClick,
                onClearClick = onClearClick,
                onAddNewClick = onAddNewClick
            )
        }
    }
}

@Composable
private fun CustomerDetailsSectionContent(
    selectedCustomer: SelectableCustomer?,
    customerName: String,
    customerContact: String,
    hasCustomers: Boolean,
    validationError: String?,
    onSelectClick: () -> Unit,
    onClearClick: () -> Unit,
    onAddNewClick: () -> Unit
) {
    val hasSelection = selectedCustomer != null || customerName.isNotBlank()

    when {
        !hasCustomers && !hasSelection -> {
            EmptyCustomerState(onAddNewClick = onAddNewClick)
        }

        hasSelection -> {
            SelectedCustomerCard(
                customer = selectedCustomer,
                customerName = customerName,
                customerContact = customerContact,
                onClear = onClearClick
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onSelectClick) {
                    Text("🔄 ${stringResource(Res.string.customer_change)}", maxLines = 1)
                }
                TextButton(onClick = onAddNewClick) {
                    Text("+ ${stringResource(Res.string.customer_add_new)}", maxLines = 1)
                }
            }
        }

        else -> {
            OutlinedButton(
                onClick = onSelectClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("👤", modifier = Modifier.padding(end = 8.dp))
                Text(text = stringResource(Res.string.customer_select), fontWeight = FontWeight.Medium)
            }

            if (validationError != null) {
                Text(
                    text = validationError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onAddNewClick) {
                    Text("+ ${stringResource(Res.string.customer_add_new)}", maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun EmptyCustomerState(
    onAddNewClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "👤", style = MaterialTheme.typography.displaySmall)
        Text(
            text = stringResource(Res.string.customer_no_customers),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(Res.string.customer_add_trip_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onAddNewClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("➕ ${stringResource(Res.string.customer_add_new_button)}")
        }
    }
}

