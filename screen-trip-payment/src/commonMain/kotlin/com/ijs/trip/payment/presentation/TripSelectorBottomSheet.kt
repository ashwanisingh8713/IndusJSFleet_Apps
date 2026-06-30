package com.ijs.trip.payment.presentation

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.FleetFilterChip
import com.indusjs.uicomponents.components.FleetSearchField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.trip.payment.domain.entity.*
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Bottom sheet for selecting a trip when adding a payment.
 * Includes search, status/payment filters, and enhanced trip cards.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun TripSelectorBottomSheet(
    trips: List<TripSummaryForPayment>,
    searchQuery: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit,
    onSelect: (TripSummaryForPayment) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Filter state
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }
    var selectedPaymentFilter by remember { mutableStateOf<String?>(null) }
    var showOnlyWithPending by remember { mutableStateOf(true) }

    // Filter trips based on search and filters
    val filteredTrips = remember(trips, searchQuery, selectedStatusFilter, selectedPaymentFilter, showOnlyWithPending) {
        trips.filter { trip ->
            val matchesSearch = searchQuery.isBlank() ||
                trip.id.contains(searchQuery, ignoreCase = true) ||
                trip.vehicleRegistration.contains(searchQuery, ignoreCase = true) ||
                trip.startLocation.contains(searchQuery, ignoreCase = true) ||
                trip.endLocation.contains(searchQuery, ignoreCase = true) ||
                (trip.customerName?.contains(searchQuery, ignoreCase = true) == true)

            val matchesTripStatus = selectedStatusFilter == null ||
                trip.state?.lowercase() == selectedStatusFilter?.lowercase()

            val matchesPaymentStatus = selectedPaymentFilter == null ||
                trip.paymentStatus?.lowercase() == selectedPaymentFilter?.lowercase()

            val matchesPending = !showOnlyWithPending || trip.hasPendingAmount

            matchesSearch && matchesTripStatus && matchesPaymentStatus && matchesPending
        }.sortedByDescending { it.pendingAmount } // Show highest pending first
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            val bp = rememberFleetBreakpoint()
            val gridColumns = if (bp == FleetBreakpoint.Compact) 1 else 2

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = FleetTokens.Width.MaxContent)
                    .align(Alignment.TopCenter)
                    .padding(horizontal = FleetTokens.Spacing.L)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.payment_select_trip_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(Res.string.payment_trips_count, filteredTrips.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

                // Search field
                FleetSearchField(
                    query = searchQuery,
                    onQueryChange = onSearch,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = stringResource(Res.string.payment_search_trips)
                )

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

                // Filter chips row
                TripFilterChips(
                    showOnlyWithPending = showOnlyWithPending,
                    selectedStatusFilter = selectedStatusFilter,
                    selectedPaymentFilter = selectedPaymentFilter,
                    onTogglePending = { showOnlyWithPending = !showOnlyWithPending },
                    onStatusFilterChange = { selectedStatusFilter = it },
                    onPaymentFilterChange = { selectedPaymentFilter = it }
                )

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (filteredTrips.isEmpty()) {
                    TripSelectorEmptyContent(
                        searchQuery = searchQuery,
                        showOnlyWithPending = showOnlyWithPending,
                        onShowAll = { showOnlyWithPending = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S),
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                    ) {
                        items(filteredTrips, key = { it.id }) { trip ->
                            EnhancedTripCard(
                                trip = trip,
                                onSelect = { onSelect(trip) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TripFilterChips(
    showOnlyWithPending: Boolean,
    selectedStatusFilter: String?,
    selectedPaymentFilter: String?,
    onTogglePending: () -> Unit,
    onStatusFilterChange: (String?) -> Unit,
    onPaymentFilterChange: (String?) -> Unit
) {
    val unpaidLabel = stringResource(Res.string.payment_trip_payment_unpaid)
    val partialLabel = stringResource(Res.string.payment_trip_payment_partial_label)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S),
        verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
    ) {
        // Show only pending toggle
        FleetFilterChip(
            selected = showOnlyWithPending,
            label = stringResource(Res.string.payment_filter_with_pending),
            onClick = onTogglePending
        )

        // Trip Status filters
        listOf("on_route", "completed", "planned").forEach { value ->
            FleetFilterChip(
                selected = selectedStatusFilter == value,
                onClick = {
                    onStatusFilterChange(if (selectedStatusFilter == value) null else value)
                },
                label = tripStateFilterLabel(value)
            )
        }

        // Payment Status filters
        listOf("pending" to unpaidLabel, "partial" to partialLabel).forEach { (value, label) ->
            FilterChip(
                selected = selectedPaymentFilter == value,
                onClick = {
                    onPaymentFilterChange(if (selectedPaymentFilter == value) null else value)
                },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (value == "pending")
                        FleetStatusColors.PaymentPending.copy(alpha = 0.2f)
                    else
                        FleetStatusColors.PaymentPartial.copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
private fun TripSelectorEmptyContent(
    searchQuery: String,
    showOnlyWithPending: Boolean,
    onShowAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyContent(
        icon = "📋",
        title = if (searchQuery.isBlank()) {
            stringResource(Res.string.payment_no_trips_available)
        } else {
            stringResource(Res.string.payment_no_matching_trips)
        },
        actionLabel = if (showOnlyWithPending) {
            stringResource(Res.string.payment_show_all_trips)
        } else {
            null
        },
        onAction = onShowAll.takeIf { showOnlyWithPending },
        modifier = modifier,
        fillMaxSize = false
    )
}

/**
 * Enhanced trip card for the trip selector bottom sheet.
 * Shows trip ID, vehicle, route, customer, date, and financial info.
 */
@Composable
internal fun EnhancedTripCard(
    trip: TripSummaryForPayment,
    onSelect: () -> Unit
) {
    val paymentStatusColor = when {
        trip.isFullyPaid -> FleetStatusColors.PaymentReceived
        trip.paidAmount > 0 -> FleetStatusColors.PaymentPartial
        else -> FleetStatusColors.PaymentPending
    }

    val tripStateColor = when (trip.state?.lowercase()) {
        "completed" -> FleetStatusColors.PaymentReceived
        "on_route" -> FleetStatusColors.TripOnRoute
        "planned" -> FleetStatusColors.TripPlanned
        "cancelled", "failed" -> FleetStatusColors.TripFailed
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    FleetSectionCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect,
        contentPadding = FleetTokens.Spacing.M
    ) {
        // Row 1: Trip ID, Vehicle, Trip Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
            ) {
                Text(
                    text = "#${trip.id}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(FleetTokens.Radius.M),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = trip.vehicleRegistration,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(
                            horizontal = FleetTokens.Spacing.XS,
                            vertical = FleetTokens.Spacing.XXS
                        )
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(FleetTokens.Radius.M),
                color = tripStateColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = tripCardStateLabel(trip.state),
                    style = MaterialTheme.typography.labelSmall,
                    color = tripStateColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(
                        horizontal = FleetTokens.Spacing.XS,
                        vertical = FleetTokens.Spacing.XXS
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))

        // Row 2: Route
        Text(
            text = "📍 ${trip.routeDisplay}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Row 3: Customer & Date
        if (trip.customerName != null || trip.scheduledDate != null) {
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trip.customerName?.let { customer ->
                    Text(
                        text = "👤 ${customer.take(20)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
                trip.scheduledDate?.let { date ->
                    Text(
                        text = "📅 $date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

        // Row 4: Financial info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = stringResource(Res.string.payment_add_trip_price),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.tripPriceDisplay,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(Res.string.payment_status_received),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.paidAmountDisplay,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = FleetStatusColors.PaymentReceived
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(Res.string.payment_add_pending),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.pendingDisplay,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = paymentStatusColor
                )
            }
        }
    }
}

