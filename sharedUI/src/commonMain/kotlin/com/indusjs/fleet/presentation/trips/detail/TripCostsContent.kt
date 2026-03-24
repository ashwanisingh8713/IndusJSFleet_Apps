package com.indusjs.fleet.presentation.trips.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.data.model.costs.TripCostDto

/**
 * Trip Costs Section - Flat list displaying costs with dialog for details.
 *
 * @param costs List of all trip costs
 * @param totalCost Sum of all cost amounts
 * @param costsByType Costs grouped by their cost type
 * @param isLoading Whether costs are being loaded
 * @param onExportPdf Optional callback for PDF export action
 * @param onAddTripCost Optional callback to add a new trip cost
 */
@Composable
internal fun TripCostsSection(
    costs: List<TripCostDto>,
    totalCost: Double,
    costsByType: Map<String, List<TripCostDto>>,
    isLoading: Boolean,
    onExportPdf: (() -> Unit)? = null,
    onAddTripCost: (() -> Unit)? = null
) {
    var selectedCost by remember { mutableStateOf<TripCostDto?>(null) }

    EnhancedSectionCard(
        title = "Trip Costs",
        icon = "💰"
    ) {
        when {
            isLoading -> {
                TripCostsLoadingContent()
            }
            costs.isEmpty() -> {
                TripCostsEmptyContent(onAddTripCost = onAddTripCost)
            }
            else -> {
                // Total Cost Header
                com.indusjs.uicomponents.components.TotalCostHeader(
                    totalCost = totalCost,
                    transactionCount = costs.size,
                    categoryCount = costsByType.size
                )

                // Action buttons row
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onAddTripCost != null) {
                        OutlinedButton(
                            onClick = onAddTripCost,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ Add Cost", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (onExportPdf != null && costs.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onExportPdf,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("📄 Export PDF", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cost Breakdown Header
                Text(
                    text = "Cost Breakdown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Flat list of all costs
                costs.forEachIndexed { index, cost ->
                    com.indusjs.uicomponents.components.CostListItem(
                        cost = cost,
                        totalCost = totalCost,
                        onClick = { selectedCost = cost }
                    )
                    if (index < costs.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Show dialog when a cost is selected
    selectedCost?.let { cost ->
        com.indusjs.uicomponents.components.CostDetailDialog(
            cost = cost,
            onDismiss = { selectedCost = null }
        )
    }
}

/**
 * Empty state content for Trip Costs section.
 */
@Composable
private fun TripCostsEmptyContent(onAddTripCost: (() -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "💸", style = MaterialTheme.typography.displaySmall)
        Text(
            text = "No costs recorded",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Trip expenses will appear here once added",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        if (onAddTripCost != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAddTripCost,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ Add Trip Cost")
            }
        }
    }
}

/**
 * Loading state content for Trip Costs section.
 */
@Composable
internal fun TripCostsLoadingContent() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
    }
}

