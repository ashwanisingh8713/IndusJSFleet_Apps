package com.ijs.trip.payment.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.pdfreport.handler.PaymentReceiptPdfHandler
import com.indusjs.pdfreport.model.PaymentReceiptPdfData
import com.indusjs.uicomponents.components.DeleteConfirmationDialog
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.trip.payment.domain.entity.TripPayment
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Payment Detail Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDetailScreen(
    viewModel: PaymentDetailViewModel,
    paymentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val receiptExportedMessage = stringResource(Res.string.payment_receipt_exported)
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    // PDF Export state
    var pdfExportData by remember { mutableStateOf<PaymentReceiptPdfData?>(null) }
    var isExportingPdf by remember { mutableStateOf(false) }

    // Initialize
    LaunchedEffect(paymentId) {
        viewModel.sendIntent(PaymentDetailContract.Intent.LoadPayment(paymentId))
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PaymentDetailContract.Effect.NavigateBack -> onNavigateBack()
                is PaymentDetailContract.Effect.NavigateToEdit -> onNavigateToEdit(effect.paymentId)
                is PaymentDetailContract.Effect.ShowSnackbar -> pendingSnackbar = effect.message
                is PaymentDetailContract.Effect.ShowError -> pendingSnackbar = effect.message
                is PaymentDetailContract.Effect.PaymentDeleted -> { /* Handled in NavigateBack */ }
            }
        }
    }

    // PDF Export Handler
    PaymentReceiptPdfHandler(
        pdfData = pdfExportData,
        onExportComplete = {
            isExportingPdf = false
            pdfExportData = null
            scope.launch {
                snackbarHostState.showSnackbar(receiptExportedMessage)
            }
        },
        onExportError = { error ->
            isExportingPdf = false
            pdfExportData = null
            scope.launch {
                snackbarHostState.showSnackbar(error)
            }
        }
    )

    Scaffold(
        topBar = {
            PaymentDetailTopBar(
                state = state,
                isExportingPdf = isExportingPdf,
                showMenu = showMenu,
                onNavigateBack = onNavigateBack,
                onShowMenu = { showMenu = true },
                onDismissMenu = { showMenu = false },
                onDownloadReceipt = { payment ->
                    isExportingPdf = true
                    pdfExportData = generatePdfData(payment, state.paymentStateLabels)
                },
                onEdit = { viewModel.sendIntent(PaymentDetailContract.Intent.NavigateToEdit) },
                onDelete = { viewModel.sendIntent(PaymentDetailContract.Intent.ShowDeleteConfirmation) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    LoadingContent()
                }
                state.error != null -> {
                    ErrorContent(
                        error = state.error?.resolve() ?: stringResource(Res.string.error_generic),
                        onRetry = { viewModel.sendIntent(PaymentDetailContract.Intent.Refresh) }
                    )
                }
                state.payment != null -> {
                    PaymentDetailContent(
                        payment = state.payment!!,
                        scrollState = scrollState,
                        paymentStateLabels = state.paymentStateLabels
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    DeleteConfirmationDialog(
        showDialog = state.showDeleteConfirmation,
        entityName = stringResource(Res.string.payment_entity_singular),
        entityDetail = state.payment?.amountDisplay,
        isLoading = state.isDeleting,
        onConfirmDelete = { viewModel.sendIntent(PaymentDetailContract.Intent.ConfirmDelete) },
        onDismiss = { viewModel.sendIntent(PaymentDetailContract.Intent.HideDeleteConfirmation) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentDetailTopBar(
    state: PaymentDetailContract.State,
    isExportingPdf: Boolean,
    showMenu: Boolean,
    onNavigateBack: () -> Unit,
    onShowMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onDownloadReceipt: (TripPayment) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = { Text(stringResource(Res.string.payments_detail)) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_back),
                    contentDescription = stringResource(Res.string.back)
                )
            }
        },
        actions = {
            // Download Receipt button
            state.payment?.let { payment ->
                IconButton(
                    onClick = { onDownloadReceipt(payment) },
                    enabled = !isExportingPdf
                ) {
                    if (isExportingPdf) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(FleetTokens.IconSize.M),
                            strokeWidth = FleetTokens.Height.ProgressStroke
                        )
                    } else {
                        Icon(
                            painter = painterResource(Res.drawable.ic_download),
                            contentDescription = stringResource(Res.string.action_download_receipt)
                        )
                    }
                }
            }

            if (state.canEdit || state.canDelete) {
                Box {
                    IconButton(onClick = onShowMenu) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_more_vert),
                            contentDescription = stringResource(Res.string.payment_more_options)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = onDismissMenu
                    ) {
                        // Download Receipt option in menu
                        state.payment?.let { payment ->
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.payment_cd_download_receipt)) },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_download),
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    onDismissMenu()
                                    onDownloadReceipt(payment)
                                }
                            )
                        }

                        if (state.canEdit) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.edit)) },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_edit),
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    onDismissMenu()
                                    onEdit()
                                }
                            )
                        }
                        if (state.canDelete) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.delete), color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_delete),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    onDismissMenu()
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun PaymentDetailContent(
    payment: TripPayment,
    scrollState: androidx.compose.foundation.ScrollState,
    paymentStateLabels: Map<String, String> = emptyMap()
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val bp = rememberFleetBreakpoint()
        // Detail content is centered and width-capped on wider screens.
        val contentWidthModifier = if (bp == FleetBreakpoint.Compact) {
            Modifier.fillMaxSize()
        } else {
            Modifier
                .fillMaxSize()
                .widthIn(max = FleetTokens.Width.MaxContent)
                .align(Alignment.TopCenter)
        }

        Column(
            modifier = contentWidthModifier
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(
                    horizontal = FleetTokens.Spacing.L,
                    vertical = FleetTokens.Spacing.M
                ),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            HeroSection(payment = payment, paymentStateLabels = paymentStateLabels)
            TripInfoCard(payment = payment)
            PaymentDetailsCard(payment = payment)
            CustomerDetailsCard(payment = payment)
            FinancialInfoCard(payment = payment)
            AdditionalInfoCard(payment = payment)
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
        }
    }
}

