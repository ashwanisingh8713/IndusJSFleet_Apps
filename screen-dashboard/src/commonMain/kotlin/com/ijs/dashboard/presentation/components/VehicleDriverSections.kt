package com.ijs.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.indusjs.fleet.domain.entity.dashboard.DriverStatusSummary
import com.indusjs.fleet.domain.entity.dashboard.VehicleStatusSummary
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.indusjs.uicomponents.theme.FleetTokens
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
        tonalElevation = FleetTokens.Elevation.Raised
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FleetTokens.Spacing.L, vertical = FleetTokens.Spacing.S),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(FleetTokens.IconSize.L)
                    .background(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(FleetTokens.Radius.M)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_warning),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(FleetTokens.IconSize.M)
                )
            }
            Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))
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
                modifier = Modifier.size(FleetTokens.IconSize.L)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_close),
                    contentDescription = stringResource(Res.string.cd_dismiss),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(FleetTokens.IconSize.M)
                )
            }
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
                    accent = FleetStatusColors.FleetAvailable,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
            ) {
                FleetButton(
                    text = stringResource(Res.string.action_add_vehicle),
                    onClick = onAddVehicleClick,
                    variant = ButtonVariant.SECONDARY,
                    size = ButtonSize.SMALL,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_add),
                            contentDescription = null,
                            modifier = Modifier.size(FleetTokens.IconSize.S)
                        )
                    }
                )
                FleetButton(
                    text = stringResource(Res.string.action_add_cost),
                    onClick = onAddMaintenanceCostClick,
                    variant = ButtonVariant.SECONDARY,
                    size = ButtonSize.SMALL,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_add),
                            contentDescription = null,
                            modifier = Modifier.size(FleetTokens.IconSize.S)
                        )
                    }
                )
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
            accent = MaterialTheme.colorScheme.primary, // §f8: one accent (was secondary/purple)
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
                    accent = FleetStatusColors.FleetAvailable,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
            ) {
                FleetButton(
                    text = stringResource(Res.string.action_add_driver),
                    onClick = onAddDriverClick,
                    variant = ButtonVariant.SECONDARY,
                    size = ButtonSize.SMALL,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_add),
                            contentDescription = null,
                            modifier = Modifier.size(FleetTokens.IconSize.S)
                        )
                    }
                )
                FleetButton(
                    text = stringResource(Res.string.action_add_cost),
                    onClick = onAddDriverCostClick,
                    variant = ButtonVariant.SECONDARY,
                    size = ButtonSize.SMALL,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_add),
                            contentDescription = null,
                            modifier = Modifier.size(FleetTokens.IconSize.S)
                        )
                    }
                )
            }
        }
    }
}
