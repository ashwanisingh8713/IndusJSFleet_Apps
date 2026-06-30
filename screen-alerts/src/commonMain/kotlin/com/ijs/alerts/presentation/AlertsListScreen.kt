package com.ijs.alerts.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FilterDefinition
import com.indusjs.uicomponents.components.FleetFilterBar
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAtLeastMedium
import com.indusjs.uicomponents.theme.isExpanded
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.indusjs.fleet.domain.entity.dashboard.Alert
import com.indusjs.fleet.domain.entity.dashboard.AlertPriority
import com.indusjs.fleet.domain.entity.dashboard.AlertType
import com.indusjs.fleet.domain.entity.dashboard.AlertsSummary
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Vector icon for each alert type (no emoji). */
private fun alertIconRes(type: AlertType): DrawableResource = when (type) {
    AlertType.DOCUMENT_EXPIRY -> Res.drawable.ic_folder
    AlertType.MISSING_DOCUMENTS -> Res.drawable.ic_edit
    AlertType.LICENSE_EXPIRY -> Res.drawable.ic_driver
    AlertType.MAINTENANCE -> Res.drawable.ic_wrench
    AlertType.FUEL_LOW -> Res.drawable.ic_fuel
    else -> Res.drawable.ic_warning
}

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
                title = { Text(stringResource(Res.string.alerts_title)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.sendIntent(AlertsListContract.Intent.NavigateBack) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
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
                                modifier = Modifier.size(FleetTokens.IconSize.M),
                                strokeWidth = FleetTokens.Height.ProgressStroke
                            )
                        } else {
                            Icon(
                                painter = painterResource(Res.drawable.ic_refresh),
                                contentDescription = stringResource(Res.string.refresh),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(FleetTokens.IconSize.Default)
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
                LoadingContent(message = stringResource(Res.string.alerts_loading))
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
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

                    val alertFilters = buildList {
                        val summary = state.alertsSummary
                        if (summary.criticalAlerts > 0) {
                            add(
                                FilterDefinition(
                                    id = AlertsListContract.AlertFilter.CRITICAL,
                                    label = stringResource(Res.string.dashboard_label_critical),
                                    count = summary.criticalAlerts
                                )
                            )
                        }
                        if (summary.warningAlerts > 0) {
                            add(
                                FilterDefinition(
                                    id = AlertsListContract.AlertFilter.WARNING,
                                    label = stringResource(Res.string.dashboard_label_warning),
                                    count = summary.warningAlerts
                                )
                            )
                        }
                        add(
                            FilterDefinition(
                                id = AlertsListContract.AlertFilter.DOCUMENTS,
                                label = stringResource(Res.string.alerts_documents)
                            )
                        )
                        add(
                            FilterDefinition(
                                id = AlertsListContract.AlertFilter.LICENSES,
                                label = stringResource(Res.string.alerts_licenses)
                            )
                        )
                    }
                    FleetFilterBar(
                        filters = alertFilters,
                        selectedFilterId = when (state.selectedFilter) {
                            AlertsListContract.AlertFilter.ALL -> null
                            else -> state.selectedFilter
                        },
                        onFilterSelected = { id ->
                            viewModel.sendIntent(
                                AlertsListContract.Intent.ChangeFilter(
                                    id ?: AlertsListContract.AlertFilter.ALL
                                )
                            )
                        },
                        allLabel = stringResource(
                            Res.string.alerts_filter_all,
                            state.alertsSummary.totalAlerts
                        ),
                        allCount = null,
                        modifier = Modifier.padding(horizontal = FleetTokens.Spacing.L)
                    )

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

                    // Alerts List
                    if (state.filteredAlerts.isEmpty()) {
                        EmptyContent(
                            title = stringResource(Res.string.alerts_empty_title),
                            message = when (state.selectedFilter) {
                                AlertsListContract.AlertFilter.ALL -> stringResource(Res.string.alerts_all_clear)
                                AlertsListContract.AlertFilter.CRITICAL -> stringResource(Res.string.alerts_no_alerts_for_filter, stringResource(Res.string.dashboard_label_critical).lowercase())
                                AlertsListContract.AlertFilter.WARNING -> stringResource(Res.string.alerts_no_alerts_for_filter, stringResource(Res.string.dashboard_label_warning).lowercase())
                                AlertsListContract.AlertFilter.INFO -> stringResource(Res.string.alerts_no_alerts_for_filter, stringResource(Res.string.dashboard_label_info).lowercase())
                                AlertsListContract.AlertFilter.DOCUMENTS -> stringResource(Res.string.alerts_no_alerts_for_filter, stringResource(Res.string.alerts_documents).lowercase())
                                AlertsListContract.AlertFilter.LICENSES -> stringResource(Res.string.alerts_no_alerts_for_filter, stringResource(Res.string.alerts_licenses).lowercase())
                            },
                            iconRes = Res.drawable.ic_check_circle
                        )
                    } else {
                        // Responsive list: 1-up on compact, 2-up on medium/expanded.
                        // On expanded the grid is capped to a readable width and centered.
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val breakpoint = rememberFleetBreakpoint()
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
                                    items(
                                        items = state.filteredAlerts,
                                        key = { it.id }
                                    ) { alert ->
                                        AlertItemCard(
                                            alert = alert,
                                            onDismiss = { viewModel.sendIntent(AlertsListContract.Intent.DismissAlert(alert.id)) }
                                        )
                                    }
                                } else {
                                    val rows = state.filteredAlerts.chunked(columns)
                                    items(rows, key = { row -> row.first().id }) { row ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                                        ) {
                                            row.forEach { alert ->
                                                AlertItemCard(
                                                    alert = alert,
                                                    onDismiss = { viewModel.sendIntent(AlertsListContract.Intent.DismissAlert(alert.id)) },
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

                                item {
                                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertItemCard(
    alert: Alert,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
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
        AlertType.MISSING_DOCUMENTS -> MaterialTheme.colorScheme.error
    }

    val priorityColor = when (alert.priority) {
        AlertPriority.CRITICAL -> MaterialTheme.colorScheme.error
        AlertPriority.WARNING -> FleetStatusColors.FleetMaintenance
        AlertPriority.INFO -> MaterialTheme.colorScheme.primary
    }

    // Flat card with a crisp colored outline instead of a shadow — a shadowElevation on a tinted
    // surface renders as a muddy grey halo in light theme, which looked bad.
    FleetSectionCard(
        modifier = modifier,
        containerColor = alertColor.copy(alpha = 0.06f),
        border = BorderStroke(FleetTokens.Border.Default, alertColor.copy(alpha = 0.35f)),
        elevation = FleetTokens.Elevation.None
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M),
                    modifier = Modifier.weight(1f)
                ) {
                    // Alert icon in colored box
                    Box(
                        modifier = Modifier
                            .size(FleetTokens.Height.ButtonMedium)
                            .clip(RoundedCornerShape(FleetTokens.Radius.ML))
                            .background(alertColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(alertIconRes(alert.type)),
                            contentDescription = null,
                            tint = alertColor,
                            modifier = Modifier.size(FleetTokens.IconSize.M)
                        )
                    }

                    Column {
                        // Title with priority badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                        ) {
                            Text(
                                text = alert.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = alertColor
                            )
                            // Priority badge
                            Surface(
                                shape = RoundedCornerShape(FleetTokens.Radius.S),
                                color = priorityColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = alert.priority.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = priorityColor,
                                    modifier = Modifier.padding(horizontal = FleetTokens.Spacing.XS, vertical = FleetTokens.Spacing.XXS)
                                )
                            }
                        }

                        // Days until expiry badge
                        alert.daysUntilExpiry?.let { days ->
                            val badgeColor = when {
                                days < 0 -> MaterialTheme.colorScheme.error
                                days <= 7 -> FleetStatusColors.FleetMaintenance
                                else -> MaterialTheme.colorScheme.primary
                            }
                            val badgeText = when {
                                days < 0 -> stringResource(Res.string.alerts_days_overdue_long, -days)
                                days == 0 -> stringResource(Res.string.alerts_expires_today)
                                days == 1 -> stringResource(Res.string.alerts_expires_tomorrow)
                                else -> stringResource(Res.string.alerts_days_left_long, days)
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
                    modifier = Modifier.size(FleetTokens.Height.MinTouchTarget)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = stringResource(Res.string.alerts_dismiss),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FleetTokens.IconSize.M)
                    )
                }
            }

            // Entity info (vehicle registration or driver name)
            val vehicleRegNumber = alert.vehicleRegistrationNumber
            if ((alert.type == AlertType.DOCUMENT_EXPIRY || alert.type == AlertType.MISSING_DOCUMENTS) && vehicleRegNumber != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_truck),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FleetTokens.IconSize.S)
                    )
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
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_driver),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FleetTokens.IconSize.S)
                    )
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
