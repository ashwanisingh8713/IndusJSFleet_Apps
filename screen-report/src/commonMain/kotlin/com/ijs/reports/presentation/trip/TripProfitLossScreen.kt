package com.ijs.reports.presentation.trip

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.fleet.core.util.convertToEpochMillis
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetInlineErrorBanner
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetSectionHeader
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.reports.domain.entity.TripProfitLoss
import com.ijs.trip.domain.entity.Trip
import com.ijs.trip.domain.entity.TripStatus
import com.ijs.reports.presentation.trip.TripPLContract.Effect
import com.ijs.reports.presentation.trip.TripPLContract.Intent
import com.ijs.reports.presentation.trip.TripPLContract.State
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Trip Profit/Loss Screen - Trip-centric design
 * Flow: Select date range → Load trips → Select trips → Generate P&L
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripProfitLossScreen(
    viewModel: TripPLViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackbar -> {
                    pendingSnackbar = effect.message
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(Res.string.reports_trip_pl_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(Res.string.reports_trip_pl_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (!state.isLoading && state.tripsLoaded) {
                        IconButton(onClick = { viewModel.sendIntent(Intent.Refresh) }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_refresh),
                                contentDescription = stringResource(Res.string.refresh),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            TripPLContent(
                state = state,
                viewModel = viewModel
            )

            // Loading overlay
            if (state.isLoading || state.isLoadingTrips) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(FleetTokens.Radius.XL),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = FleetTokens.Elevation.Dialog
                    ) {
                        Column(
                            modifier = Modifier.padding(FleetTokens.Spacing.XXL),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
                            Text(
                                text = if (state.isLoadingTrips) stringResource(Res.string.reports_loading_trips) else stringResource(Res.string.reports_generating),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TripPLContent(
    state: State,
    viewModel: TripPLViewModel
) {
    // Inline date validation. The picker enforces minDate, but we still guard
    // format + range so the gate and inline errors stay correct.
    val startError = dateFieldError(state.startDate)
    val endError = dateFieldError(state.endDate)
    val rangeError = if (startError == null && endError == null &&
        state.startDate.isNotBlank() && state.endDate.isNotBlank()
    ) {
        val startMs = convertToEpochMillis(state.startDate)
        val endMs = convertToEpochMillis(state.endDate)
        if (startMs != null && endMs != null && endMs < startMs) {
            stringResource(Res.string.reports_date_to_before_from)
        } else null
    } else null

    val datesValid = startError == null && endError == null && rangeError == null

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val bp = rememberFleetBreakpoint()
        // Center content with a width cap on wide screens.
        val contentModifier = if (bp == FleetBreakpoint.Expanded) {
            Modifier
                .widthIn(max = FleetTokens.Width.MaxContent)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        } else {
            Modifier.fillMaxWidth()
        }

        LazyColumn(
            modifier = contentModifier.fillMaxHeight(),
            contentPadding = PaddingValues(FleetTokens.Spacing.L),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
        ) {
            // Header Card
            item {
                HeaderCard()
            }

            // Step 1: Date Range Selection
            item {
                DateRangeCard(
                    startDate = state.startDate,
                    endDate = state.endDate,
                    startError = startError,
                    endError = endError ?: rangeError,
                    onStartDateChange = { viewModel.sendIntent(Intent.UpdateStartDate(it)) },
                    onEndDateChange = { viewModel.sendIntent(Intent.UpdateEndDate(it)) }
                )
            }

            // Load Trips Button
            item {
                FleetButton(
                    text = if (state.tripsLoaded) {
                        stringResource(Res.string.reports_reload_trips)
                    } else {
                        stringResource(Res.string.reports_load_trips)
                    },
                    onClick = { viewModel.sendIntent(Intent.LoadTrips) },
                    variant = ButtonVariant.SECONDARY,
                    size = ButtonSize.LARGE,
                    enabled = state.canLoadTrips && datesValid,
                    isLoading = state.isLoadingTrips,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_search),
                            contentDescription = null,
                            modifier = Modifier.size(FleetTokens.IconSize.M)
                        )
                    }
                )
            }

            // Step 2: Trip Selection (shown after loading)
            if (state.tripsLoaded) {
                item {
                    TripSelectionCard(
                        trips = state.filteredTrips,
                        allTripsCount = state.trips.size,
                        selectedTripIds = state.selectedTripIds,
                        searchQuery = state.tripSearchQuery,
                        onSearchChange = { viewModel.sendIntent(Intent.UpdateTripSearch(it)) },
                        onToggleTrip = { viewModel.sendIntent(Intent.ToggleTrip(it)) },
                        onSelectAll = { viewModel.sendIntent(Intent.SelectAllTrips) },
                        onClearSelection = { viewModel.sendIntent(Intent.ClearSelection) }
                    )
                }

                // Generate Report Button
                item {
                    FleetButton(
                        text = stringResource(Res.string.reports_generate_pl_count, state.selectedCount),
                        onClick = { viewModel.sendIntent(Intent.GenerateReport) },
                        variant = ButtonVariant.PRIMARY,
                        size = ButtonSize.LARGE,
                        enabled = state.canGenerateReport,
                        isLoading = state.isLoading,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_trending_up),
                                contentDescription = null,
                                modifier = Modifier.size(FleetTokens.IconSize.M)
                            )
                        }
                    )
                }
            }

            // Error message
            if (state.error != null && state.results.isEmpty()) {
                item {
                    ErrorCard(error = state.error.resolve())
                }
            }

            // Results
            if (state.results.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_dashboard),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(FleetTokens.IconSize.M)
                            )
                            Text(
                                text = stringResource(Res.string.reports_pl_results_count, state.results.size),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // Summary
                        val totalProfit = state.results.sumOf { it.netProfit }
                        Surface(
                            shape = RoundedCornerShape(FleetTokens.Radius.L),
                            // §H addendum-3: neutral surface; the ▲/▼ + loss-red numeral carry the sign.
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = stringResource(Res.string.reports_total_profit, formatCurrency(totalProfit)),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                // §H money-neutral: total-profit numeral stays neutral onSurface when
                                // profit; only a genuine loss may colour it (loss-red exception).
                                color = if (totalProfit >= 0) MaterialTheme.colorScheme.onSurface else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed,
                                modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.XS)
                            )
                        }
                    }
                }
                items(state.results) { result ->
                    TripPLResultCard(result)
                }
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXL)) }
        }
    }
}

