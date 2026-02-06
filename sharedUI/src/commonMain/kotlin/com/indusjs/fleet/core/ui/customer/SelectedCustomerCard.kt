package com.indusjs.fleet.core.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.domain.entity.customer.Customer

/**
 * Reusable card showing the selected customer with option to clear.
 *
 * @param customer The selected customer object (optional, for full details)
 * @param customerName Customer/company name to display
 * @param customerContact Customer contact number
 * @param onClear Callback when clear/remove button is clicked
 */
@Composable
fun SelectedCustomerCard(
    customer: Customer? = null,
    customerName: String = "",
    customerContact: String = "",
    onClear: () -> Unit
) {
    val displayName = customer?.companyName ?: customerName.ifBlank { "Customer Selected" }
    val displayPerson = customer?.personName ?: ""
    val displayContact = customer?.primaryContact ?: customerContact

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Avatar
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "👤",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Customer info
                Column {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (displayPerson.isNotBlank()) {
                        Text(
                            text = displayPerson,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (displayContact.isNotBlank()) {
                        Text(
                            text = "📞 $displayContact",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Clear button
            IconButton(onClick = onClear) {
                Text(
                    text = "✕",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

