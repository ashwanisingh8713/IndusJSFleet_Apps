package com.ijs.customer.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.pdfreport.handler.CustomerPaymentsPdfHandler
import com.indusjs.pdfreport.handler.CustomerTripsPdfHandler
import com.indusjs.pdfreport.handler.CustomerFinancialsPdfHandler
import com.indusjs.pdfreport.model.CustomerPaymentsPdfData
import com.indusjs.pdfreport.model.CustomerTripsPdfData
import com.indusjs.pdfreport.model.CustomerFinancialsPdfData
import com.indusjs.uicomponents.components.*
import com.ijs.customer.domain.entity.Customer
import com.ijs.customer.presentation.detail.CustomerDetailContract.CustomerDetailTab
import com.ijs.customer.presentation.detail.CustomerDetailContract.Effect
import com.ijs.customer.presentation.detail.CustomerDetailContract.Intent
import com.ijs.customer.presentation.detail.CustomerDetailContract.ReportType
import com.ijs.customer.presentation.detail.CustomerDetailContract.State
import com.ijs.customer.presentation.detail.components.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import indusjsfleet.ijs_ui_components_lib.generated.resources.*

/**
 * Customer Detail Screen with Tabs.
 * Shows customer info, trips, pending payments, received payments, and financial report.
 * Pending, Payments, and Financials tabs are only visible to Owner/GM.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    viewModel: CustomerDetailViewModel,
    customerId: String,
    onNavigateBack: () -> Unit,
    onNavigateToTrip: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // PDF export state
    var tripsPdfData by remember { mutableStateOf<CustomerTripsPdfData?>(null) }
    var paymentsPdfData by remember { mutableStateOf<CustomerPaymentsPdfData?>(null) }
    var financialsPdfData by remember { mutableStateOf<CustomerFinancialsPdfData?>(null) }

    LaunchedEffect(customerId) {
        viewModel.sendIntent(Intent.LoadCustomer(customerId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.NavigateBack -> onNavigateBack()
                is Effect.ShowSnackbar -> scope.launch { snackbarHostState.showSnackbar(effect.message) }
                is Effect.PdfExported -> scope.launch { snackbarHostState.showSnackbar("Report exported: ${effect.filePath}") }
                is Effect.PdfExportError -> scope.launch { snackbarHostState.showSnackbar(effect.message) }
                is Effect.ExportHtml -> {
                    // Handle HTML export - show message for now
                    scope.launch { snackbarHostState.showSnackbar("Report ready: ${effect.fileName}") }
                }
                is Effect.ExportTripsPdf -> {
                    tripsPdfData = effect.pdfData
                }
                is Effect.ExportPaymentsPdf -> {
                    paymentsPdfData = effect.pdfData
                }
                is Effect.ExportFinancialsPdf -> {
                    financialsPdfData = effect.pdfData
                }
            }
        }
    }

    // Customer Trips PDF Export Handler
    CustomerTripsPdfHandler(
        pdfData = tripsPdfData,
        onExportComplete = {
            tripsPdfData = null
        },
        onExportError = { error ->
            tripsPdfData = null
            scope.launch { snackbarHostState.showSnackbar(error) }
        }
    )

    // Customer Payments PDF Export Handler
    CustomerPaymentsPdfHandler(
        pdfData = paymentsPdfData,
        onExportComplete = {
            paymentsPdfData = null
        },
        onExportError = { error ->
            paymentsPdfData = null
            scope.launch { snackbarHostState.showSnackbar(error) }
        }
    )

    // Customer Financials PDF Export Handler
    CustomerFinancialsPdfHandler(
        pdfData = financialsPdfData,
        onExportComplete = {
            financialsPdfData = null
            scope.launch { snackbarHostState.showSnackbar("Financial report exported successfully!") }
        },
        onExportError = { error ->
            financialsPdfData = null
            scope.launch { snackbarHostState.showSnackbar(error) }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Edit Customer" else "Customer Details") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.isEditMode) {
                            viewModel.sendIntent(Intent.CancelEdit)
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            painter = painterResource(if (state.isEditMode) Res.drawable.ic_close else Res.drawable.ic_arrow_back),
                            contentDescription = if (state.isEditMode) "Cancel" else "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    if (!state.isEditMode && state.customer != null) {
                        IconButton(onClick = { viewModel.sendIntent(Intent.ToggleEditMode) }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_edit),
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (state.isEditMode) {
                EditModeBottomBar(
                    canSave = state.canSave,
                    isSaving = state.isSaving,
                    onCancel = { viewModel.sendIntent(Intent.CancelEdit) },
                    onSave = { viewModel.sendIntent(Intent.SaveChanges) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            state.isLoading -> LoadingContent()
            state.error != null && state.customer == null -> ErrorContent(
                error = state.error!!,
                onRetry = { viewModel.sendIntent(Intent.LoadCustomer(customerId)) }
            )
            state.customer == null -> EmptyContent(
                title = "Customer not found",
                icon = "🏢"
            )
            state.isEditMode -> EditCustomerContent(
                state = state,
                onIntent = viewModel::sendIntent,
                modifier = Modifier.padding(paddingValues)
            )
            else -> CustomerDetailTabbedContent(
                state = state,
                onIntent = viewModel::sendIntent,
                onNavigateToTrip = onNavigateToTrip,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerDetailTabbedContent(
    state: State,
    onIntent: (Intent) -> Unit,
    onNavigateToTrip: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleTabs = state.visibleTabs
    val pagerState = rememberPagerState(
        initialPage = visibleTabs.indexOf(state.selectedTab).coerceAtLeast(0),
        pageCount = { visibleTabs.size }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        val selectedTab = visibleTabs.getOrNull(pagerState.currentPage) ?: CustomerDetailTab.OVERVIEW
        if (selectedTab != state.selectedTab) {
            onIntent(Intent.SelectTab(selectedTab))
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 8.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
        ) {
            visibleTabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(tab.icon)
                            Text(tab.title)
                        }
                    }
                )
            }
        }

        // Pager Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val tab = visibleTabs.getOrNull(page) ?: CustomerDetailTab.OVERVIEW
            when (tab) {
                CustomerDetailTab.OVERVIEW -> OverviewTabContent(
                    state = state,
                    onIntent = onIntent
                )
                CustomerDetailTab.TRIPS -> TripsTabContent(
                    state = state,
                    onIntent = onIntent,
                    onTripClick = onNavigateToTrip,
                    onExportPdf = { onIntent(Intent.ExportPdf(ReportType.TRIPS)) }
                )
                CustomerDetailTab.PAYMENTS -> PaymentsTabContent(
                    state = state,
                    onIntent = onIntent,
                    onExportPdf = { onIntent(Intent.ExportPdf(ReportType.PAYMENTS)) }
                )
                CustomerDetailTab.FINANCIALS -> FinancialsTabContent(
                    state = state,
                    onIntent = onIntent,
                    onExportPdf = { onIntent(Intent.ExportPdf(ReportType.FINANCIALS)) }
                )
            }
        }
    }
}

@Composable
private fun OverviewTabContent(
    state: State,
    onIntent: (Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Error message if any
        state.error?.let { error ->
            ErrorCard(error)
        }

        // Hero Card
        CustomerHeroCard(state.customer!!, state.isSaving)

        // Contact Details
        CustomerContactCard(state.customer!!)

        // Business Details
        CustomerBusinessCard(state.customer!!)
    }
}

@Composable
private fun ErrorCard(error: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚠️", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CustomerHeroCard(
    customer: Customer,
    isSaving: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = customer.companyName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(customer.isActive, isSaving)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👤", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = customer.personName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    isActive: Boolean,
    isSaving: Boolean
) {
    val bgColor = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val textColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        if (isSaving) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
        } else {
            Text(
                text = if (isActive) "● Active" else "● Inactive",
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


@Composable
private fun CustomerContactCard(customer: Customer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📞 Contact Details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            ClickablePhoneRow(
                label = "Primary",
                phoneNumber = customer.primaryContact
            )

            customer.secondaryContact?.takeIf { it.isNotBlank() }?.let { secondary ->
                ClickablePhoneRow(
                    label = "Secondary",
                    phoneNumber = secondary
                )
            }

            customer.email?.takeIf { it.isNotBlank() }?.let { email ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📧", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerBusinessCard(customer: Customer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🏢 Business Details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            customer.gstNumber?.takeIf { it.isNotBlank() }?.let { gst ->
                DetailRow("GST Number", gst)
            }

            customer.companyAddress?.takeIf { it.isNotBlank() }?.let { address ->
                DetailRow("Address", address)
            }

            customer.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                DetailRow("Notes", notes)
            }

            if (customer.gstNumber.isNullOrBlank() && customer.companyAddress.isNullOrBlank() && customer.notes.isNullOrBlank()) {
                Text(
                    text = "No additional business details",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EditModeBottomBar(
    canSave: Boolean,
    isSaving: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onSave,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = canSave,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Updating...")
                } else {
                    Text("Update Customer", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun EditCustomerContent(
    state: State,
    onIntent: (Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Error message
        state.error?.let { ErrorCard(it) }

        // Company Details Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🏢 Company Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                OutlinedTextField(
                    value = state.companyName,
                    onValueChange = { onIntent(Intent.UpdateCompanyName(it)) },
                    label = { Text("Company Name *") },
                    isError = state.companyNameError != null,
                    supportingText = state.companyNameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = state.personName,
                    onValueChange = { onIntent(Intent.UpdatePersonName(it)) },
                    label = { Text("Contact Person *") },
                    isError = state.personNameError != null,
                    supportingText = state.personNameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Contact Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "📞 Contact Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                FleetMobileField(
                    rawValue = state.primaryContact,
                    onRawValueChange = { onIntent(Intent.UpdatePrimaryContact(it)) },
                    label = "Primary Mobile *",
                    isError = state.primaryContactError != null,
                    errorMessage = state.primaryContactError
                )

                FleetMobileField(
                    rawValue = state.secondaryContact,
                    onRawValueChange = { onIntent(Intent.UpdateSecondaryContact(it)) },
                    label = "Secondary Mobile",
                    isError = state.secondaryContactError != null,
                    errorMessage = state.secondaryContactError
                )

                FleetEmailField(
                    value = state.email,
                    onValueChange = { onIntent(Intent.UpdateEmail(it)) },
                    label = "Email",
                    isError = state.emailError != null,
                    errorMessage = state.emailError
                )
            }
        }

        // Business Details Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "📋 Business Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                OutlinedTextField(
                    value = state.gstNumber,
                    onValueChange = { onIntent(Intent.UpdateGstNumber(it)) },
                    label = { Text("GST Number") },
                    placeholder = { Text("22AAAAA0000A1Z5") },
                    isError = state.gstNumberError != null,
                    supportingText = state.gstNumberError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = state.companyAddress,
                    onValueChange = { onIntent(Intent.UpdateCompanyAddress(it)) },
                    label = { Text("Address") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { onIntent(Intent.UpdateNotes(it)) },
                    label = { Text("Notes") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Status Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Customer Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (state.customer?.isActive == true) "Currently active" else "Currently inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.customer?.isActive ?: true,
                    onCheckedChange = { onIntent(Intent.ToggleStatus) }
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
