package com.indusjs.fleet.presentation.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.FinanceColors
import com.indusjs.fleet.core.ui.FleetSectionCard
import com.indusjs.fleet.core.ui.FleetTextField
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.domain.entity.finance.*
import com.indusjs.fleet.presentation.finance.VehicleFinanceContract.Effect
import com.indusjs.fleet.presentation.finance.VehicleFinanceContract.Intent
import indusjsfleet.sharedui.generated.resources.*
import org.jetbrains.compose.resources.painterResource

// Use FinanceColors from core.ui
private val LoanBlue = FinanceColors.LoanBlue
private val CashGreen = FinanceColors.CashGreen
private val WarningOrange = FinanceColors.WarningOrange
private val CriticalRed = FinanceColors.CriticalRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleFinanceDetailScreen(
    vehicleId: Int,
    viewModel: VehicleFinanceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vehicleId) {
        viewModel.sendIntent(Intent.SelectVehicle(vehicleId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is Effect.NavigateBack -> onNavigateBack()
                is Effect.PaymentRecorded -> {
                    viewModel.sendIntent(Intent.SelectVehicle(vehicleId))
                }
                else -> { /* Ignore */ }
            }
        }
    }

    val purchase = state.selectedPurchase
    val vehicle = state.vehicles.find { (it.id.toIntOrNull() ?: 0) == vehicleId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Finance Details")
                        vehicle?.let {
                            Text(
                                text = "${it.registrationNumber} - ${it.make} ${it.model}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        // Determine if we should show loading:
        // - isLoading is true (API call in progress) OR
        // - Data hasn't been fetched for this vehicle yet (selectedVehicleId is different)
        // BUT NOT when we already have data for this vehicle
        val hasDataForThisVehicle = state.selectedVehicleId == vehicleId

        when {
            state.isLoading -> LoadingContent(modifier = Modifier.padding(paddingValues))
            !hasDataForThisVehicle -> LoadingContent(modifier = Modifier.padding(paddingValues))
            state.error != null -> ErrorContent(
                error = state.error ?: "Something went wrong",
                onRetry = { viewModel.sendIntent(Intent.SelectVehicle(vehicleId)) },
                modifier = Modifier.padding(paddingValues)
            )
            purchase == null -> NoPurchaseInfoContent(
                onAddClick = { viewModel.sendIntent(Intent.NavigateToAddPurchase) },
                modifier = Modifier.padding(paddingValues)
            )
            else -> FinanceDetailContent(
                purchase = purchase,
                loanSummary = state.loanSummary,
                payments = state.loanPayments,
                onRecordPaymentClick = { viewModel.sendIntent(Intent.ShowRecordPaymentSheet) },
                onViewHistoryClick = onNavigateToHistory,
                onPaymentClick = { payment -> viewModel.sendIntent(Intent.ShowPaymentDetail(payment)) },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }

    // Record Payment BottomSheet
    if (state.showRecordPaymentSheet && state.selectedPurchase != null) {
        RecordPaymentBottomSheet(
            purchase = state.selectedPurchase!!,
            amount = state.paymentAmount,
            date = state.paymentDate,
            mode = state.paymentMode,
            source = state.paymentSource,
            transactionRef = state.transactionRef,
            lateFee = state.lateFee,
            notes = state.paymentNotes,
            amountError = state.paymentAmountError,
            dateError = state.paymentDateError,
            isSaving = state.isSaving,
            onAmountChange = { viewModel.sendIntent(Intent.UpdatePaymentAmount(it)) },
            onDateChange = { viewModel.sendIntent(Intent.UpdatePaymentDate(it)) },
            onModeChange = { viewModel.sendIntent(Intent.UpdatePaymentMode(it)) },
            onSourceChange = { viewModel.sendIntent(Intent.UpdatePaymentSource(it)) },
            onRefChange = { viewModel.sendIntent(Intent.UpdateTransactionRef(it)) },
            onLateFeeChange = { viewModel.sendIntent(Intent.UpdateLateFee(it)) },
            onNotesChange = { viewModel.sendIntent(Intent.UpdatePaymentNotes(it)) },
            onRecord = { viewModel.sendIntent(Intent.RecordPayment) },
            onDismiss = { viewModel.sendIntent(Intent.HideRecordPaymentSheet) }
        )
    }

    // Payment Detail BottomSheet
    if (state.showPaymentDetailSheet && state.selectedPaymentForDetail != null) {
        PaymentDetailBottomSheet(
            payment = state.selectedPaymentForDetail!!,
            onDismiss = { viewModel.sendIntent(Intent.HidePaymentDetail) }
        )
    }
}

@Composable
private fun NoPurchaseInfoContent(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("⚪", style = MaterialTheme.typography.displayLarge)
            Text(
                text = "No purchase information",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Add purchase details to track this vehicle's finance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onAddClick) {
                Text("Add Purchase Info")
            }
        }
    }
}

@Composable
private fun FinanceDetailContent(
    purchase: VehiclePurchase,
    loanSummary: LoanSummary?,
    payments: List<LoanPayment>,
    onRecordPaymentClick: () -> Unit,
    onViewHistoryClick: () -> Unit,
    onPaymentClick: (LoanPayment) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Purchase Summary
        item {
            FleetSectionCard(title = "💰 Purchase Summary") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("Purchase Date", purchase.purchaseDate)
                    DetailRow("Purchase Price", formatCurrency(purchase.purchasePrice))
                    purchase.vendorName?.let { DetailRow("Vendor", it) }
                    purchase.invoiceNumber?.let { DetailRow("Invoice", it) }
                    DetailRow(
                        "Payment Type",
                        if (purchase.isFinanced) "🏦 Loan" else "💵 Cash"
                    )
                }
            }
        }

        // Loan Details (only for financed vehicles)
        if (purchase.isFinanced) {
            item {
                FleetSectionCard(title = "🏦 Loan Details") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailRow("Financier", purchase.financierName ?: "N/A")
                        purchase.loanAccountNumber?.let { DetailRow("Account Number", it) }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DetailRow("Down Payment", "${formatCurrency(purchase.downPayment)} (${purchase.downPaymentPercent}%)")
                        DetailRow("Loan Amount", formatCurrency(purchase.loanAmount))
                        DetailRow("Interest Rate", "${purchase.interestRate}% per annum")
                        DetailRow("Tenure", "${purchase.tenureMonths} months")
                        DetailRow("Monthly EMI", formatCurrency(purchase.emiAmount), valueColor = LoanBlue)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DetailRow("Total Interest", formatCurrency(purchase.totalInterest), valueColor = CriticalRed)
                        DetailRow("Total Payable", formatCurrency(purchase.totalPayable), valueColor = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Loan Progress
            item {
                FleetSectionCard(title = "📊 Loan Progress") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Progress bar
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${purchase.loanProgressPercent}% Complete",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LoanBlue
                                )
                                Text(
                                    text = "${purchase.emisPaid}/${purchase.tenureMonths} EMIs",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { purchase.loanProgressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = LoanBlue,
                                trackColor = LoanBlue.copy(alpha = 0.2f)
                            )
                        }

                        // Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                value = purchase.emisPaid.toString(),
                                label = "EMIs Paid",
                                subValue = formatCurrency(purchase.totalPaid),
                                color = CashGreen
                            )
                            StatItem(
                                value = purchase.emisRemaining.toString(),
                                label = "Remaining",
                                subValue = "EMIs",
                                color = WarningOrange
                            )
                            StatItem(
                                value = formatCurrency(purchase.outstandingBalance),
                                label = "Outstanding",
                                color = CriticalRed
                            )
                        }

                        // Loan Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            val (statusColor, statusLabel) = when (purchase.loanStatus) {
                                LoanStatus.ACTIVE -> CashGreen to "🟢 Active"
                                LoanStatus.CLOSED -> LoanBlue to "✅ Closed"
                                LoanStatus.DEFAULTED -> CriticalRed to "🔴 Defaulted"
                                else -> Color.Gray to "N/A"
                            }
                            Text(
                                text = "Loan Status: $statusLabel",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Next EMI
            if (purchase.loanStatus == LoanStatus.ACTIVE) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Single row with Next EMI, Due Date, Amount - equal spacing
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Next EMI",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "#${purchase.emisPaid + 1}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Due Date",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = getNextEmiDueDate(purchase),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Amount",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatCurrency(purchase.emiAmount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = LoanBlue
                                    )
                                }
                            }

                            // Record Payment button - full width
                            Button(
                                onClick = onRecordPaymentClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Record Payment")
                            }
                        }
                    }
                }
            }

            // Payment History - Only show PAID payments
            item {
                val paidPayments = payments.filter { it.isPaid }

                FleetSectionCard(
                    title = "Payment History",
                    trailingAction = if (paidPayments.isNotEmpty()) {
                        { TextButton(onClick = onViewHistoryClick) { Text("View All (${paidPayments.size})") } }
                    } else null
                ) {
                    if (paidPayments.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No payments recorded yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Record your first EMI payment to track history",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Show only first 3 payments in summary view
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            paidPayments.take(3).forEach { payment ->
                                PaymentHistoryItem(
                                    payment = payment,
                                    onClick = { onPaymentClick(payment) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bank Details (if available)
        if (purchase.bankName != null || purchase.bankAccountNumber != null) {
            item {
                FleetSectionCard(title = "🏦 Bank Details") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        purchase.bankName?.let { DetailRow("Bank Name", it) }
                        purchase.bankAccountNumber?.let { DetailRow("Account Number", it) }
                        purchase.bankIfsc?.let { DetailRow("IFSC Code", it) }
                        DetailRow(
                            "Auto-debit",
                            if (purchase.autoDebitEnabled) "✅ Enabled" else "❌ Disabled"
                        )
                    }
                }
            }
        }

        // Notes
        purchase.notes?.let { notes ->
            if (notes.isNotBlank()) {
                item {
                    FleetSectionCard(title = "📝 Notes") {
                        Text(notes)
                    }
                }
            }
        }

        // Export Button
        item {
            OutlinedButton(
                onClick = { /* TODO: Export PDF */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("📄 Export PDF Report")
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    subValue: String? = null,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        subValue?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PaymentHistoryItem(
    payment: LoanPayment,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = payment.emiLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatDueDateDisplay(payment.paymentDate ?: payment.dueDate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = formatCurrency(payment.amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = CashGreen
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordPaymentBottomSheet(
    purchase: VehiclePurchase,
    amount: String,
    date: String,
    mode: PaymentMode?,
    source: String,
    transactionRef: String,
    lateFee: String,
    notes: String,
    amountError: String?,
    dateError: String?,
    isSaving: Boolean,
    onAmountChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onModeChange: (PaymentMode?) -> Unit,
    onSourceChange: (String) -> Unit,
    onRefChange: (String) -> Unit,
    onLateFeeChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onRecord: () -> Unit,
    onDismiss: () -> Unit
) {
    var showModeDropdown by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Record EMI Payment",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Vehicle and EMI info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🚛 ${purchase.vehicle?.registrationNumber ?: "Vehicle"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "EMI #${purchase.emisPaid + 1}  |  Due: ${formatDueDateDisplay(purchase.nextEmiDueDate)}  |  Amount: ${formatCurrency(purchase.emiAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // Amount
            FleetTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = "Amount Paid *",
                placeholder = purchase.emiAmount.toString(),
                leadingIcon = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = amountError != null,
                errorMessage = amountError
            )

            // Payment Date
            FleetDateTimePicker(
                date = date,
                time = "",
                onDateTimeChange = { newDate, _ -> onDateChange(newDate) },
                mode = PickerMode.DATE_ONLY,
                label = "Payment Date *",
                isError = dateError != null,
                errorMessage = dateError
            )

            // Payment Mode
            ExposedDropdownMenuBox(
                expanded = showModeDropdown,
                onExpandedChange = { showModeDropdown = it }
            ) {
                OutlinedTextField(
                    value = mode?.label ?: "",
                    onValueChange = {},
                    label = { Text("Payment Mode") },
                    placeholder = { Text("Select mode") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showModeDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = showModeDropdown,
                    onDismissRequest = { showModeDropdown = false }
                ) {
                    PaymentMode.entries.forEach { paymentMode ->
                        DropdownMenuItem(
                            text = { Text(paymentMode.label) },
                            onClick = {
                                onModeChange(paymentMode)
                                showModeDropdown = false
                            }
                        )
                    }
                }
            }

            // Transaction Ref
            FleetTextField(
                value = transactionRef,
                onValueChange = onRefChange,
                label = "Transaction Ref/UTR",
                placeholder = "UTR123456789"
            )

            // Payment Source
            FleetTextField(
                value = source,
                onValueChange = onSourceChange,
                label = "Payment Source",
                placeholder = "e.g., HDFC NetBanking"
            )

            // Late Fee
            FleetTextField(
                value = lateFee,
                onValueChange = onLateFeeChange,
                label = "Late Fee (if any)",
                placeholder = "0",
                leadingIcon = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Notes") },
                placeholder = { Text("Additional notes...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Info text
            Text(
                text = "ℹ️ This records a payment you've already made to the bank",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = onRecord,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Record Payment")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Get the next EMI due date for display.
 *
 * Always calculates from loanStartDate + emisPaid months to ensure accuracy.
 * The API's nextEmiDueDate might contain stale data.
 */
private fun getNextEmiDueDate(purchase: VehiclePurchase): String {
    // Calculate from loanStartDate + emisPaid months
    val loanStartDate = purchase.loanStartDate
    if (!loanStartDate.isNullOrBlank()) {
        val startParsed = FleetDateTime.fromIso8601(loanStartDate)
        if (startParsed != null) {
            val startDateStr = FleetDateTime.formatDate(startParsed)
            val nextDueDateStr = FleetDateTime.addMonths(startDateStr, purchase.emisPaid)
            if (nextDueDateStr != null) {
                return formatDueDateDisplay(nextDueDateStr)
            }
        }
    }

    // Fallback to API-provided nextEmiDueDate
    if (!purchase.nextEmiDueDate.isNullOrBlank()) {
        return formatDueDateDisplay(purchase.nextEmiDueDate)
    }

    return "N/A"
}

/**
 * Format date for display using ijs-datetime-utils.
 * Converts ISO 8601, YYYY-MM-DD, or DD-MM-YYYY format to "07 Apr 2024" format.
 */
private fun formatDueDateDisplay(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "N/A"

    // Try to parse ISO format first (e.g., "2026-01-15T00:00:00Z")
    val parsed = FleetDateTime.fromIso8601(dateString)
    if (parsed != null) {
        val day = parsed.day.toString().padStart(2, '0')
        val monthName = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[parsed.month - 1]
        return "$day $monthName ${parsed.year}"
    }

    val parts = dateString.split("-")
    if (parts.size == 3) {
        return try {
            // Check if YYYY-MM-DD format (year first, 4 digits)
            if (parts[0].length == 4) {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                val monthName = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[month - 1]
                "${day.toString().padStart(2, '0')} $monthName $year"
            } else {
                // DD-MM-YYYY format
                val day = parts[0].toInt()
                val month = parts[1].toInt()
                val year = parts[2].toInt()
                val monthName = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[month - 1]
                "${day.toString().padStart(2, '0')} $monthName $year"
            }
        } catch (e: Exception) {
            dateString
        }
    }

    return dateString
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentDetailBottomSheet(
    payment: LoanPayment,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Payment Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Amount Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CashGreen.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formatCurrency(payment.amount),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = CashGreen
                    )
                    Text(
                        text = payment.emiLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // Payment Details
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("Payment Date", formatDueDateDisplay(payment.paymentDate))
                payment.dueDate?.let {
                    DetailRow("Due Date", formatDueDateDisplay(it))
                }
                payment.paymentMode?.let {
                    DetailRow("Payment Mode", it.label)
                }
                payment.transactionRef?.let {
                    DetailRow("Transaction Ref", it)
                }
                payment.paymentSource?.let {
                    DetailRow("Payment Source", it)
                }

                if (payment.lateFee > 0) {
                    DetailRow("Late Fee", formatCurrency(payment.lateFee), valueColor = CriticalRed)
                }

                if (payment.principalAmount > 0 || payment.interestAmount > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    if (payment.principalAmount > 0) {
                        DetailRow("Principal", formatCurrency(payment.principalAmount))
                    }
                    if (payment.interestAmount > 0) {
                        DetailRow("Interest", formatCurrency(payment.interestAmount))
                    }
                }
            }

            // Notes
            payment.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    HorizontalDivider()
                    Column {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
