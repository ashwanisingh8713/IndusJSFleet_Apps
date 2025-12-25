package com.indusjs.fleet.presentation.drivers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.EmptyContent
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.FleetSearchField
import com.indusjs.fleet.core.ui.FleetStatusBadge
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.domain.entity.driver.Driver
import com.indusjs.fleet.domain.entity.driver.DriverStatus
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

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
                title = { Text("Drivers") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(DriversContract.Intent.RefreshDrivers) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.sendIntent(DriversContract.Intent.AddDriver) }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = "Add Driver",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar - using reusable component
            FleetSearchField(
                query = state.searchQuery,
                onQueryChange = { viewModel.sendIntent(DriversContract.Intent.SearchDrivers(it)) },
                placeholder = "Search drivers...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Status Filter Chips
            StatusFilterChips(
                selectedStatus = state.selectedStatusFilter,
                onStatusSelected = { viewModel.sendIntent(DriversContract.Intent.FilterByStatus(it)) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            when {
                state.isLoading -> {
                    // Using reusable LoadingContent component
                    LoadingContent(message = "Loading drivers...")
                }
                state.error != null -> {
                    // Using reusable ErrorContent component
                    ErrorContent(
                        error = state.error!!,
                        onRetry = { viewModel.sendIntent(DriversContract.Intent.LoadDrivers) }
                    )
                }
                state.filteredDrivers.isEmpty() -> {
                    // Using reusable EmptyContent component
                    EmptyContent(
                        icon = "👥",
                        title = if (state.searchQuery.isNotEmpty() || state.selectedStatusFilter != null)
                            "No drivers match your filters"
                        else
                            "No drivers found",
                        message = "Try adjusting your search or filters"
                    )
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


@Composable
private fun StatusFilterChips(
    selectedStatus: DriverStatus?,
    onStatusSelected: (DriverStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedStatus == null,
                onClick = { onStatusSelected(null) },
                label = { Text("All") }
            )
        }
        items(DriverStatus.entries.toList()) { status ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(if (selectedStatus == status) null else status) },
                label = { Text(getStatusDisplayName(status)) }
            )
        }
    }
}

private fun getStatusDisplayName(status: DriverStatus): String {
    return when (status) {
        DriverStatus.ACTIVE -> "Active"
        DriverStatus.INACTIVE -> "Inactive"
        DriverStatus.ON_TRIP -> "On Trip"
        DriverStatus.ON_LEAVE -> "On Leave"
        DriverStatus.SUSPENDED -> "Suspended"
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
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${driver.firstName.first()}${driver.lastName.first()}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = driver.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = driver.phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusBadge(status = driver.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DriverInfoItem(
                    emoji = "⭐",
                    label = "Rating",
                    value = "${driver.rating}"
                )
                DriverInfoItem(
                    emoji = "🛣️",
                    label = "Trips",
                    value = "${driver.totalTrips}"
                )
                DriverInfoItem(
                    emoji = "🚗",
                    label = "Vehicle",
                    value = driver.assignedVehicleNumber ?: "None"
                )
            }

            driver.currentLocation?.let { location ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = location.address ?: "Unknown location",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverInfoItem(
    emoji: String,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusBadge(status: DriverStatus) {
    val (color, text) = when (status) {
        DriverStatus.ACTIVE -> MaterialTheme.colorScheme.primary to "Active"
        DriverStatus.INACTIVE -> MaterialTheme.colorScheme.error to "Inactive"
        DriverStatus.ON_TRIP -> MaterialTheme.colorScheme.tertiary to "On Trip"
        DriverStatus.ON_LEAVE -> MaterialTheme.colorScheme.secondary to "On Leave"
        DriverStatus.SUSPENDED -> MaterialTheme.colorScheme.error to "Suspended"
    }

    // Using reusable FleetStatusBadge component
    FleetStatusBadge(
        status = text,
        color = color
    )
}

