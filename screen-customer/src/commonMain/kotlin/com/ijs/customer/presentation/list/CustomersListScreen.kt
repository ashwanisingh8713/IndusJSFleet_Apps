package com.ijs.customer.presentation.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetSearchField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetStatusBadge
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAtLeastMedium
import com.indusjs.uicomponents.theme.isExpanded
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.customer.domain.entity.Customer
import com.ijs.customer.presentation.list.CustomersListContract.Effect
import com.ijs.customer.presentation.list.CustomersListContract.Intent
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import indusjsfleet.ijs_ui_components_lib.generated.resources.*

/**
 * Customers List Screen.
 * Displays all customers with search and filter capabilities.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersListScreen(
    viewModel: CustomersListViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCustomerDetail: (String) -> Unit,
    onNavigateToCreateCustomer: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.NavigateToCustomerDetail -> onNavigateToCustomerDetail(effect.customerId)
                is Effect.NavigateToCreateCustomer -> onNavigateToCreateCustomer()
                is Effect.ShowSnackbar -> pendingSnackbar = effect.message
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(Res.string.customers_title))
                        if (state.customers.isNotEmpty()) {
                            Text(
                                text = stringResource(Res.string.customers_total_count, state.customers.size),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Show loading indicator or refresh button
                    if (state.isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(FleetTokens.IconSize.Default)
                                .padding(end = FleetTokens.Spacing.XS),
                            strokeWidth = FleetTokens.Height.ProgressStroke,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))
                    } else {
                        IconButton(onClick = { viewModel.sendIntent(Intent.RefreshCustomers) }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_refresh),
                                contentDescription = stringResource(Res.string.refresh),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(FleetTokens.IconSize.Default)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Only show FAB when there's content (not empty and not loading)
            if (!state.isLoading && state.customers.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.sendIntent(Intent.OnAddCustomerClick) }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = stringResource(Res.string.customers_add),
                        modifier = Modifier.size(FleetTokens.IconSize.Default)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.sendIntent(Intent.RefreshCustomers) },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                FleetSearchField(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.sendIntent(Intent.UpdateSearchQuery(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = FleetTokens.Spacing.ScreenHorizontal,
                            vertical = FleetTokens.Spacing.S
                        ),
                    placeholder = stringResource(Res.string.customers_search_placeholder)
                )

                // Content
                when {
                    state.isLoading && state.customers.isEmpty() && !state.isInitialLoadComplete -> {
                        LoadingContent(message = stringResource(Res.string.loading))
                    }
                    state.showError -> {
                        ErrorContent(
                            error = state.error?.resolve() ?: stringResource(Res.string.customers_unable_to_load),
                            onRetry = { viewModel.sendIntent(Intent.RefreshCustomers) }
                        )
                    }
                    state.isEmpty -> {
                        EmptyContent(
                            iconRes = Res.drawable.ic_team,
                            title = if (state.searchQuery.isNotBlank())
                                stringResource(Res.string.no_data_for_filter)
                            else
                                stringResource(Res.string.customers_empty_title),
                            message = if (state.searchQuery.isNotBlank())
                                stringResource(Res.string.team_no_results_message)
                            else
                                stringResource(Res.string.customers_empty_message),
                            actionLabel = stringResource(Res.string.customers_add),
                            onAction = { viewModel.sendIntent(Intent.OnAddCustomerClick) }
                        )
                    }
                    else -> {
                        CustomersList(
                            customers = state.displayedCustomers,
                            isLoading = state.isLoading,
                            onCustomerClick = { viewModel.sendIntent(Intent.OnCustomerClick(it)) },
                            onLoadMore = { viewModel.sendIntent(Intent.LoadMore) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomersList(
    customers: List<Customer>,
    isLoading: Boolean,
    onCustomerClick: (Customer) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

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
            state = listState,
            modifier = contentWidthModifier.fillMaxHeight().align(Alignment.TopCenter),
            contentPadding = PaddingValues(FleetTokens.Spacing.L),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            if (columns == 1) {
                items(customers, key = { it.id }) { customer ->
                    CustomerCard(
                        customer = customer,
                        onClick = { onCustomerClick(customer) }
                    )
                }
            } else {
                val rows = customers.chunked(columns)
                items(rows, key = { row -> row.first().id }) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                    ) {
                        row.forEach { customer ->
                            CustomerCard(
                                customer = customer,
                                onClick = { onCustomerClick(customer) },
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

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(FleetTokens.Spacing.L),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(FleetTokens.IconSize.Default))
                    }
                }
            }
        }

        // Load more when reaching the end. The number of LazyColumn item slots depends
        // on the layout (one per customer when 1-up, one per row when 2-up), so the
        // threshold is computed from the row count to stay correct in both modes.
        val itemSlots = (customers.size + columns - 1) / columns
        LaunchedEffect(listState, itemSlots) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { lastVisibleIndex ->
                    if (lastVisibleIndex != null && lastVisibleIndex >= itemSlots - 2) {
                        onLoadMore()
                    }
                }
        }
    }
}

@Composable
private fun CustomerCard(
    customer: Customer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FleetSectionCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header with company name and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.companyName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                StatusChip(isActive = customer.isActive)
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

            // Contact Person
            Text(
                text = stringResource(Res.string.customer_label_contact, customer.personName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))

            // Phone
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_phone),
                    contentDescription = null,
                    modifier = Modifier.size(FleetTokens.IconSize.S),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = customer.primaryContact,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // GST Number if available
            customer.gstNumber?.let { gst ->
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
                Text(
                    text = stringResource(Res.string.customer_label_gst_prefix, gst),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusChip(isActive: Boolean) {
    FleetStatusBadge(
        status = stringResource(
            if (isActive) Res.string.customer_status_active else Res.string.customer_status_inactive
        ),
        color = if (isActive)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.error
    )
}
