package com.ijs.driver.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.uicomponents.components.DeleteConfirmationDialog
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FilterDefinition
import com.indusjs.uicomponents.components.FleetAvatar
import com.indusjs.uicomponents.components.FleetFilterBar
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetSearchField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetStatusBadge
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAtLeastMedium
import com.indusjs.uicomponents.theme.isExpanded
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.driver.domain.entity.Driver
import com.ijs.driver.domain.entity.DriverStatus
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Drivers List Screen composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriversScreen(
    viewModel: DriversViewModel,
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToAdd: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val resourceLabels = driverStatusLabelsByApi()
    // Merge: DB-cached labels take priority over resource strings
    val driverStatusLabels = remember(resourceLabels, state.stateLabels) {
        resourceLabels + state.stateLabels
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val pullRefreshState = rememberPullToRefreshState()
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DriversContract.Effect.NavigateToDriverDetail -> onNavigateToDetail(effect.driverId)
                is DriversContract.Effect.NavigateToAddDriver -> onNavigateToAdd()
                is DriversContract.Effect.ShowSnackbar -> {
                    pendingSnackbar = effect.message
                }
                is DriversContract.Effect.ShowError -> {
                    pendingSnackbar = effect.message
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.drivers_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(DriversContract.Intent.RefreshDrivers) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = stringResource(Res.string.refresh),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            // Only show FAB when there's content (not empty and not loading)
            if (!state.isLoading && state.filteredDrivers.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.sendIntent(DriversContract.Intent.AddDriver) }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = stringResource(Res.string.drivers_add),
                        modifier = Modifier.size(FleetTokens.IconSize.Default)
                    )
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.sendIntent(DriversContract.Intent.RefreshDrivers) },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
            // Search Bar - using reusable component
            FleetSearchField(
                query = state.searchQuery,
                onQueryChange = { viewModel.sendIntent(DriversContract.Intent.SearchDrivers(it)) },
                placeholder = stringResource(Res.string.drivers_search_placeholder),
                modifier = Modifier.padding(
                    horizontal = FleetTokens.Spacing.ScreenHorizontal,
                    vertical = FleetTokens.Spacing.S
                )
            )

            val driverStatusFilters = DriverStatus.entries.map { status ->
                FilterDefinition(
                    id = status,
                    label = driverStatusLabel(status, driverStatusLabels),
                    count = state.drivers.count { it.status == status }
                )
            }
            FleetFilterBar(
                filters = driverStatusFilters,
                selectedFilterId = state.selectedStatusFilter,
                onFilterSelected = { id ->
                    when {
                        id == null -> viewModel.sendIntent(DriversContract.Intent.FilterByStatus(null))
                        id == state.selectedStatusFilter ->
                            viewModel.sendIntent(DriversContract.Intent.FilterByStatus(null))
                        else -> viewModel.sendIntent(DriversContract.Intent.FilterByStatus(id))
                    }
                },
                allLabel = stringResource(Res.string.all_filter),
                allCount = state.drivers.size,
                modifier = Modifier.padding(vertical = FleetTokens.Spacing.S)
            )

            when {
                state.isLoading -> {
                    // Using reusable LoadingContent component
                    LoadingContent(message = stringResource(Res.string.loading))
                }
                state.error != null -> {
                    // Using reusable ErrorContent component
                    ErrorContent(
                        error = state.error?.resolve() ?: stringResource(Res.string.error_generic),
                        screenContext = FleetErrorContext.DRIVERS,
                        onRetry = { viewModel.sendIntent(DriversContract.Intent.LoadDrivers) }
                    )
                }
                state.filteredDrivers.isEmpty() -> {
                    // Using reusable EmptyContent component with SVG icon
                    val isFiltering = state.searchQuery.isNotEmpty() || state.selectedStatusFilter != null
                    if (isFiltering) {
                        EmptyContent(
                            iconRes = Res.drawable.ic_search,
                            title = stringResource(Res.string.no_data_for_filter),
                            message = stringResource(Res.string.adjust_search_filters)
                        )
                    } else {
                        EmptyContent(
                            iconRes = Res.drawable.ic_driver,
                            title = stringResource(Res.string.drivers_empty_title),
                            message = stringResource(Res.string.drivers_empty_message),
                            actionLabel = stringResource(Res.string.drivers_add),
                            onAction = { viewModel.sendIntent(DriversContract.Intent.AddDriver) }
                        )
                    }
                }
                else -> {
                    DriverList(
                        drivers = state.filteredDrivers,
                        driverStatusLabels = driverStatusLabels,
                        onDriverClick = { viewModel.sendIntent(DriversContract.Intent.SelectDriver(it)) },
                        onDeleteClick = { viewModel.sendIntent(DriversContract.Intent.DeleteDriver(it)) }
                    )
                }
            }
        }
        }
    }

    // Delete confirmation dialog (standardized)
    DeleteConfirmationDialog(
        showDialog = state.showDeleteConfirmation,
        entityName = stringResource(Res.string.driver_entity),
        isLoading = state.isDeleting,
        onConfirmDelete = { viewModel.sendIntent(DriversContract.Intent.ConfirmDelete) },
        onDismiss = { viewModel.sendIntent(DriversContract.Intent.DismissDelete) }
    )
}

