package com.indusjs.uicomponents.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.indusjs.fleet.core.util.CostTypeUtils
import com.indusjs.fleet.core.util.formatCostAmount
import com.indusjs.fleet.core.util.formatCostTime
import com.indusjs.fleet.core.util.formatDateToHumanReadable
import com.indusjs.fleet.core.util.getCostTypeColorComposable
import com.indusjs.fleet.data.model.costs.TripCostDto
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Reusable UI components for displaying trip and vehicle costs.
 * These components follow Material 3 design and are used across:
 * - TripDetailScreen (Trip Costs section)
 * - VehicleDetailScreen (Costs tab)
 */

/**
 * Prominent total cost header card displaying the total expenses.
 */
@Composable
fun TotalCostHeader(
    totalCost: Double,
    transactionCount: Int,
    categoryCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.total_expenses),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹${formatCostAmount(totalCost)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
            ) {
                val transactionText = if (transactionCount == 1) "transaction" else "transactions"
                val categoryText = if (categoryCount == 1) "category" else "categories"
                Text(
                    text = "$transactionCount $transactionText • $categoryCount $categoryText",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Export to PDF button for cost data.
 */
@Composable
fun ExportPdfButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        Text("📄", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(Res.string.export_pdf), fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Flat cost item row - no expand, clickable to show dialog.
 *
 * Layout:
 * ┌─────────────────────────────────────────────────┐
 * │ ⛽ Diesel (1 entry • 5%)            ₹500.00    │
 * │    📅 10-01-2026 • 🕐 2:30 PM                   │
 * └─────────────────────────────────────────────────┘
 *
 * Uses the new structured cost fields:
 * - displayLabel for the title (uses cost_label with fallbacks)
 * - effectiveCostType for icon/color (uses cost_id if available)
 */
@Composable
fun CostListItem(
    cost: TripCostDto,
    totalCost: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Use effectiveCostType (cost_id) for icon and color lookup
    val effectiveType = cost.effectiveCostType
    val costColor = getCostTypeColorComposable(effectiveType)
    // Use displayLabel which prefers cost_label, then custom_cost_label, then costType
    val displayName = cost.displayLabel
    val typeIcon = CostTypeUtils.getIcon(effectiveType)
    val percentage = if (totalCost > 0) (cost.amount / totalCost * 100).toInt() else 0

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // First row: Title (1 entry - percentage) + Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Icon + Title + metadata
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Colored indicator bar
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(40.dp)
                            .background(costColor, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))

                    // Icon
                    Text(
                        text = typeIcon,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Title with metadata
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " (1 entry • $percentage%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Right: Amount badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = costColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "₹${formatCostAmount(cost.amount)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = costColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Second row: Date & Time
            Row(
                modifier = Modifier.padding(start = 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📅", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatDateToHumanReadable(cost.date, shortMonth = true),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                cost.time?.takeIf { it.isNotBlank() }?.let { timeValue ->
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = "🕐", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = formatCostTime(timeValue),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Cost detail dialog showing full information about a cost entry.
 *
 * Uses the new structured cost fields:
 * - displayLabel for the title (uses cost_label with fallbacks)
 * - effectiveCostType for icon/color (uses cost_id if available)
 * - isFuelCost to check if fuel-specific fields should be shown
 */
@Composable
fun CostDetailDialog(
    cost: TripCostDto,
    onDismiss: () -> Unit
) {
    // Use effectiveCostType (cost_id) for icon and color lookup
    val effectiveType = cost.effectiveCostType
    val costColor = getCostTypeColorComposable(effectiveType)
    // Use displayLabel which prefers cost_label, then custom_cost_label, then costType
    val displayName = cost.displayLabel
    val typeIcon = CostTypeUtils.getIcon(effectiveType)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Icon and Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = costColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(text = typeIcon, style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(Res.string.cost_details),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // Amount - Prominent display
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = costColor.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.label_amount),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${formatCostAmount(cost.amount)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = costColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Details Grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date & Time
                    DetailRow(
                        icon = "📅",
                        label = stringResource(Res.string.label_date),
                        value = formatDateToHumanReadable(cost.date, shortMonth = false)
                    )

                    cost.time?.takeIf { it.isNotBlank() }?.let { time ->
                        DetailRow(
                            icon = "🕐",
                            label = stringResource(Res.string.label_time),
                            value = formatCostTime(time)
                        )
                    }

                    // Notes/Description
                    cost.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                        DetailRow(
                            icon = "📝",
                            label = stringResource(Res.string.label_description),
                            value = notes
                        )
                    }

                    // Fuel specific details - use isFuelCost which checks group_id TC-G-001
                    if (cost.isFuelCost || cost.costType == "fuel") {
                        cost.fuelQuantity?.let { qty ->
                            DetailRow(
                                icon = "⛽",
                                label = "Fuel Quantity",
                                value = "$qty ${stringResource(Res.string.trip_cost_placeholder_fuel_quantity)}"
                            )
                        }
                        cost.fuelRate?.let { rate ->
                            DetailRow(
                                icon = "💰",
                                label = "Fuel Rate",
                                value = "₹${formatCostAmount(rate)}/L"
                            )
                        }
                        cost.kmPerLiter?.let { efficiency ->
                            val efficiencyFormatted = ((efficiency * 10).toLong() / 10.0).toString()
                            DetailRow(
                                icon = "🚗",
                                label = "Fuel Efficiency",
                                value = "$efficiencyFormatted km/L"
                            )
                        }
                        cost.fuelType?.takeIf { it.isNotBlank() }?.let { fuelType ->
                            DetailRow(
                                icon = "🛢️",
                                label = stringResource(Res.string.trip_cost_label_fuel_type),
                                value = fuelType.replaceFirstChar { it.uppercaseChar() }
                            )
                        }
                    }

                    // Added by - show user name and role
                    cost.createdByUser?.let { user ->
                        val addedByText = if (user.roleLabel.isNotBlank()) {
                            "${user.fullName} (${user.roleLabel})"
                        } else {
                            user.fullName
                        }
                        DetailRow(
                            icon = "👤",
                            label = stringResource(Res.string.driver_overview_added_by),
                            value = addedByText
                        )
                    }

                    // Created At / Added on timestamp (epoch millis; treat 0 as unset)
                    cost.createdAt?.takeIf { it > 0L }?.let { createdAt ->
                        DetailRow(
                            icon = "➕",
                            label = "Added on",
                            value = formatDateToHumanReadable(createdAt, shortMonth = false)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.close),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Detail row for the dialog.
 */
@Composable
private fun DetailRow(
    icon: String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Fuel details card showing quantity, rate, and efficiency.
 * Kept for backward compatibility.
 */
@Composable
fun FuelDetailsCard(
    fuelQuantity: Double?,
    fuelRate: Double?,
    kmPerLiter: Double?,
    modifier: Modifier = Modifier
) {
    val fuelColor = Color(0xFF4CAF50)

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = fuelColor.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⛽",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(Res.string.fuel_filled),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${fuelQuantity ?: 0} ${stringResource(Res.string.trip_cost_placeholder_fuel_quantity)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
                fuelRate?.let { rate ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(Res.string.label_rate),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${formatCostAmount(rate)}/L",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }

        kmPerLiter?.let { efficiency ->
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                ) {
                    val efficiencyFormatted = ((efficiency * 10).toLong() / 10.0).toString()
                    Text(
                        text = "🚗 Efficiency: $efficiencyFormatted km/L",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
