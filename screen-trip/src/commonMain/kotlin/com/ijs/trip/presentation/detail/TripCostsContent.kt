package com.ijs.trip.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.fleet.data.model.costs.TripCostDto
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.theme.FleetTokens
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
    costsError: String? = null,
    onRetryCosts: (() -> Unit)? = null,
    onExportPdf: (() -> Unit)? = null,
    onAddTripCost: (() -> Unit)? = null
) {
    var selectedCost by remember { mutableStateOf<TripCostDto?>(null) }

    EnhancedSectionCard(
        title = stringResource(Res.string.vehicle_costs_trip_costs),
        iconRes = Res.drawable.ic_cost
    ) {
        when {
            isLoading -> {
                TripCostsLoadingContent()
            }
            costsError != null -> {
                TripCostsErrorContent(message = costsError, onRetry = onRetryCosts)
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
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                ) {
                    if (onAddTripCost != null) {
                        FleetButton(
                            text = "+ " + stringResource(Res.string.action_add_cost),
                            onClick = onAddTripCost,
                            variant = ButtonVariant.SECONDARY,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (onExportPdf != null && costs.isNotEmpty()) {
                        FleetButton(
                            text = stringResource(Res.string.action_export_pdf),
                            onClick = onExportPdf,
                            variant = ButtonVariant.SECONDARY,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_download),
                                    contentDescription = null,
                                    modifier = Modifier.size(FleetTokens.IconSize.S)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

                // Cost Breakdown Header
                Text(
                    text = stringResource(Res.string.trip_detail_cost_breakdown),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = FleetTokens.Spacing.S)
                )

                // Flat list of all costs
                costs.forEachIndexed { index, cost ->
                    com.indusjs.uicomponents.components.CostListItem(
                        cost = cost,
                        totalCost = totalCost,
                        onClick = { selectedCost = cost }
                    )
                    if (index < costs.size - 1) {
                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
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
    EmptyContent(
        iconRes = Res.drawable.ic_cost,
        title = stringResource(Res.string.no_costs_recorded),
        message = stringResource(Res.string.trip_costs_empty_message),
        actionLabel = if (onAddTripCost != null) stringResource(Res.string.trip_costs_add) else null,
        onAction = onAddTripCost,
        fillMaxSize = false
    )
}

/**
 * Error state for Trip Costs section — shown when the costs fetch fails (distinct from empty),
 * with a retry action so a backend error (e.g. 500) is visible rather than hidden as "no costs".
 */
@Composable
private fun TripCostsErrorContent(message: String, onRetry: (() -> Unit)?) {
    ErrorContent(
        error = message,
        screenContext = FleetErrorContext.TRIP_DETAIL,
        onRetry = onRetry,
        fillMaxSize = false
    )
}

/**
 * Loading state content for Trip Costs section.
 */
@Composable
internal fun TripCostsLoadingContent() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(FleetTokens.Spacing.XL),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(FleetTokens.IconSize.L))
    }
}

