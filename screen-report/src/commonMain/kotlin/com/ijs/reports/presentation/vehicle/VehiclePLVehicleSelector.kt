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
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.components.FleetSearchField
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.ijs.vehicle.domain.entity.Vehicle
import com.ijs.vehicle.domain.entity.VehicleStatus
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
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

        Spacer(modifier = Modifier.height(12.dp))

        // Search
        FleetSearchField(
            query = searchQuery,
            onQueryChange = onSearchChange,
            placeholder = stringResource(Res.string.reports_search_vehicles_placeholder),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Select All / Clear All
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onSelectAll) {
                Text(stringResource(Res.string.reports_select_all_count, vehicles.size))
            }
            TextButton(onClick = onClearAll) {
                Text(stringResource(Res.string.reports_clear_all))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Vehicle list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(vehicles, key = { it.id }) { vehicle ->
                val isSelected = selectedIds.contains(vehicle.id)
                Surface(
                    onClick = { onToggleVehicle(vehicle.id) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleVehicle(vehicle.id) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(Res.string.cancel))
            }
            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                enabled = selectedIds.isNotEmpty()
            ) {
                Text(stringResource(Res.string.reports_apply_filter))
            }
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
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.vehicle_pl_select_vehicle),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "${vehicles.size}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                modifier = Modifier.padding(top = 6.dp, start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (recentVehicles.isNotEmpty() && searchQuery.isBlank()) {
                    item {
                        Text(
                            text = stringResource(Res.string.vehicle_pl_recent),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            items(recentVehicles, key = { "recent_${it.id}" }) { vehicle ->
                                Surface(
                                    modifier = Modifier.clickable { onVehicleSelected(vehicle.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (vehicle.id == selectedVehicleId)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
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
                            modifier = Modifier.padding(vertical = 6.dp),
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
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (vehicles.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
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
                            shape = RoundedCornerShape(8.dp),
                            color = if (vehicle.id == selectedVehicleId)
                                MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = vehicle.registrationNumber,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                        .size(8.dp)
                                        .background(statusColor, CircleShape)
                                )

                                if (vehicle.id == selectedVehicleId) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_check),
                                        contentDescription = stringResource(Res.string.reports_cd_selected),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

// ============================================================================
// Recent Report Card
// ============================================================================


