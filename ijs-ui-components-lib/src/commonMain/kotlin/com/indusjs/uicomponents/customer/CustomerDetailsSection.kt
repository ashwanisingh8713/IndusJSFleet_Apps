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
                    text = "👤 Customer Details",
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
                    Text("🔄 Change Customer")
                }
                TextButton(onClick = onAddNewClick) {
                    Text("+ Add New Customer")
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
                Text(text = "Select Customer", fontWeight = FontWeight.Medium)
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "👤", style = MaterialTheme.typography.displaySmall)
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
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("➕ Add New Customer")
        }
    }
}

