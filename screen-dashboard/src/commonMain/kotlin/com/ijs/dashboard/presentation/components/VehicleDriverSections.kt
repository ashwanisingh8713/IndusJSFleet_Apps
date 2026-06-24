package com.ijs.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.indusjs.uicomponents.theme.FleetTokens
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.domain.entity.dashboard.DriverStatusSummary
import com.indusjs.fleet.domain.entity.dashboard.VehicleStatusSummary
import com.indusjs.uicomponents.theme.FleetStatusColors
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Offline banner shown when displaying cached data due to network error.
 */
@Composable
internal fun OfflineBanner(
    message: String,
    lastUpdated: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📡",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.dashboard_offline_mode),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = if (lastUpdated != null) stringResource(Res.string.dashboard_last_updated, lastUpdated) else stringResource(Res.string.dashboard_showing_cached),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
            TextButton(
                onClick = onRetry,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(
                    text = stringResource(Res.string.retry),
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_close),
                    contentDescription = stringResource(Res.string.cd_dismiss),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Enhanced Status Chip with better visual design.
 * Used by VehicleStatusSection, DriversStatusSection, TripsStatusSection.
 */
@Composable
internal fun EnhancedStatusChip(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/**
 * Vehicle Status Section.
 */
@Composable
internal fun VehicleStatusSection(
    vehicleStatus: VehicleStatusSummary,
    onClick: () -> Unit,
    onAddVehicleClick: () -> Unit,
    onAddMaintenanceCostClick: () -> Unit = {}
) {
    DashboardSectionCard {
        DashboardSectionHeader(
            title = stringResource(Res.string.org_stats_vehicles),
            iconRes = Res.drawable.ic_truck,
            accent = MaterialTheme.colorScheme.primary,
            actionLabel = stringResource(Res.string.action_view_all),
            onActionClick = onClick
        )

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

        if (vehicleStatus.total == 0) {
            SectionEmptyState(
                iconRes = Res.drawable.ic_vehicle,
                title = stringResource(Res.string.dashboard_no_vehicles_yet),
                message = stringResource(Res.string.dashboard_add_vehicle_message),
                actionLabel = stringResource(Res.string.add),
                onAction = onAddVehicleClick
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
            ) {
                MetricTile(
                    value = vehicleStatus.onTripInProgress.toString(),
                    label = stringResource(Res.string.dashboard_label_on_route),
                    accent = FleetStatusColors.FleetOnRoute,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    value = vehicleStatus.onTripPlanned.toString(),
                    label = stringResource(Res.string.dashboard_label_planned),
                    accent = FleetStatusColors.FleetPlanned,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    value = vehicleStatus.available.toString(),
                    label = stringResource(Res.string.dashboard_label_available),
                    accent = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
            ) {
                OutlinedButton(
                    onClick = onAddVehicleClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(Res.string.action_add_vehicle), style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = onAddMaintenanceCostClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(Res.string.action_add_cost), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/**
 * Drivers Status Section - Enhanced UI.
 */
@Composable
internal fun DriversStatusSection(
    driverStatus: DriverStatusSummary,
    onClick: () -> Unit,
    onAddDriverClick: () -> Unit,
    onAddDriverCostClick: () -> Unit = {}
) {
    DashboardSectionCard {
        DashboardSectionHeader(
            title = stringResource(Res.string.org_stats_drivers),
            iconRes = Res.drawable.ic_driver,
            accent = MaterialTheme.colorScheme.secondary,
            actionLabel = stringResource(Res.string.action_view_all),
            onActionClick = onClick
        )

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

        if (driverStatus.total == 0) {
            SectionEmptyState(
                iconRes = Res.drawable.ic_driver,
                title = stringResource(Res.string.dashboard_no_drivers_yet),
                message = stringResource(Res.string.dashboard_add_driver_message),
                actionLabel = stringResource(Res.string.add),
                onAction = onAddDriverClick
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
            ) {
                MetricTile(
                    value = driverStatus.onTripInProgress.toString(),
                    label = stringResource(Res.string.dashboard_label_on_route),
                    accent = FleetStatusColors.FleetOnRoute,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    value = driverStatus.onTripPlanned.toString(),
                    label = stringResource(Res.string.dashboard_label_planned),
                    accent = FleetStatusColors.FleetPlanned,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    value = driverStatus.available.toString(),
                    label = stringResource(Res.string.dashboard_label_available),
                    accent = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
            ) {
                OutlinedButton(
                    onClick = onAddDriverClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(Res.string.action_add_driver), style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = onAddDriverCostClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(Res.string.action_add_cost), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
