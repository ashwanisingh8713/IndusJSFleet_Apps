package com.indusjs.fleet.presentation.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.pdfreport.handler.PaymentReceiptPdfHandler
import com.indusjs.pdfreport.model.PaymentReceiptPdfData
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.core.util.rememberPhoneDialer
import com.indusjs.fleet.domain.entity.payment.PaymentStatus
import com.indusjs.fleet.domain.entity.payment.TripPayment
import indusjsfleet.sharedui.generated.resources.*
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

    // Helper function to generate PDF data
    fun generatePdfData(payment: TripPayment): PaymentReceiptPdfData {
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

    Scaffold(
        topBar = {
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
                            onClick = {
                                isExportingPdf = true
                                pdfExportData = generatePdfData(payment)
                            },
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
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_more_vert),
                                    contentDescription = "More options"
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
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
                                            showMenu = false
                                            isExportingPdf = true
                                            pdfExportData = generatePdfData(payment)
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
                                            showMenu = false
                                            viewModel.sendIntent(PaymentDetailContract.Intent.NavigateToEdit)
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
                                            showMenu = false
                                            viewModel.sendIntent(PaymentDetailContract.Intent.ShowDeleteConfirmation)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
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
        AlertDialog(
            onDismissRequest = { viewModel.sendIntent(PaymentDetailContract.Intent.HideDeleteConfirmation) },
            title = { Text("Delete Payment") },
            text = {
                Text("Are you sure you want to delete this payment of ${state.payment?.amountDisplay}? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.sendIntent(PaymentDetailContract.Intent.ConfirmDelete) },
                    enabled = !state.isDeleting
                ) {
                    if (state.isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.sendIntent(PaymentDetailContract.Intent.HideDeleteConfirmation) }) {
                    Text("Cancel")
                }
            }
        )
    }
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
        // Hero Section - Amount and Status
        HeroSection(payment = payment)

        // Trip Info Card
        TripInfoCard(payment = payment)

        // Payment Details Card
        PaymentDetailsCard(payment = payment)

        // Customer Details Card
        CustomerDetailsCard(payment = payment)

        // Financial Info Card
        FinancialInfoCard(payment = payment)

        // Additional Info Card
        AdditionalInfoCard(payment = payment)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HeroSection(payment: TripPayment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top row: Amount + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = payment.amountDisplay,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (payment.hasTds || payment.hasDiscount) {
                        Text(
                            text = "Net: ${payment.netAmountDisplay}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                PaymentStatusBadge(status = payment.paymentStatus)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Middle row: Receipt + Date + Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Receipt number
                payment.receiptNumber?.let { receipt ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = receipt,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Payment Mode with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = payment.modeIcon,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = payment.modeDisplay,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                // Date
                payment.paymentDate?.let { date ->
                    Text(
                        text = formatDisplayDate(date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Payment type
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${payment.paymentType.icon} ${payment.typeDisplay} Payment",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun TripInfoCard(payment: TripPayment) {
    val tripInfo = payment.tripInfo ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "Trip Information",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Vehicle & Driver in single row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tripInfo.vehicleRegistration?.let { vehicle ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Vehicle",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = vehicle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                tripInfo.driverName?.let { driver ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Driver",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = driver,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Route - compact format
            if (tripInfo.startLocation != null || tripInfo.endLocation != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tripInfo.startLocation?.take(20) ?: "N/A",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = " → ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = tripInfo.endLocation?.take(20) ?: "N/A",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Trip price
            tripInfo.tripPrice?.let { price ->
                if (price > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Trip Price",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = tripInfo.tripPriceDisplay,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentDetailsCard(payment: TripPayment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "Payment Details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompactDetailRow(label = "Amount", value = payment.amountDisplay)

            if (payment.hasTds) {
                CompactDetailRow(label = "TDS Deducted", value = payment.tdsDisplay)
            }

            if (payment.hasDiscount) {
                CompactDetailRow(label = "Discount", value = payment.discountDisplay)
            }

            CompactDetailRow(
                label = "Net Amount",
                value = payment.netAmountDisplay,
                isHighlighted = true
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            CompactDetailRow(label = "Type", value = "${payment.paymentType.icon} ${payment.typeDisplay}")
            CompactDetailRow(label = "Mode", value = "${payment.modeIcon} ${payment.modeDisplay}")

            payment.transactionId?.let {
                CompactDetailRow(label = "Transaction ID", value = it)
            }

            payment.bankName?.let {
                CompactDetailRow(label = "Bank", value = it)
            }
        }
    }
}

@Composable
private fun CustomerDetailsCard(payment: TripPayment) {
    val hasCustomerInfo = !payment.customerName.isNullOrBlank() ||
            !payment.customerContact.isNullOrBlank() ||
            !payment.customerCompany.isNullOrBlank()

    if (!hasCustomerInfo) return

    // Phone dialer for customer contact
    val phoneDialer = payment.customerContact?.let { rememberPhoneDialer(it) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "Customer",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            payment.customerName?.let {
                CompactDetailRow(label = "Name", value = it)
            }

            payment.customerCompany?.let {
                // Only show if different from name
                if (it != payment.customerName) {
                    CompactDetailRow(label = "Company", value = it)
                }
            }

            // Contact with calling functionality
            payment.customerContact?.let { contact ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .then(
                            if (phoneDialer != null) {
                                Modifier.clickable { phoneDialer() }
                            } else {
                                Modifier
                            }
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Contact",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = contact,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (phoneDialer != null) {
                            Text(
                                text = "📞",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            payment.customerGst?.let {
                CompactDetailRow(label = "GST", value = it)
            }
        }
    }
}

@Composable
private fun FinancialInfoCard(payment: TripPayment) {
    val hasFinancialInfo = !payment.financialYear.isNullOrBlank() ||
            !payment.financialMonth.isNullOrBlank()

    if (!hasFinancialInfo) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Financial Info",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                payment.financialYear?.let {
                    Text(
                        text = "FY: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                payment.financialMonth?.let {
                    Text(
                        text = "Month: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AdditionalInfoCard(payment: TripPayment) {
    val hasAdditionalInfo = !payment.notes.isNullOrBlank() ||
            !payment.receivedBy.isNullOrBlank() ||
            !payment.receivedAtLocation.isNullOrBlank() ||
            !payment.createdByName.isNullOrBlank()

    if (!hasAdditionalInfo) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "Additional Info",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            payment.notes?.let {
                CompactDetailRow(label = "Notes", value = it)
            }

            payment.receivedBy?.let {
                CompactDetailRow(label = "Received By", value = it)
            }

            payment.receivedAtLocation?.let {
                CompactDetailRow(label = "Location", value = it)
            }

            payment.createdByName?.let {
                CompactDetailRow(label = "Created By", value = it)
            }

            payment.createdAt?.let {
                CompactDetailRow(label = "Created", value = formatDisplayDate(it))
            }
        }
    }
}

@Composable
private fun CompactDetailRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlighted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}


@Composable
private fun PaymentStatusBadge(
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        PaymentStatus.RECEIVED -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        PaymentStatus.PENDING -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        PaymentStatus.CANCELLED -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

/**
 * Format ISO date to display format: "DD-MMM-YYYY hh:mm AM/PM"
 */
private fun formatDisplayDate(isoDate: String): String {
    return FleetDateTime.formatIsoToDisplayDateTime12Hour(isoDate)
}