@Composable
private fun DriverList(
    drivers: List<Driver>,
    driverStatusLabels: Map<String, String>,
    onDriverClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val breakpoint = rememberFleetBreakpoint()
        // Compact stays 1-up; Medium/Expanded show two cards per row. On Expanded
        // the grid is capped to a readable width and centered instead of stretching.
        val columns = if (breakpoint.isAtLeastMedium) 2 else 1
        val contentWidthModifier = if (breakpoint.isExpanded) {
            Modifier.fillMaxWidth().widthIn(max = FleetTokens.Width.MaxContent)
        } else {
            Modifier.fillMaxWidth()
        }

        LazyColumn(
            modifier = contentWidthModifier.fillMaxHeight().align(Alignment.TopCenter),
            contentPadding = PaddingValues(FleetTokens.Spacing.L),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            if (columns == 1) {
                items(drivers, key = { it.id }) { driver ->
                    DriverCard(
                        driver = driver,
                        driverStatusLabels = driverStatusLabels,
                        onClick = { onDriverClick(driver.id) },
                        onDeleteClick = { onDeleteClick(driver.id) }
                    )
                }
            } else {
                val rows = drivers.chunked(columns)
                items(rows, key = { row -> row.first().id }) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                    ) {
                        row.forEach { driver ->
                            DriverCard(
                                driver = driver,
                                driverStatusLabels = driverStatusLabels,
                                onClick = { onDriverClick(driver.id) },
                                onDeleteClick = { onDeleteClick(driver.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Keep the last odd card aligned to a single column width.
                        if (row.size < columns) {
                            repeat(columns - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverCard(
    driver: Driver,
    driverStatusLabels: Map<String, String>,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FleetSectionCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column {
            // Header Row - Avatar, Name, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar
                    FleetAvatar(name = driver.fullName)

                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))

                    Column {
                        Text(
                            text = driver.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXS))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_phone),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(FleetTokens.IconSize.S)
                            )
                            Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                            Text(
                                text = driver.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                StatusBadge(status = driver.status, driverStatusLabels = driverStatusLabels)
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

            // Info Row - Stats displayed inline without cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DriverInfoItem(
                    iconRes = Res.drawable.ic_star,
                    value = if (driver.rating > 0.0) "${driver.rating}" else "—",
                    label = stringResource(Res.string.driver_overview_rating)
                )

                VerticalDivider(
                    modifier = Modifier.height(FleetTokens.Spacing.XXL),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                DriverInfoItem(
                    iconRes = Res.drawable.ic_trip,
                    value = "${driver.totalTrips}",
                    label = stringResource(Res.string.driver_overview_trips)
                )

                VerticalDivider(
                    modifier = Modifier.height(FleetTokens.Spacing.XXL),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                DriverInfoItem(
                    iconRes = Res.drawable.ic_profile,
                    value = driver.licenseNumber.take(10),
                    label = stringResource(Res.string.drivers_license)
                )
            }

            // Location if available
            driver.currentLocation?.let { location ->
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_map),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FleetTokens.IconSize.S)
                    )
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                    Text(
                        text = location.address ?: stringResource(Res.string.driver_unknown_location),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverInfoItem(
    iconRes: org.jetbrains.compose.resources.DrawableResource,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    FleetMetricTile(
        value = value,
        label = label,
        iconRes = iconRes,
        showBackground = false,
        centered = true,
        modifier = modifier.padding(horizontal = FleetTokens.Spacing.S)
    )
}

@Composable
private fun StatusBadge(status: DriverStatus, driverStatusLabels: Map<String, String>) {
    val colorScheme = DriverStatus.getColorScheme(status)
    val color = when (colorScheme) {
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.SUCCESS -> MaterialTheme.colorScheme.primary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.WARNING -> MaterialTheme.colorScheme.secondary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.ERROR -> MaterialTheme.colorScheme.error
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.INFO -> MaterialTheme.colorScheme.tertiary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.NEUTRAL -> MaterialTheme.colorScheme.outline
    }
    val text = driverStatusLabel(status, driverStatusLabels)

    // Using reusable FleetStatusBadge component
    FleetStatusBadge(
        status = text,
        color = color
    )
}
