package com.ijs.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.domain.entity.dashboard.DriverStatusSummary
import com.indusjs.fleet.domain.entity.dashboard.TripSummary
import com.indusjs.fleet.domain.entity.dashboard.VehicleStatusSummary

// ============ Helper Functions ============

/**
 * Calculate fleet health percentage based on active vehicles.
 */
internal fun calculateFleetHealth(vehicleStatus: VehicleStatusSummary): Int {
    if (vehicleStatus.total == 0) return 100
    val activeCount = vehicleStatus.available + vehicleStatus.onTripPlanned + vehicleStatus.onTripInProgress
    return ((activeCount.toFloat() / vehicleStatus.total.toFloat()) * 100).toInt().coerceIn(0, 100)
}

/**
 * Get fleet health color based on percentage.
 */
@Composable
internal fun getFleetHealthColor(percentage: Int): Color {
    return when {
        percentage >= 80 -> Color(0xFF4CAF50) // Green
        percentage >= 60 -> Color(0xFFFFA726) // Orange
        else -> Color(0xFFEF5350) // Red
    }
}

/**
 * Get fleet health label based on percentage.
 */
internal fun getFleetHealthLabel(percentage: Int): String {
    return when {
        percentage >= 80 -> "Excellent"
        percentage >= 60 -> "Good"
        percentage >= 40 -> "Fair"
        else -> "Needs Attention"
    }
}

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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Fleet overview summary" },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Title
        Text(
            text = "Fleet Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FleetMetricCard(
                icon = "🚛",
                value = vehicleStatus.total,
                label = "Vehicles",
                subLabel = "${vehicleStatus.available} available",
                onClick = onVehiclesClick,
                modifier = Modifier.weight(1f),
                accentColor = MaterialTheme.colorScheme.primary
            )
            FleetMetricCard(
                icon = "👨‍✈️",
                value = driverStatus.total,
                label = "Drivers",
                subLabel = "${driverStatus.available} available",
                onClick = onDriversClick,
                modifier = Modifier.weight(1f),
                accentColor = MaterialTheme.colorScheme.secondary
            )
            FleetMetricCard(
                icon = "🗺️",
                value = tripSummary.total,
                label = "Trips",
                subLabel = "${tripSummary.inProgress} active",
                onClick = onTripsClick,
                modifier = Modifier.weight(1f),
                accentColor = MaterialTheme.colorScheme.tertiary
            )
        }

        // Bottom color range bar showing vehicle status distribution
        if (vehicleStatus.total > 0) {
            FleetStatusRatioBar(vehicleStatus = vehicleStatus)
        }
    }
}

/**
 * Fleet Metric Card - Individual metric display.
 */
@Composable
private fun FleetMetricCard(
    icon: String,
    value: Int,
    label: String,
    subLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Color bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (onRouteFraction > 0) {
                Box(
                    modifier = Modifier
                        .weight(onRouteFraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(Color(0xFF4CAF50))
                )
            }
            if (plannedFraction > 0) {
                Box(
                    modifier = Modifier
                        .weight(plannedFraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(Color(0xFF2196F3))
                )
            }
            if (availableFraction > 0) {
                Box(
                    modifier = Modifier
                        .weight(availableFraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(Color(0xFF009688))
                )
            }
            if (maintenanceFraction > 0) {
                Box(
                    modifier = Modifier
                        .weight(maintenanceFraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(Color(0xFFFF9800))
                )
            }
            if (inactiveFraction > 0) {
                Box(
                    modifier = Modifier
                        .weight(inactiveFraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(Color(0xFF9E9E9E))
                )
            }
        }

        // Legend row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (onRouteCount > 0) {
                RatioLegendItem(color = Color(0xFF4CAF50), label = "Route", count = onRouteCount)
            }
            if (plannedCount > 0) {
                RatioLegendItem(color = Color(0xFF2196F3), label = "Planned", count = plannedCount)
            }
            if (availableCount > 0) {
                RatioLegendItem(color = Color(0xFF009688), label = "Available", count = availableCount)
            }
            if (maintenanceCount > 0) {
                RatioLegendItem(color = Color(0xFFFF9800), label = "Maint.", count = maintenanceCount)
            }
            if (inactiveCount > 0) {
                RatioLegendItem(color = Color(0xFF9E9E9E), label = "Inactive", count = inactiveCount)
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
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
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

