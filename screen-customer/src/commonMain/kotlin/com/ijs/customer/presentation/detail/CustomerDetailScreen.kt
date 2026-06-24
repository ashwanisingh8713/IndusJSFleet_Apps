package com.ijs.customer.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.pdfreport.handler.CustomerPaymentsPdfHandler
import com.indusjs.pdfreport.handler.CustomerTripsPdfHandler
import com.indusjs.pdfreport.handler.CustomerFinancialsPdfHandler
import com.indusjs.pdfreport.model.CustomerPaymentsPdfData
import com.indusjs.pdfreport.model.CustomerTripsPdfData
import com.indusjs.pdfreport.model.CustomerFinancialsPdfData
import com.indusjs.uicomponents.components.*
import com.ijs.customer.presentation.localizedTitle
import com.ijs.customer.presentation.detail.CustomerDetailContract.CustomerDetailTab
import com.ijs.customer.presentation.detail.CustomerDetailContract.Effect
import com.ijs.customer.presentation.detail.CustomerDetailContract.Intent
import com.ijs.customer.presentation.detail.CustomerDetailContract.ReportType
import com.ijs.customer.presentation.detail.CustomerDetailContract.State
import com.ijs.customer.presentation.detail.components.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import indusjsfleet.ijs_ui_components_lib.generated.resources.*

/**
 * Customer Detail Screen with Tabs.
 * Shows customer info, trips, pending payments, received payments, and financial report.
 * Pending, Payments, and Financials tabs are only visible to Owner/GM.
 */
private sealed interface CustomerDetailPendingSnackbar {
    data class Resource(
        val res: StringResource,
        val formatArgs: List<Any> = emptyList()
    ) : CustomerDetailPendingSnackbar

    data class Raw(val message: String) : CustomerDetailPendingSnackbar

    data class Text(val uiText: UiText) : CustomerDetailPendingSnackbar
}

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
    var pendingSnackbar by remember { mutableStateOf<CustomerDetailPendingSnackbar?>(null) }

    pendingSnackbar?.let { pending ->
        val message = when (pending) {
            is CustomerDetailPendingSnackbar.Resource -> {
                if (pending.formatArgs.isEmpty()) {
                    stringResource(pending.res)
                } else {
                    stringResource(pending.res, *pending.formatArgs.toTypedArray())
                }
            }
            is CustomerDetailPendingSnackbar.Raw -> pending.message
            is CustomerDetailPendingSnackbar.Text -> pending.uiText.resolve()
        }
        LaunchedEffect(pending) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

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
                is Effect.ShowSnackbar -> {
                    pendingSnackbar = CustomerDetailPendingSnackbar.Text(effect.message)
                }
                is Effect.PdfExported -> {
                    pendingSnackbar = CustomerDetailPendingSnackbar.Resource(
                        Res.string.customer_snackbar_report_exported,
                        listOf(effect.filePath)
                    )
                }
                is Effect.PdfExportError -> {
                    pendingSnackbar = CustomerDetailPendingSnackbar.Raw(effect.message)
                }
                is Effect.ExportHtml -> {
                    pendingSnackbar = CustomerDetailPendingSnackbar.Resource(
                        Res.string.customer_snackbar_report_ready,
                        listOf(effect.fileName)
                    )
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
            pendingSnackbar = CustomerDetailPendingSnackbar.Raw(error)
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
            pendingSnackbar = CustomerDetailPendingSnackbar.Raw(error)
        }
    )

    // Customer Financials PDF Export Handler
    CustomerFinancialsPdfHandler(
        pdfData = financialsPdfData,
        onExportComplete = {
            financialsPdfData = null
            pendingSnackbar = CustomerDetailPendingSnackbar.Resource(Res.string.customer_snackbar_financial_export_success)
        },
        onExportError = { error ->
            financialsPdfData = null
            pendingSnackbar = CustomerDetailPendingSnackbar.Raw(error)
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditMode) {
                            stringResource(Res.string.customer_edit)
                        } else {
                            stringResource(Res.string.customers_detail)
                        }
                    )
                },
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
                            contentDescription = if (state.isEditMode) {
                                stringResource(Res.string.cancel)
                            } else {
                                stringResource(Res.string.back)
                            },
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
                                contentDescription = stringResource(Res.string.edit),
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
                error = state.error!!.resolve(),
                onRetry = { viewModel.sendIntent(Intent.LoadCustomer(customerId)) }
            )
            state.customer == null -> EmptyContent(
                title = stringResource(Res.string.customer_not_found),
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
        FleetTabBar(
            tabs = visibleTabs.mapIndexed { index, tab ->
                FleetTab(
                    id = index,
                    label = "${tab.icon} ${tab.localizedTitle()}"
                )
            },
            selectedTabId = pagerState.currentPage,
            onTabSelected = { index ->
                scope.launch { pagerState.animateScrollToPage(index) }
            }
        )

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

