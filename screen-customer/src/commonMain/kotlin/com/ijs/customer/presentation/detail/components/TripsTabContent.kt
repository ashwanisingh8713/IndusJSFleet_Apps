package com.ijs.customer.presentation.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.customer.presentation.detail.CustomerDetailContract.Intent
import com.ijs.customer.presentation.detail.CustomerDetailContract.TripStateFilter
import com.ijs.customer.presentation.detail.CustomerDetailContract.State
import com.ijs.customer.presentation.localizedDisplayName
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
    val activeTrips = state.tripsSummary?.activeTrips ?: state.trips.count { it.state?.lowercase() == "in_progress" }
    val totalRevenue = state.tripsSummary?.totalRevenueDisplay ?: formatCurrency(state.trips.sumOf { it.tripPrice ?: 0.0 })
    val totalPending = state.tripsSummary?.totalPendingDisplay ?: formatCurrency(state.trips.sumOf {
        val price = it.tripPrice ?: 0.0
        val paid = it.paidAmount ?: 0.0
        (price - paid).coerceAtLeast(0.0)
    })

    when {
        isLoading -> LoadingContent()
        state.tripsError != null && state.trips.isEmpty() -> ErrorContent(
            error = state.tripsError?.resolve() ?: tripsErrorFallback,
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
    FleetSectionCard(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentPadding = FleetTokens.Spacing.M
    ) {
        Column {
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
                label = { Text(filter.localizedDisplayName(), style = MaterialTheme.typography.labelMedium) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(32.dp)
            )
        }
    }
}

