package com.ijs.customer.presentation.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.LoadingContent
import com.ijs.customer.domain.entity.Customer
import com.ijs.customer.presentation.list.CustomersListContract.Effect
import com.ijs.customer.presentation.list.CustomersListContract.Intent
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
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
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.NavigateToCustomerDetail -> onNavigateToCustomerDetail(effect.customerId)
                is Effect.NavigateToCreateCustomer -> onNavigateToCreateCustomer()
                is Effect.ShowSnackbar -> scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Customers")
                        if (state.customers.isNotEmpty()) {
                            Text(
                                text = "${state.customers.size} total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    // Show loading indicator or refresh button
                    if (state.isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 4.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    } else {
                        IconButton(onClick = { viewModel.sendIntent(Intent.RefreshCustomers) }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_refresh),
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
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
                    onClick = { viewModel.sendIntent(Intent.OnAddCustomerClick) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = "Add Customer"
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.sendIntent(Intent.UpdateSearchQuery(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Content
            when {
                state.isLoading && state.customers.isEmpty() && !state.isInitialLoadComplete -> {
                    LoadingContent(message = "Loading customers...")
                }
                state.showError -> {
                    ErrorContent(
                        error = state.error ?: "Unable to load customers",
                        onRetry = { viewModel.sendIntent(Intent.RefreshCustomers) }
                    )
                }
                state.isEmpty -> {
                    EmptyContent(
                        icon = "👥",
                        title = if (state.searchQuery.isNotBlank())
                            "No Customers Found"
                        else
                            "No Customers Yet",
                        message = if (state.searchQuery.isNotBlank())
                            "No customers found matching \"${state.searchQuery}\""
                        else
                            "Add your first customer to get started",
                        actionLabel = "Add Customer",
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

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search customers...") },
        leadingIcon = {
            Icon(
                painter = painterResource(Res.drawable.ic_search),
                contentDescription = null
            )
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun CustomersList(
    customers: List<Customer>,
    isLoading: Boolean,
    onCustomerClick: (Customer) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(customers, key = { it.id }) { customer ->
            CustomerCard(
                customer = customer,
                onClick = { onCustomerClick(customer) }
            )
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }

    // Load more when reaching end
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= customers.size - 3) {
                    onLoadMore()
                }
            }
    }
}

@Composable
private fun CustomerCard(
    customer: Customer,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusChip(isActive = customer.isActive)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contact Person
            Text(
                text = "Contact: ${customer.personName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Phone
            Text(
                text = "📞 ${customer.primaryContact}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // GST Number if available
            customer.gstNumber?.let { gst ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "GST: $gst",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusChip(isActive: Boolean) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isActive)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = if (isActive) "Active" else "Inactive",
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

