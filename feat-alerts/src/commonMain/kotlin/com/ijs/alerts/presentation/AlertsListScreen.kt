package com.ijs.alerts.presentation

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.fleet.domain.entity.dashboard.Alert
import com.indusjs.fleet.domain.entity.dashboard.AlertPriority
import com.indusjs.fleet.domain.entity.dashboard.AlertType
import com.indusjs.fleet.domain.entity.dashboard.AlertsSummary
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

/**
 * Alerts List Screen - Shows all alerts with filtering options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsListScreen(
    viewModel: AlertsListViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AlertsListContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is AlertsListContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is AlertsListContract.Effect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alerts") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.sendIntent(AlertsListContract.Intent.NavigateBack) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.sendIntent(AlertsListContract.Intent.RefreshAlerts) },
                        enabled = !state.isRefreshing
                    ) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                painter = painterResource(Res.drawable.ic_refresh),
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
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
        when {
            state.isLoading -> {
                LoadingContent(message = "Loading alerts...")
            }
            state.error != null && !state.hasAlerts -> {
                ErrorContent(
                    error = state.error!!,
                    screenContext = FleetErrorContext.DASHBOARD,
                    onRetry = { viewModel.sendIntent(AlertsListContract.Intent.LoadAlerts) }
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Summary Card
                    AlertsSummaryCard(
                        summary = state.alertsSummary,
                        modifier = Modifier.padding(16.dp)
                    )

                    // Filter Chips
                    AlertFilterChips(
                        selectedFilter = state.selectedFilter,
                        alertsSummary = state.alertsSummary,
                        onFilterSelected = { viewModel.sendIntent(AlertsListContract.Intent.ChangeFilter(it)) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Alerts List
                    if (state.filteredAlerts.isEmpty()) {
                        EmptyContent(
                            title = "No Alerts",
                            message = when (state.selectedFilter) {
                                AlertsListContract.AlertFilter.ALL -> "All clear! No alerts at this time."
                                AlertsListContract.AlertFilter.CRITICAL -> "No critical alerts"
                                AlertsListContract.AlertFilter.WARNING -> "No warning alerts"
                                AlertsListContract.AlertFilter.INFO -> "No info alerts"
                                AlertsListContract.AlertFilter.DOCUMENTS -> "No document expiry alerts"
                                AlertsListContract.AlertFilter.LICENSES -> "No license expiry alerts"
                            },
                            icon = "✅"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.filteredAlerts,
                                key = { it.id }
                            ) { alert ->
                                AlertItemCard(
                                    alert = alert,
                                    onDismiss = { viewModel.sendIntent(AlertsListContract.Intent.DismissAlert(alert.id)) }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertsSummaryCard(
    summary: AlertsSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alert Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${summary.totalAlerts} Total",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Priority breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (summary.criticalAlerts > 0) {
                    SummaryChip(
                        count = summary.criticalAlerts,
                        label = "Critical",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (summary.warningAlerts > 0) {
                    SummaryChip(
                        count = summary.warningAlerts,
                        label = "Warning",
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (summary.infoAlerts > 0) {
                    SummaryChip(
                        count = summary.infoAlerts,
                        label = "Info",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Detailed breakdown
            if (summary.documentExpired > 0 || summary.documentExpiring7Days > 0 ||
                summary.licenseExpired > 0 || summary.licenseExpiring7Days > 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📄 Documents",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (summary.documentExpired > 0) {
                            Text(
                                text = "${summary.documentExpired} Expired",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (summary.documentExpiring7Days > 0) {
                            Text(
                                text = "${summary.documentExpiring7Days} Expiring soon",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📋 Licenses",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (summary.licenseExpired > 0) {
                            Text(
                                text = "${summary.licenseExpired} Expired",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (summary.licenseExpiring7Days > 0) {
                            Text(
                                text = "${summary.licenseExpiring7Days} Expiring soon",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AlertFilterChips(
    selectedFilter: AlertsListContract.AlertFilter,
    alertsSummary: AlertsSummary,
    onFilterSelected: (AlertsListContract.AlertFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedFilter == AlertsListContract.AlertFilter.ALL,
                onClick = { onFilterSelected(AlertsListContract.AlertFilter.ALL) },
                label = { Text("All (${alertsSummary.totalAlerts})") },
                leadingIcon = { Text("📋") }
            )
        }
        if (alertsSummary.criticalAlerts > 0) {
            item {
                FilterChip(
                    selected = selectedFilter == AlertsListContract.AlertFilter.CRITICAL,
                    onClick = { onFilterSelected(AlertsListContract.AlertFilter.CRITICAL) },
                    label = { Text("Critical (${alertsSummary.criticalAlerts})") },
                    leadingIcon = { Text("🔴") }
                )
            }
        }
        if (alertsSummary.warningAlerts > 0) {
            item {
                FilterChip(
                    selected = selectedFilter == AlertsListContract.AlertFilter.WARNING,
                    onClick = { onFilterSelected(AlertsListContract.AlertFilter.WARNING) },
                    label = { Text("Warning (${alertsSummary.warningAlerts})") },
                    leadingIcon = { Text("🟠") }
                )
            }
        }
        item {
            FilterChip(
                selected = selectedFilter == AlertsListContract.AlertFilter.DOCUMENTS,
                onClick = { onFilterSelected(AlertsListContract.AlertFilter.DOCUMENTS) },
                label = { Text("Documents") },
                leadingIcon = { Text("📄") }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == AlertsListContract.AlertFilter.LICENSES,
                onClick = { onFilterSelected(AlertsListContract.AlertFilter.LICENSES) },
                label = { Text("Licenses") },
                leadingIcon = { Text("📋") }
            )
        }
    }
}

@Composable
private fun AlertItemCard(
    alert: Alert,
    onDismiss: () -> Unit
) {
    val alertColor = when (alert.type) {
        AlertType.MAINTENANCE -> MaterialTheme.colorScheme.tertiary
        AlertType.FUEL_LOW -> MaterialTheme.colorScheme.error
        AlertType.SPEED_VIOLATION -> MaterialTheme.colorScheme.error
        AlertType.GEOFENCE_VIOLATION -> MaterialTheme.colorScheme.secondary
        AlertType.DRIVER_BEHAVIOR -> MaterialTheme.colorScheme.secondary
        AlertType.SYSTEM -> MaterialTheme.colorScheme.primary
        AlertType.DOCUMENT_EXPIRY -> MaterialTheme.colorScheme.error
        AlertType.LICENSE_EXPIRY -> MaterialTheme.colorScheme.error
    }

    val alertIcon = when (alert.type) {
        AlertType.DOCUMENT_EXPIRY -> "📄"
        AlertType.LICENSE_EXPIRY -> "📋"
        AlertType.MAINTENANCE -> "🔧"
        AlertType.FUEL_LOW -> "⛽"
        else -> "⚠️"
    }

    val priorityColor = when (alert.priority) {
        AlertPriority.CRITICAL -> MaterialTheme.colorScheme.error
        AlertPriority.WARNING -> Color(0xFFFF9800)
        AlertPriority.INFO -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = alertColor.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Alert icon in colored box
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(alertColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = alertIcon,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Column {
                        // Title with priority badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = alert.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = alertColor
                            )
                            // Priority badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = priorityColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = alert.priority.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = priorityColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Days until expiry badge
                        alert.daysUntilExpiry?.let { days ->
                            val badgeColor = when {
                                days < 0 -> MaterialTheme.colorScheme.error
                                days <= 7 -> Color(0xFFFF9800)
                                else -> MaterialTheme.colorScheme.primary
                            }
                            val badgeText = when {
                                days < 0 -> "${-days} days overdue"
                                days == 0 -> "Expires today"
                                days == 1 -> "Expires tomorrow"
                                else -> "$days days left"
                            }
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = badgeColor
                            )
                        }
                    }
                }

                // Dismiss button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Entity info (vehicle registration or driver name)
            val vehicleRegNumber = alert.vehicleRegistrationNumber
            if (alert.type == AlertType.DOCUMENT_EXPIRY && vehicleRegNumber != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "🚛", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = vehicleRegNumber,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            val driverNameValue = alert.driverName
            if (alert.type == AlertType.LICENSE_EXPIRY && driverNameValue != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "👤", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = driverNameValue,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Message
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

