package com.ijs.trip.payment.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.pdfreport.handler.PaymentReceiptPdfHandler
import com.indusjs.pdfreport.model.PaymentReceiptPdfData
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.LoadingContent
import com.ijs.trip.payment.domain.entity.TripPayment
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

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

    // PDF Export state
    var pdfExportData by remember { mutableStateOf<PaymentReceiptPdfData?>(null) }
    var isExportingPdf by remember { mutableStateOf(false) }

    // Initialize
    LaunchedEffect(paymentId) {
        viewModel.sendIntent(PaymentDetailContract.Intent.LoadPayment(paymentId))
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PaymentDetailContract.Effect.NavigateBack -> onNavigateBack()
                is PaymentDetailContract.Effect.NavigateToEdit -> onNavigateToEdit(effect.paymentId)
                is PaymentDetailContract.Effect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is PaymentDetailContract.Effect.ShowError -> snackbarHostState.showSnackbar(effect.message)
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
                snackbarHostState.showSnackbar("Receipt exported successfully!")
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
                    pdfExportData = generatePdfData(payment)
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
                        error = state.error ?: "Something went wrong",
                        onRetry = { viewModel.sendIntent(PaymentDetailContract.Intent.Refresh) }
                    )
                }
                state.payment != null -> {
                    PaymentDetailContent(
                        payment = state.payment!!,
                        scrollState = scrollState
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (state.showDeleteConfirmation) {
        DeletePaymentDialog(
            amountDisplay = state.payment?.amountDisplay,
            isDeleting = state.isDeleting,
            onConfirm = { viewModel.sendIntent(PaymentDetailContract.Intent.ConfirmDelete) },
            onDismiss = { viewModel.sendIntent(PaymentDetailContract.Intent.HideDeleteConfirmation) }
        )
    }
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
        title = { Text("Payment Details") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_back),
                    contentDescription = "Back"
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
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            painter = painterResource(Res.drawable.ic_download),
                            contentDescription = "Download Receipt"
                        )
                    }
                }
            }

            if (state.canEdit || state.canDelete) {
                Box {
                    IconButton(onClick = onShowMenu) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_more_vert),
                            contentDescription = "More options"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = onDismissMenu
                    ) {
                        // Download Receipt option in menu
                        state.payment?.let { payment ->
                            DropdownMenuItem(
                                text = { Text("Download Receipt") },
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
                                text = { Text("Edit") },
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
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
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
    scrollState: androidx.compose.foundation.ScrollState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeroSection(payment = payment)
        TripInfoCard(payment = payment)
        PaymentDetailsCard(payment = payment)
        CustomerDetailsCard(payment = payment)
        FinancialInfoCard(payment = payment)
        AdditionalInfoCard(payment = payment)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DeletePaymentDialog(
    amountDisplay: String?,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Payment") },
        text = {
            Text("Are you sure you want to delete this payment of ${amountDisplay ?: ""}? This action cannot be undone.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Generate PDF data from a TripPayment for receipt export.
 */
private fun generatePdfData(payment: TripPayment): PaymentReceiptPdfData {
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
        paymentDate = payment.paymentDate?.take(10) ?: "",
        paymentStatus = payment.paymentStatus.displayName,
        transactionId = payment.transactionId,
        bankName = payment.bankName,
        notes = payment.notes,
        createdBy = payment.createdByName,
        generatedAt = FleetDateTime.formatDisplayDateTime12Hour(FleetDateTime.now())
    )
}
