package com.ijs.dashboard.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.indusjs.fleet.domain.entity.dashboard.OngoingTrip
import com.indusjs.fleet.domain.entity.dashboard.TripSummary
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.indusjs.uicomponents.theme.FleetTokens
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Trips Status Section - Enhanced UI.
 */
@Composable
internal fun TripsStatusSection(
    tripSummary: TripSummary,
    ongoingTrips: List<OngoingTrip>,
    onClick: () -> Unit,
    onCreateTripClick: () -> Unit,
    onAddTripCostClick: () -> Unit = {}
) {
    DashboardSectionCard {
        DashboardSectionHeader(
            title = stringResource(Res.string.org_stats_trips),
            iconRes = Res.drawable.ic_trip,
            accent = MaterialTheme.colorScheme.tertiary,
            actionLabel = stringResource(Res.string.action_view_all),
            onActionClick = onClick
        )

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

        if (tripSummary.total == 0) {
            SectionEmptyState(
                iconRes = Res.drawable.ic_trip,
                title = stringResource(Res.string.dashboard_no_trips_yet),
                message = stringResource(Res.string.dashboard_create_trip_message),
                actionLabel = stringResource(Res.string.add),
                onAction = onCreateTripClick
            )
        } else {
            // Number-forward status tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
            ) {
                MetricTile(
                    value = tripSummary.inProgress.toString(),
                    label = stringResource(Res.string.dashboard_label_active),
                    accent = FleetStatusColors.FleetOnRoute,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    value = tripSummary.planned.toString(),
                    label = stringResource(Res.string.dashboard_label_planned),
                    accent = FleetStatusColors.FleetPlanned,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    value = tripSummary.completed.toString(),
                    label = stringResource(Res.string.dashboard_label_done),
                    accent = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
            }

            // Active trips preview
            if (ongoingTrips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
                Text(
                    text = stringResource(Res.string.dashboard_active_trips),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ongoingTrips.take(2).forEach { trip ->
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                    OngoingTripItem(trip = trip)
                }
            }

            // Action buttons
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
            ) {
                FleetButton(
                    text = stringResource(Res.string.action_create_trip),
                    onClick = onCreateTripClick,
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
                    onClick = onAddTripCostClick,
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
 * Individual ongoing trip item display.
 */
@Composable
private fun OngoingTripItem(trip: OngoingTrip) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FleetTokens.Radius.M),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(FleetTokens.Spacing.M),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Route info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${trip.startLocation} → ${trip.endLocation}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${trip.vehicleRegistration} • ${trip.driverName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Status badge
            Surface(
                shape = RoundedCornerShape(FleetTokens.Radius.M),
                color = FleetStatusColors.FleetOnRoute.copy(alpha = 0.15f)
            ) {
                Text(
                    text = stringResource(Res.string.dashboard_label_on_route),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = FleetStatusColors.FleetOnRoute,
                    modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XS)
                )
            }
        }
    }
}
