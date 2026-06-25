package com.ijs.driver.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.util.formatCostAmount
import com.indusjs.fleet.core.util.formatDateToHumanReadable
import com.indusjs.uicomponents.theme.FleetTokens
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DriverCostGroupSection(
    groupId: String,
    groupName: String,
    groupIcon: DrawableResource,
    costs: List<com.indusjs.fleet.data.model.driver.DriverCostDto>,
    totalAmount: Double,
    isExpanded: Boolean,
    isDeductionGroup: Boolean,
    onToggle: () -> Unit,
    onTripClick: (Int) -> Unit
) {
    val groupColor = if (isDeductionGroup) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column {
            // Header (always visible, clickable)
            Surface(
                onClick = onToggle,
                color = androidx.compose.ui.graphics.Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Icon
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = groupColor.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    painter = painterResource(groupIcon),
                                    contentDescription = null,
                                    tint = groupColor,
                                    modifier = Modifier.size(FleetTokens.IconSize.M)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = groupName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (costs.size == 1) {
                                    stringResource(Res.string.driver_costs_one_entry, costs.size)
                                } else {
                                    stringResource(Res.string.driver_costs_n_entries, costs.size)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total amount
                        Text(
                            text = "${if (isDeductionGroup) "- " else ""}₹${formatCostAmount(totalAmount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = groupColor
                        )

                        // Expand/Collapse indicator
                        Text(
                            text = if (isExpanded) "▼" else "▶",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Expanded content
            androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))

                    costs.forEach { cost ->
                        EnhancedDriverCostItem(
                            cost = cost,
                            isDeductionGroup = isDeductionGroup,
                            onTripClick = onTripClick
                        )
                    }
                }
            }
        }
    }
}

/**
 * Enhanced driver cost item with trip link support.
 */
@Composable
internal fun EnhancedDriverCostItem(
    cost: com.indusjs.fleet.data.model.driver.DriverCostDto,
    isDeductionGroup: Boolean,
    onTripClick: (Int) -> Unit
) {
    val itemColor = if (isDeductionGroup) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Color bar + Content
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Colored indicator bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(itemColor)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    // Cost Label
                    Text(
                        text = cost.displayLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Date - formatted using FleetDateTime (DD-MMM-YYYY format)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XXS)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_calendar),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(FleetTokens.IconSize.S)
                            )
                            Text(
                                text = formatDateToHumanReadable(cost.date),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Trip link (if available)
                        cost.tripId?.let { tripId ->
                            Surface(
                                onClick = { onTripClick(tripId) },
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    text = stringResource(Res.string.driver_costs_trip_link, tripId),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Description if available
                    val costDescription = cost.description
                    if (!costDescription.isNullOrBlank()) {
                        Text(
                            text = costDescription,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isDeductionGroup) "- " else "+ "}₹${formatCostAmount(cost.amount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = itemColor
                )
            }
        }
    }
}