/**
 * Generate PDF data from a TripPayment for receipt export.
 */
private fun generatePdfData(
    payment: TripPayment,
    paymentStateLabels: Map<String, String> = emptyMap()
): PaymentReceiptPdfData {
    return PaymentReceiptPdfData(
        paymentId = payment.id.toIntOrNull() ?: 0,
        receiptNumber = payment.receiptNumber ?: "N/A",
        tripId = payment.tripId.toIntOrNull() ?: 0,
        vehicleNumber = payment.tripInfo?.vehicleRegistration ?: "N/A",
        driverName = payment.tripInfo?.driverName,
        customerName = payment.customerName ?: "N/A",
        customerCompany = payment.customerCompany,
        customerContact = payment.customerContact,
        startLocation = payment.tripInfo?.startLocation ?: "N/A",
        endLocation = payment.tripInfo?.endLocation ?: "N/A",
        tripDate = payment.tripInfo?.tripStartDate ?: "",
        amount = payment.amount,
        tdsAmount = payment.tdsAmount,
        discountAmount = payment.discountAmount,
        netAmount = payment.netAmount,
        paymentType = payment.typeDisplay,
        paymentMode = payment.modeDisplay,
        paymentDate = com.indusjs.fleet.core.util.formatDateToHumanReadable(payment.paymentDate),
        paymentStatus = paymentStateLabels[payment.paymentStatus.apiValue] ?: payment.paymentStatus.displayName,
        transactionId = payment.transactionId,
        bankName = payment.bankName,
        notes = payment.notes,
        createdBy = payment.createdByName,
        generatedAt = FleetDateTime.formatDisplayDateTime12Hour(FleetDateTime.now())
    )
}