/**
 * Inline date error for the Trip P&L date-range fields. Blank is allowed (no
 * error shown until the user picks a value); otherwise the value must be a
 * valid DD-MM-YYYY date.
 */
@Composable
private fun dateFieldError(date: String): String? {
    if (date.isBlank()) return null
    return if (com.indusjs.fleet.core.util.ValidationUtils.isValidDate(date)) {
        null
    } else {
        stringResource(Res.string.reports_invalid_date)
    }
}

@Composable
private fun HeaderCard() {
    FleetSectionCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        border = null,
        elevation = FleetTokens.Elevation.None
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(FleetTokens.IconSize.XL),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_trip),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(FleetTokens.IconSize.M)
                    )
                }
            }
            Spacer(modifier = Modifier.width(FleetTokens.Spacing.L))
            Column {
                Text(
                    text = stringResource(Res.string.reports_trip_analysis),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(Res.string.reports_trip_analysis_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun DateRangeCard(
    startDate: String,
    endDate: String,
    startError: String?,
    endError: String?,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit
) {
    FleetTitledSectionCard(
        title = stringResource(Res.string.reports_step_select_date_range),
        iconRes = Res.drawable.ic_calendar
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            Column {
                FleetDateTimePicker(
                    date = startDate,
                    time = "",
                    onDateTimeChange = { newDate, _ -> onStartDateChange(newDate) },
                    label = stringResource(Res.string.reports_label_from_date),
                    mode = PickerMode.DATE_ONLY,
                    modifier = Modifier.fillMaxWidth()
                )
                if (startError != null) {
                    Text(
                        text = startError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = FleetTokens.Spacing.XS, start = FleetTokens.Spacing.M)
                    )
                }
            }
            Column {
                FleetDateTimePicker(
                    date = endDate,
                    time = "",
                    onDateTimeChange = { newDate, _ -> onEndDateChange(newDate) },
                    label = stringResource(Res.string.reports_label_to_date),
                    mode = PickerMode.DATE_ONLY,
                    minDate = startDate.ifBlank { null },
                    modifier = Modifier.fillMaxWidth()
                )
                if (endError != null) {
                    Text(
                        text = endError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = FleetTokens.Spacing.XS, start = FleetTokens.Spacing.M)
                    )
                }
            }
        }
    }
}

