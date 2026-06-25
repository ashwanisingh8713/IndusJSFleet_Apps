package com.ijs.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.fleet.domain.entity.dashboard.DriverStatusSummary
import com.indusjs.fleet.domain.entity.dashboard.TripSummary
import com.indusjs.fleet.domain.entity.dashboard.VehicleStatusSummary
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

// ============ Fleet Overview Hero Card ============

/**
 * Fleet Overview Hero Card - Key metrics at a glance with fleet health indicator.
 */
@Composable
internal fun FleetOverviewHeroCard(
    vehicleStatus: VehicleStatusSummary,
    driverStatus: DriverStatusSummary,
    tripSummary: TripSummary,
    onVehiclesClick: () -> Unit,
    onDriversClick: () -> Unit,
    onTripsClick: () -> Unit
) {
    val fleetOverviewDesc = stringResource(Res.string.cd_fleet_overview_summary)
    DashboardSectionCard(
        modifier = Modifier.semantics { contentDescription = fleetOverviewDesc }
    ) {
        DashboardSectionHeader(
            title = stringResource(Res.string.dashboard_fleet_overview),
            iconRes = Res.drawable.ic_truck,
            accent = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

        // Number-forward KPI tiles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            MetricTile(
                value = vehicleStatus.total.toString(),
                label = stringResource(Res.string.org_stats_vehicles),
                subLabel = stringResource(Res.string.dashboard_count_available, vehicleStatus.available),
                iconRes = Res.drawable.ic_truck,
                accent = MaterialTheme.colorScheme.primary,
                onClick = onVehiclesClick,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                value = driverStatus.total.toString(),
                label = stringResource(Res.string.org_stats_drivers),
                subLabel = stringResource(Res.string.dashboard_count_available, driverStatus.available),
                iconRes = Res.drawable.ic_driver,
                accent = MaterialTheme.colorScheme.secondary,
                onClick = onDriversClick,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                value = tripSummary.total.toString(),
                label = stringResource(Res.string.org_stats_trips),
                subLabel = stringResource(Res.string.dashboard_count_active, tripSummary.inProgress),
                iconRes = Res.drawable.ic_trip,
                accent = MaterialTheme.colorScheme.tertiary,
                onClick = onTripsClick,
                modifier = Modifier.weight(1f)
            )
        }

        // Vehicle status distribution bar
        if (vehicleStatus.total > 0) {
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
            FleetStatusRatioBar(vehicleStatus = vehicleStatus)
        }
    }
}

/**
 * Fleet Status Ratio Bar - Linear color bar showing vehicle status distribution.
 */
@Composable
private fun FleetStatusRatioBar(vehicleStatus: VehicleStatusSummary) {
    val total = vehicleStatus.total.toFloat().coerceAtLeast(1f)

    val onRouteCount = vehicleStatus.onTripInProgress
    val plannedCount = vehicleStatus.onTripPlanned
    val availableCount = vehicleStatus.available
    val maintenanceCount = vehicleStatus.underMaintenance
    val inactiveCount = vehicleStatus.inactive

    val onRouteFraction = onRouteCount / total
    val plannedFraction = plannedCount / total
    val availableFraction = availableCount / total
    val maintenanceFraction = maintenanceCount / total
    val inactiveFraction = inactiveCount / total

    Column(
        verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
    ) {
        // Color bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(FleetTokens.Spacing.S)
                .clip(RoundedCornerShape(FleetTokens.Radius.S))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (onRouteFraction > 0) {
                Box(
                    modifier = Modifier
                        .weight(onRouteFraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(com.indusjs.uicomponents.theme.FleetStatusColors.FleetOnRoute)
                )
            }
            if (plannedFraction > 0) {
                Box(
                    modifier = Modifier
                        .weight(plannedFraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(com.indusjs.uicomponents.theme.FleetStatusColors.FleetPlanned)
                )
            }
            if (availableFraction > 0) {
                Box(
                    modifier = Modifier
                        .weight(availableFraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(com.indusjs.uicomponents.theme.FleetStatusColors.FleetAvailable)
                )
            }
            if (maintenanceFraction > 0) {
                Box(
                    modifier = Modifier
                        .weight(maintenanceFraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(com.indusjs.uicomponents.theme.FleetStatusColors.FleetMaintenance)
                )
            }
            if (inactiveFraction > 0) {
                Box(
                    modifier = Modifier
                        .weight(inactiveFraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(com.indusjs.uicomponents.theme.FleetStatusColors.FleetInactive)
                )
            }
        }

        // Legend row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            if (onRouteCount > 0) {
                RatioLegendItem(color = com.indusjs.uicomponents.theme.FleetStatusColors.FleetOnRoute, label = stringResource(Res.string.dashboard_label_route), count = onRouteCount)
            }
            if (plannedCount > 0) {
                RatioLegendItem(color = com.indusjs.uicomponents.theme.FleetStatusColors.FleetPlanned, label = stringResource(Res.string.dashboard_label_planned), count = plannedCount)
            }
            if (availableCount > 0) {
                RatioLegendItem(color = com.indusjs.uicomponents.theme.FleetStatusColors.FleetAvailable, label = stringResource(Res.string.dashboard_label_available), count = availableCount)
            }
            if (maintenanceCount > 0) {
                RatioLegendItem(color = com.indusjs.uicomponents.theme.FleetStatusColors.FleetMaintenance, label = stringResource(Res.string.dashboard_label_maintenance), count = maintenanceCount)
            }
            if (inactiveCount > 0) {
                RatioLegendItem(color = com.indusjs.uicomponents.theme.FleetStatusColors.FleetInactive, label = stringResource(Res.string.dashboard_label_inactive), count = inactiveCount)
            }
        }
    }
}

/**
 * Ratio Legend Item - Compact legend with color dot, count, and label.
 */
@Composable
private fun RatioLegendItem(
    color: Color,
    label: String,
    count: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
    ) {
        Box(
            modifier = Modifier
                .size(FleetTokens.Spacing.S)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
