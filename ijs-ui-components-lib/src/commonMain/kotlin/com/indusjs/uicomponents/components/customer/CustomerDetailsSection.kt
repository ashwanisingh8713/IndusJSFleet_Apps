package com.indusjs.uicomponents.components.customer

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

/**
 * Reusable Customer Details Section Card.
 * Displays customer information with options to select, change, clear, refresh, and add new customer.
 * Used in both Create Trip and Edit Trip screens.
 *
 * Refactored to use [SelectableCustomer] (shared contract from ijs-core-lib)
 * instead of Customer entity, enabling cross-module usage without feature dependencies.
 *
 * @param selectedCustomer The currently selected customer (null if none)
 * @param customerName Fallback customer name (used if selectedCustomer is null but name exists)
 * @param customerContact Fallback customer contact (used if selectedCustomer is null)
 * @param isRefreshing Whether customers are being refreshed from API
 * @param hasCustomers Whether there are any customers available
 * @param validationError Validation error message to display
 * @param onSelectClick Callback when "Select Customer" button is clicked (opens bottom sheet)
 * @param onClearClick Callback when clear selection is clicked
 * @param onRefreshClick Callback when refresh button is clicked
 * @param onAddNewClick Callback when "Add New Customer" is clicked
 */
@Composable
fun CustomerDetailsSection(
    selectedCustomer: SelectableCustomer?,
    customerName: String = "",
    customerContact: String = "",
    isRefreshing: Boolean = false,
    hasCustomers: Boolean = true,
    validationError: String? = null,
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
            // Header with title and refresh button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👤 Customer Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Refresh button
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
                            contentDescription = "Refresh customers",
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

            // Content based on state
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
        // Empty state - no customers exist
        !hasCustomers && !hasSelection -> {
            EmptyCustomerState(onAddNewClick = onAddNewClick)
        }

        // Customer selected - show selected card
        hasSelection -> {
            SelectedCustomerCard(
                companyName = selectedCustomer?.companyName ?: customerName,
                personName = selectedCustomer?.personName ?: "",
                primaryContact = selectedCustomer?.primaryContact ?: customerContact,
                onClear = onClearClick
            )

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onSelectClick) {
                    Text("🔄 Change Customer")
                }
                TextButton(onClick = onAddNewClick) {
                    Text("+ Add New Customer")
                }
            }
        }

        // No selection - show select button
        else -> {
            // Select Customer Button
            OutlinedButton(
                onClick = onSelectClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("👤", modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = "Select Customer",
                    fontWeight = FontWeight.Medium
                )
            }

            // Validation error
            if (validationError != null) {
                Text(
                    text = validationError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            // Add New Customer text button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onAddNewClick) {
                    Text("+ Add New Customer")
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "👤",
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            text = "No customers found",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Add a customer to associate with this trip.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onAddNewClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("➕ Add New Customer")
        }
    }
}