@Composable
private fun TripSelectionCard(
    trips: List<Trip>,
    allTripsCount: Int,
    selectedTripIds: Set<String>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleTrip: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit
) {
    FleetSectionCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                FleetSectionHeader(
                    title = stringResource(Res.string.reports_step_select_trips),
                    iconRes = Res.drawable.ic_trip,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                Text(
                    text = "${selectedTripIds.size}/$allTripsCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

            // Search field
            FleetInputField(
                value = searchQuery,
                onValueChange = onSearchChange,
                fieldType = FieldType.SEARCH,
                placeholder = stringResource(Res.string.reports_search_trips_placeholder),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_search),
                        contentDescription = null,
                        modifier = Modifier.size(FleetTokens.IconSize.M)
                    )
                }
            )

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

            // Bulk actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    FleetButton(
                        text = stringResource(Res.string.reports_action_all),
                        onClick = onSelectAll,
                        variant = ButtonVariant.GHOST,
                        size = ButtonSize.SMALL
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    FleetButton(
                        text = stringResource(Res.string.reports_action_clear),
                        onClick = onClearSelection,
                        variant = ButtonVariant.GHOST,
                        size = ButtonSize.SMALL
                    )
                }
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

            // Trip list
            if (trips.isEmpty()) {
                Text(
                    text = if (searchQuery.isNotBlank()) stringResource(Res.string.reports_no_trips_match, searchQuery) else stringResource(Res.string.reports_no_trips_found),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(FleetTokens.Spacing.L)
                )
            } else {
                Column(
                    modifier = Modifier.heightIn(max = FleetTokens.Width.DropdownMaxHeight + FleetTokens.Spacing.XXXL).verticalScroll(rememberScrollState())
                ) {
                    trips.take(100).forEach { trip ->
                        val isSelected = selectedTripIds.contains(trip.id)
                        TripSelectionItem(
                            trip = trip,
                            isSelected = isSelected,
                            onToggle = { onToggleTrip(trip.id) }
                        )
                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                    }
                    if (trips.size > 100) {
                        Text(
                            text = stringResource(Res.string.reports_trips_showing_cap, trips.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(FleetTokens.Spacing.S)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripSelectionItem(
    trip: Trip,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FleetTokens.Radius.L))
            .clickable { onToggle() },
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(FleetTokens.Radius.L)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FleetTokens.Spacing.M),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            Column(modifier = Modifier.weight(1f).padding(start = FleetTokens.Spacing.S)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.reports_trip_number, trip.id),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                    trip.vehicleNumber?.let {
                        Surface(
                            shape = RoundedCornerShape(FleetTokens.Radius.M),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = FleetTokens.Spacing.XS + FleetTokens.Spacing.XXS, vertical = FleetTokens.Spacing.XXS)
                            )
                        }
                    }
                }
                val naLabel = stringResource(Res.string.label_not_applicable)
                Text(
                    text = "${trip.startLocation?.address ?: naLabel} → ${trip.endLocation?.address ?: naLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // State badge
            val statusStr = TripStatus.getDisplayLabel(trip.status)
            val colorScheme = TripStatus.getColorScheme(trip.status)
            val stateColor = com.indusjs.uicomponents.components.stateColorSchemeToColor(colorScheme)
            Surface(
                shape = RoundedCornerShape(FleetTokens.Radius.M),
                color = stateColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = statusStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = stateColor,
                    modifier = Modifier.padding(horizontal = FleetTokens.Spacing.XS + FleetTokens.Spacing.XXS, vertical = FleetTokens.Spacing.XXS)
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(error: String) {
    FleetInlineErrorBanner(
        title = stringResource(Res.string.reports_error_title),
        message = error
    )
}

@Composable
private fun TripPLResultCard(result: TripProfitLoss) {
    // Badge is driven off the backend P&L status (profit / loss / break_even),
    // not the net-profit sign: break_even renders neutral, not as a loss.
    val profitClass = result.profitClass
    val profitColor = when (profitClass) {
        com.ijs.reports.domain.entity.PLProfitClass.PROFIT ->
            com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
        com.ijs.reports.domain.entity.PLProfitClass.LOSS ->
            com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
        com.ijs.reports.domain.entity.PLProfitClass.BREAK_EVEN ->
            com.indusjs.uicomponents.theme.FleetStatusColors.NeutralGray
    }

    FleetSectionCard(
        modifier = Modifier.animateContentSize()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(FleetTokens.Height.ButtonSmall),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_trip),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(FleetTokens.IconSize.S)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))
                    Column {
                        Text(
                            text = stringResource(Res.string.reports_trip_number, result.tripId),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        result.vehicleNumber?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        result.customerName?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                // Profit/Loss Badge
                Surface(
                    shape = RoundedCornerShape(FleetTokens.Radius.XL),
                    color = profitColor.copy(alpha = 0.15f)
                ) {
                    val badgeIcon = when (profitClass) {
                        com.ijs.reports.domain.entity.PLProfitClass.PROFIT -> Res.drawable.ic_check_circle
                        com.ijs.reports.domain.entity.PLProfitClass.LOSS -> Res.drawable.ic_warning
                        com.ijs.reports.domain.entity.PLProfitClass.BREAK_EVEN -> null
                    }
                    val badgeText = when (profitClass) {
                        com.ijs.reports.domain.entity.PLProfitClass.PROFIT ->
                            stringResource(Res.string.reports_badge_profit)
                        com.ijs.reports.domain.entity.PLProfitClass.LOSS ->
                            stringResource(Res.string.reports_badge_loss)
                        com.ijs.reports.domain.entity.PLProfitClass.BREAK_EVEN ->
                            stringResource(Res.string.profit_status_break_even)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS),
                        modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S + FleetTokens.Spacing.XXS, vertical = FleetTokens.Spacing.XS)
                    ) {
                        if (badgeIcon != null) {
                            Icon(
                                painter = painterResource(badgeIcon),
                                contentDescription = null,
                                tint = profitColor,
                                modifier = Modifier.size(FleetTokens.IconSize.S)
                            )
                        }
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = profitColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            // Main KPIs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // §H money-neutral: revenue / expenses numerals render neutral onSurface (no
                // always-on green/amber tint on a money numeral).
                MiniKPI(
                    label = stringResource(Res.string.reports_revenue),
                    value = formatCurrency(result.sellingValue),
                    color = MaterialTheme.colorScheme.onSurface
                )
                MiniKPI(
                    label = stringResource(Res.string.reports_expenses),
                    value = formatCurrency(result.totalExpenses),
                    color = MaterialTheme.colorScheme.onSurface
                )
                // §H: net-profit numeral stays neutral when profit; loss-red only on a genuine loss.
                // The profit/loss signal is carried by the status badge above, not the numeral.
                MiniKPI(
                    label = stringResource(Res.string.reports_net_profit_label),
                    value = formatCurrency(result.netProfit),
                    color = if (result.netProfit >= 0) MaterialTheme.colorScheme.onSurface else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
                )
            }

            // Route info
            if (result.startLocation != null && result.endLocation != null) {
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_map),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FleetTokens.IconSize.S)
                    )
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                    Text(
                        text = "${result.startLocation} → ${result.endLocation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Cost Breakdown (if available)
            if (result.costBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_cost),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FleetTokens.IconSize.S)
                    )
                    Text(
                        text = stringResource(Res.string.reports_cost_breakdown),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

                result.costBreakdown.forEach { cost ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = FleetTokens.Spacing.XXS),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = cost.costType.replaceFirstChar { it.uppercaseChar() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatCurrency(cost.amount),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniKPI(
    label: String,
    value: String,
    color: Color
) {
    FleetMetricTile(
        value = value,
        label = label,
        accent = color,
        valueColor = color,
        showBackground = false,
        centered = true
    )
}
