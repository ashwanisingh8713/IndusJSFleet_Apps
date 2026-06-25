package com.ijs.reports.presentation.vehicle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetSearchField
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.vehicle.domain.entity.Vehicle
import com.ijs.vehicle.domain.entity.VehicleStatus
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Fixed height for the inline "no vehicles" empty-state box inside the selector list.
 * Bespoke layout dimension, not part of the [FleetTokens] scale.
 */
private val EmptyStateBoxHeight: Dp = 80.dp

@Composable
internal fun VehicleFilterSheetContent(
    vehicles: List<Vehicle>,
    selectedIds: Set<String>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleVehicle: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    val vehicleFallback = stringResource(Res.string.vehicle_pl_vehicle_fallback)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FleetTokens.Spacing.L)
            .padding(bottom = FleetTokens.Spacing.XL)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.vehicle_pl_filter_vehicles),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(Res.string.vehicle_pl_selected_count, selectedIds.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

        // Search
        FleetSearchField(
            query = searchQuery,
            onQueryChange = onSearchChange,
            placeholder = stringResource(Res.string.reports_search_vehicles_placeholder),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

        // Select All / Clear All
        Row(horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
            FleetButton(
                text = stringResource(Res.string.reports_select_all_count, vehicles.size),
                onClick = onSelectAll,
                variant = ButtonVariant.GHOST
            )
            FleetButton(
                text = stringResource(Res.string.reports_clear_all),
                onClick = onClearAll,
                variant = ButtonVariant.GHOST
            )
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

        // Vehicle list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
        ) {
            items(vehicles, key = { it.id }) { vehicle ->
                val isSelected = selectedIds.contains(vehicle.id)
                Surface(
                    onClick = { onToggleVehicle(vehicle.id) },
                    shape = RoundedCornerShape(FleetTokens.Radius.M),
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(FleetTokens.Spacing.M),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleVehicle(vehicle.id) }
                        )
                        Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = vehicle.registrationNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${vehicle.make ?: ""} ${vehicle.model ?: ""}".trim()
                                    .ifEmpty { vehicleFallback },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            FleetButton(
                text = stringResource(Res.string.cancel),
                onClick = onDismiss,
                variant = ButtonVariant.SECONDARY,
                modifier = Modifier.weight(1f)
            )
            FleetButton(
                text = stringResource(Res.string.reports_apply_filter),
                onClick = onApply,
                variant = ButtonVariant.PRIMARY,
                modifier = Modifier.weight(1f),
                enabled = selectedIds.isNotEmpty()
            )
        }
    }
}

// ============================================================================
// Export Options Dialog
// ============================================================================



// ============================================================================
// Vehicle Selector Bottom Sheet Content
// ============================================================================


@Composable
internal fun VehicleSelectorContent(
    vehicles: List<Vehicle>,
    recentVehicles: List<Vehicle>,
    searchQuery: String,
    isLoading: Boolean,
    selectedVehicleId: String?,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onVehicleSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(horizontal = FleetTokens.Spacing.L)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = FleetTokens.Spacing.M),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.vehicle_pl_select_vehicle),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = RoundedCornerShape(FleetTokens.Radius.XL),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "${vehicles.size}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.XS)
                )
            }
        }

        // Search Field
        FleetSearchField(
            query = searchQuery,
            onQueryChange = onSearchChange,
            placeholder = stringResource(Res.string.reports_search_vehicle_hint),
            modifier = Modifier.fillMaxWidth()
        )

        if (searchQuery.isNotBlank()) {
            Text(
                text = stringResource(
                    Res.string.vehicle_pl_results_count,
                    vehicles.size,
                    if (vehicles.size != 1) "s" else ""
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = FleetTokens.Spacing.S, start = FleetTokens.Spacing.XS)
            )
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
            ) {
                if (recentVehicles.isNotEmpty() && searchQuery.isBlank()) {
                    item {
                        Text(
                            text = stringResource(Res.string.vehicle_pl_recent),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = FleetTokens.Spacing.XS)
                        )
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S),
                            modifier = Modifier.padding(bottom = FleetTokens.Spacing.S)
                        ) {
                            items(recentVehicles, key = { "recent_${it.id}" }) { vehicle ->
                                Surface(
                                    modifier = Modifier.clickable { onVehicleSelected(vehicle.id) },
                                    shape = RoundedCornerShape(FleetTokens.Radius.M),
                                    color = if (vehicle.id == selectedVehicleId)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Column(
                                        modifier = Modifier.padding(FleetTokens.Spacing.M),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = vehicle.registrationNumber,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${vehicle.make ?: ""} ${vehicle.model ?: ""}".trim().take(12),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = FleetTokens.Spacing.S),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                item {
                    Text(
                        text = if (searchQuery.isNotBlank()) stringResource(Res.string.reports_results_heading) else stringResource(Res.string.reports_all_vehicles_heading),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = FleetTokens.Spacing.XS)
                    )
                }

                if (vehicles.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(EmptyStateBoxHeight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank())
                                    stringResource(Res.string.reports_no_vehicles_match, searchQuery)
                                else stringResource(Res.string.reports_no_vehicles_available),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(vehicles, key = { it.id }) { vehicle ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onVehicleSelected(vehicle.id) },
                            shape = RoundedCornerShape(FleetTokens.Radius.M),
                            color = if (vehicle.id == selectedVehicleId)
                                MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.M),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = vehicle.registrationNumber,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
                                        Text(
                                            text = "${vehicle.make ?: ""} ${vehicle.model ?: ""}".trim().ifEmpty { "-" },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        vehicle.assignedDriverName?.let { driver ->
                                            Text(
                                                text = "• $driver",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                // Status dot
                                val colorScheme = VehicleStatus.getColorScheme(vehicle.status)
                                val statusColor = when (colorScheme) {
                                    com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.SUCCESS -> FleetStatusColors.ProfitGreen
                                    com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.WARNING -> FleetStatusColors.ExpenseAmber
                                    com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.ERROR -> FleetStatusColors.LossRed
                                    com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.INFO -> FleetStatusColors.InfoBlue
                                    com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.NEUTRAL -> FleetStatusColors.NeutralGray
                                }
                                Box(
                                    modifier = Modifier
                                        .size(FleetTokens.Spacing.S)
                                        .background(statusColor, CircleShape)
                                )

                                if (vehicle.id == selectedVehicleId) {
                                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_check),
                                        contentDescription = stringResource(Res.string.reports_cd_selected),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(FleetTokens.IconSize.M)
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(FleetTokens.Spacing.L)) }
            }
        }
    }
}

// ============================================================================
// Recent Report Card
// ============================================================================


