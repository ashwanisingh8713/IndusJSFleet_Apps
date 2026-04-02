package com.ijs.customer.presentation.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.LoadingContent
import com.ijs.customer.domain.entity.CustomerTrip
import com.ijs.customer.presentation.detail.CustomerDetailContract.Intent
import com.ijs.customer.presentation.detail.CustomerDetailContract.TripStateFilter
import com.ijs.customer.presentation.detail.CustomerDetailContract.State
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Trips Tab Content with integrated pending payment info.
 * Each trip row shows payment status, dates, and pending amount directly.
 */
@Composable
fun TripsTabContent(
    state: State,
    onIntent: (Intent) -> Unit,
    onTripClick: (String) -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Load data when tab is selected
    LaunchedEffect(Unit) {
        if (!state.tripsDataLoaded) {
            onIntent(Intent.LoadTrips)
        }
    }

    val isLoading = state.isLoadingTrips && state.trips.isEmpty()
    val tripsErrorFallback = stringResource(Res.string.customer_trips_error_generic)

    // Calculate values from trips if summary is null
    val totalTrips = state.tripsSummary?.totalTrips ?: state.trips.size
    val completedTrips = state.tripsSummary?.completedTrips ?: state.trips.count { it.state?.lowercase() == "completed" }
    val activeTrips = state.tripsSummary?.activeTrips ?: state.trips.count { it.state?.lowercase() == "on_route" }
    val totalRevenue = state.tripsSummary?.totalRevenueDisplay ?: formatCurrency(state.trips.sumOf { it.tripPrice ?: 0.0 })
    val totalPending = state.tripsSummary?.totalPendingDisplay ?: formatCurrency(state.trips.sumOf {
        val price = it.tripPrice ?: 0.0
        val paid = it.paidAmount ?: 0.0
        (price - paid).coerceAtLeast(0.0)
    })

    when {
        isLoading -> LoadingContent()
        state.tripsError != null && state.trips.isEmpty() -> ErrorContent(
            error = state.tripsError ?: tripsErrorFallback,
            onRetry = { onIntent(Intent.RefreshTrips) }
        )
        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Summary Header with Export
                item {
                    TripsSummaryCard(
                        totalTrips = totalTrips,
                        completedTrips = completedTrips,
                        activeTrips = activeTrips,
                        totalRevenue = totalRevenue,
                        totalPending = totalPending,
                        onExportPdf = onExportPdf,
                        isExporting = state.isExporting
                    )
                }

                // Filter Chips
                item {
                    TripFilterChips(
                        selectedFilter = state.tripStateFilter,
                        onFilterSelected = { onIntent(Intent.SetTripStateFilter(it)) }
                    )
                }

                // Trips List
                if (state.trips.isEmpty()) {
                    item {
                        EmptyContent(
                            title = stringResource(Res.string.report_no_trips_found),
                            message = stringResource(Res.string.customer_no_trips),
                            icon = "🚛"
                        )
                    }
                } else {
                    items(state.trips, key = { "trip_${it.id}" }) { trip ->
                        EnhancedTripRow(
                            trip = trip,
                            onClick = { onTripClick(trip.id) },
                            showFinancials = state.canViewFinancials
                        )
                    }

                    if (state.hasMoreTrips) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.isLoadingTrips) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    TextButton(onClick = { onIntent(Intent.LoadMoreTrips) }) {
                                        Text(stringResource(Res.string.action_load_more))
                                    }
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

@Composable
private fun TripsSummaryCard(
    totalTrips: Int,
    completedTrips: Int,
    activeTrips: Int,
    totalRevenue: String,
    totalPending: String,
    onExportPdf: () -> Unit,
    isExporting: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header with title and export
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.vehicles_overview),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    onClick = onExportPdf,
                    enabled = !isExporting,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            painter = painterResource(Res.drawable.ic_download),
                            contentDescription = stringResource(Res.string.cd_export_pdf),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stats Grid 2x2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatBox(
                    icon = "🚛",
                    value = totalTrips.toString(),
                    label = stringResource(Res.string.customer_total_trips_label),
                    modifier = Modifier.weight(1f)
                )
                SummaryStatBox(
                    icon = "✅",
                    value = completedTrips.toString(),
                    label = stringResource(Res.string.trip_state_completed),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatBox(
                    icon = "💰",
                    value = totalRevenue,
                    label = stringResource(Res.string.reports_revenue),
                    isPrimary = true,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatBox(
                    icon = "⏳",
                    value = totalPending,
                    label = stringResource(Res.string.customer_trip_due_label),
                    isError = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryStatBox(
    icon: String,
    value: String,
    label: String,
    isPrimary: Boolean = false,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = when {
            isError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            isPrimary -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(icon, style = MaterialTheme.typography.titleMedium)
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isError -> MaterialTheme.colorScheme.error
                        isPrimary -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TripFilterChips(
    selectedFilter: TripStateFilter,
    onFilterSelected: (TripStateFilter) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(TripStateFilter.entries) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.displayName, style = MaterialTheme.typography.labelMedium) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(32.dp)
            )
        }
    }
}

/**
 * Format currency with suffix (K, L, Cr)
 */
private fun formatCurrency(amount: Double): String {
    return when {
        amount >= 10000000 -> {
            val value = amount / 10000000
            "₹${((value * 10).toInt() / 10.0)}Cr"
        }
        amount >= 100000 -> {
            val value = amount / 100000
            "₹${((value * 10).toInt() / 10.0)}L"
        }
        amount >= 1000 -> {
            val value = amount / 1000
            "₹${((value * 10).toInt() / 10.0)}K"
        }
        else -> "₹${amount.toInt()}"
    }
}

/**
 * Enhanced Trip Row with dates, vehicle, and payment info.
 */
@Composable
private fun EnhancedTripRow(
    trip: CustomerTrip,
    onClick: () -> Unit,
    showFinancials: Boolean
) {
    // Calculate due amount
    val dueAmount = trip.pendingAmount ?: run {
        val price = trip.tripPrice ?: 0.0
        val paid = trip.paidAmount ?: 0.0
        (price - paid).coerceAtLeast(0.0)
    }
    val hasPending = dueAmount > 0
    val isPaid = trip.paymentStatus?.lowercase() == "paid"

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Row 1: Trip ID, State, Vehicle Reg
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(trip.stateIcon, style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "#${trip.id}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TripStateChip(trip.stateDisplay, trip.state)
                }
                trip.vehicleRegistration?.let { reg ->
                    Text(
                        text = reg,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: Route
            Text(
                text = trip.routeDisplay,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Row 3: Start & End Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DateLabel(
                    label = stringResource(Res.string.customer_trip_label_start),
                    date = FleetDateTime.formatIsoToDisplayDate(trip.plannedStart ?: trip.scheduledDate)
                )
                Text(
                    text = "→",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DateLabel(
                    label = stringResource(Res.string.customer_trip_label_end),
                    date = FleetDateTime.formatIsoToDisplayDate(trip.plannedEnd)
                )
            }

            // Row 4: Payment Info (if financials visible)
            if (showFinancials) {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Trip Price, Paid & Due Amount with equal spacing
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PaymentInfoItem(
                            label = stringResource(Res.string.customer_trip_price_label),
                            value = trip.tripPriceDisplay,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        PaymentInfoItem(
                            label = stringResource(Res.string.customer_trip_paid_label),
                            value = trip.paidAmountDisplay,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        PaymentInfoItem(
                            label = stringResource(Res.string.customer_trip_due_label),
                            value = formatCurrency(dueAmount),
                            color = if (hasPending) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Right: Status Badge
                    when {
                        isPaid -> {
                            PaymentStatusBadge(
                                text = stringResource(Res.string.customer_trip_paid_badge),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        hasPending -> {
                            PaymentStatusBadge(
                                text = stringResource(Res.string.customer_trip_due_label),
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        else -> {
                            PaymentStatusBadge(
                                text = stringResource(Res.string.payment_status_pending),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateLabel(label: String, date: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = date,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PaymentInfoItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun PaymentStatusBadge(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
private fun TripStateChip(label: String, state: String?) {
    val (bgColor, textColor) = when (state?.lowercase()) {
        "planned" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        "on_route" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "completed" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "cancelled" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}
