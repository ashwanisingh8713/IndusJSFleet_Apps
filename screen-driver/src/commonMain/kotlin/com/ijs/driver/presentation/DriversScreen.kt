package com.ijs.driver.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FilterDefinition
import com.indusjs.uicomponents.components.FleetFilterBar
import com.indusjs.uicomponents.components.FleetSearchField
import com.indusjs.uicomponents.components.FleetStatusBadge
import com.indusjs.uicomponents.components.LoadingContent
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
    val snackbarHostState = remember { SnackbarHostState() }
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DriversContract.Effect.NavigateToDriverDetail -> onNavigateToDetail(effect.driverId)
                is DriversContract.Effect.NavigateToAddDriver -> onNavigateToAdd()
                is DriversContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is DriversContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
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
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(DriversContract.Intent.RefreshDrivers) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = stringResource(Res.string.refresh),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
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
                        modifier = Modifier.size(24.dp)
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            val driverStatusFilters = DriverStatus.entries.map { status ->
                FilterDefinition(
                    id = status,
                    label = DriverStatus.getDisplayLabel(status),
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
                modifier = Modifier.padding(vertical = 8.dp)
            )

            when {
                state.isLoading -> {
                    // Using reusable LoadingContent component
                    LoadingContent(message = stringResource(Res.string.loading))
                }
                state.error != null -> {
                    // Using reusable ErrorContent component
                    ErrorContent(
                        error = state.error!!,
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
                        onDriverClick = { viewModel.sendIntent(DriversContract.Intent.SelectDriver(it)) },
                        onDeleteClick = { viewModel.sendIntent(DriversContract.Intent.DeleteDriver(it)) }
                    )
                }
            }
        }
        }
    }

    // Delete confirmation dialog
    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.sendIntent(DriversContract.Intent.DismissDelete) },
            title = { Text(stringResource(Res.string.delete_confirmation_title)) },
            text = { Text(stringResource(Res.string.driver_delete_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.sendIntent(DriversContract.Intent.ConfirmDelete) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(Res.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.sendIntent(DriversContract.Intent.DismissDelete) }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun DriverList(
    drivers: List<Driver>,
    onDriverClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(drivers, key = { it.id }) { driver ->
            DriverCard(
                driver = driver,
                onClick = { onDriverClick(driver.id) },
                onDeleteClick = { onDeleteClick(driver.id) }
            )
        }
    }
}

@Composable
private fun DriverCard(
    driver: Driver,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${driver.firstName.firstOrNull() ?: ""}${driver.lastName.firstOrNull() ?: ""}".uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = driver.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📱",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = driver.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                StatusBadge(status = driver.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Info Row - Stats displayed inline without cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DriverInfoItem(
                    icon = "⭐",
                    value = "${driver.rating}",
                    label = "Rating"
                )

                VerticalDivider(
                    modifier = Modifier.height(32.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                DriverInfoItem(
                    icon = "🛣️",
                    value = "${driver.totalTrips}",
                    label = "Trips"
                )

                VerticalDivider(
                    modifier = Modifier.height(32.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                DriverInfoItem(
                    icon = "🪪",
                    value = driver.licenseNumber.take(10),
                    label = "License"
                )
            }

            // Location if available
            driver.currentLocation?.let { location ->
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📍",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = location.address ?: "Unknown location",
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
    icon: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusBadge(status: DriverStatus) {
    val colorScheme = DriverStatus.getColorScheme(status)
    val color = when (colorScheme) {
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.SUCCESS -> MaterialTheme.colorScheme.primary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.WARNING -> MaterialTheme.colorScheme.secondary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.ERROR -> MaterialTheme.colorScheme.error
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.INFO -> MaterialTheme.colorScheme.tertiary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.NEUTRAL -> MaterialTheme.colorScheme.outline
    }
    val text = DriverStatus.getDisplayLabel(status)

    // Using reusable FleetStatusBadge component
    FleetStatusBadge(
        status = text,
        color = color
    )
}
