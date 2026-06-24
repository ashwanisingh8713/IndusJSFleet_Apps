package com.ijs.finance.presentation

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.datetimeutils.FleetEpoch
import com.indusjs.fleet.core.util.formatDateToHumanReadable
import com.indusjs.uicomponents.components.DropdownOption
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FinanceColors
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetDropdown
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.UiText
import com.indusjs.fleet.core.util.formatCurrency
import com.ijs.finance.domain.entity.*
import com.ijs.vehicle.domain.entity.Vehicle
import com.ijs.finance.presentation.VehicleFinanceContract.Effect
import com.ijs.finance.presentation.VehicleFinanceContract.Intent
import com.indusjs.pdfreport.handler.VehicleFinancePdfHandler
import com.indusjs.pdfreport.model.EmiPaymentPdfItem
import com.indusjs.pdfreport.model.VehicleFinancePdfData
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }
    var pdfExportData by remember { mutableStateOf<VehicleFinancePdfData?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    // PDF Export Handler
    VehicleFinancePdfHandler(
        pdfData = pdfExportData,
        onExportComplete = { pdfExportData = null },
        onExportError = { error ->
            pdfExportData = null
            // Show error via snackbar
        }
    )

    LaunchedEffect(vehicleId) {
        viewModel.sendIntent(Intent.SelectVehicle(vehicleId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackbar -> pendingSnackbar = effect.message
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
    val notAvailableLabel = stringResource(Res.string.not_applicable_short)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(Res.string.finance_detail))
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
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                actions = {
                    if (purchase != null) {
                        IconButton(
                            onClick = {
                                pdfExportData = createVehicleFinancePdfData(
                                    purchase = purchase,
                                    vehicle = vehicle,
                                    payments = state.loanPayments
                                )
                            }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_download),
                                contentDescription = stringResource(Res.string.export_pdf)
                            )
                        }
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
                error = state.error?.resolve() ?: stringResource(Res.string.finance_error_generic),
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
                notAvailableLabel = notAvailableLabel,
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
    EmptyContent(
        icon = "⚪",
        title = stringResource(Res.string.finance_no_purchase_detail_title),
        message = stringResource(Res.string.finance_no_purchase_detail_message),
        actionLabel = stringResource(Res.string.finance_add_purchase),
        onAction = onAddClick,
        modifier = modifier
    )
}

@Composable
private fun FinanceDetailContent(
    purchase: VehiclePurchase,
    loanSummary: LoanSummary?,
    payments: List<LoanPayment>,
    notAvailableLabel: String,
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
            FleetTitledSectionCard(title = stringResource(Res.string.finance_purchase_summary_section)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(stringResource(Res.string.finance_row_purchase_date), formatDueDateDisplay(purchase.purchaseDate))
                    DetailRow(stringResource(Res.string.finance_purchase_price), formatCurrency(purchase.purchasePrice))
                    purchase.vendorName?.let { DetailRow(stringResource(Res.string.finance_label_vendor), it) }
                    purchase.invoiceNumber?.let {
                        DetailRow(stringResource(Res.string.finance_row_invoice_short), it)
                    }
                    DetailRow(
                        stringResource(Res.string.finance_row_payment_type),
                        if (purchase.isFinanced) {
                            stringResource(Res.string.finance_payment_type_loan_label)
                        } else {
                            stringResource(Res.string.finance_payment_type_cash_label)
                        }
                    )
                }
            }
        }

        // Loan Details (only for financed vehicles)
        if (purchase.isFinanced) {
            item {
                FleetTitledSectionCard(title = stringResource(Res.string.finance_section_loan)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailRow(
                            stringResource(Res.string.finance_row_financier),
                            purchase.financierName ?: notAvailableLabel
                        )
                        purchase.loanAccountNumber?.let {
                            DetailRow(stringResource(Res.string.finance_label_account_number), it)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DetailRow(
                            stringResource(Res.string.finance_row_down_payment),
                            "${formatCurrency(purchase.downPayment)} (${purchase.downPaymentPercent}%)"
                        )
                        DetailRow(stringResource(Res.string.finance_row_loan_amount), formatCurrency(purchase.loanAmount))
                        DetailRow(
                            stringResource(Res.string.finance_row_interest_rate),
                            stringResource(Res.string.finance_row_interest_rate_annum, purchase.interestRate.toString())
                        )
                        DetailRow(
                            stringResource(Res.string.finance_row_tenure_plain),
                            stringResource(Res.string.finance_row_tenure_months, purchase.tenureMonths)
                        )
                        DetailRow(
                            stringResource(Res.string.finance_row_monthly_emi_plain),
                            formatCurrency(purchase.emiAmount),
                            valueColor = LoanBlue
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DetailRow(
                            stringResource(Res.string.finance_row_total_interest_plain),
                            formatCurrency(purchase.totalInterest),
                            valueColor = CriticalRed
                        )
                        DetailRow(
                            stringResource(Res.string.finance_row_total_payable_plain),
                            formatCurrency(purchase.totalPayable),
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Loan Progress
            item {
                FleetTitledSectionCard(title = stringResource(Res.string.finance_loan_progress_section)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Progress bar
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(
                                        Res.string.finance_percent_complete,
                                        purchase.loanProgressPercent.toInt()
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LoanBlue
                                )
                                Text(
                                    text = stringResource(
                                        Res.string.finance_emis_slash,
                                        purchase.emisPaid,
                                        purchase.tenureMonths
                                    ),
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
                                label = stringResource(Res.string.finance_stat_emis_paid),
                                subValue = formatCurrency(purchase.totalPaid),
                                color = CashGreen
                            )
                            StatItem(
                                value = purchase.emisRemaining.toString(),
                                label = stringResource(Res.string.finance_stat_remaining),
                                subValue = stringResource(Res.string.finance_stat_emis_sub),
                                color = WarningOrange
                            )
                            StatItem(
                                value = formatCurrency(purchase.outstandingBalance),
                                label = stringResource(Res.string.finance_outstanding_label),
                                color = CriticalRed
                            )
                        }

                        // Loan Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            val statusLabel = when (purchase.loanStatus) {
                                LoanStatus.ACTIVE -> stringResource(Res.string.finance_loan_status_active)
                                LoanStatus.CLOSED -> stringResource(Res.string.finance_loan_status_closed)
                                LoanStatus.DEFAULTED -> stringResource(Res.string.finance_loan_status_defaulted)
                                else -> notAvailableLabel
                            }
                            Text(
                                text = stringResource(Res.string.finance_loan_status_line, statusLabel),
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
                    FleetSectionCard {
                        Column(
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
                                        text = stringResource(Res.string.finance_next_emi_header),
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
                                        text = stringResource(Res.string.finance_row_due_date),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = getNextEmiDueDate(purchase, notAvailableLabel),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = stringResource(Res.string.label_amount),
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
                                Text(stringResource(Res.string.record_payment))
                            }
                        }
                    }
                }
            }

            // Payment History - Only show PAID payments
            item {
                val paidPayments = payments.filter { it.isPaid }

                FleetTitledSectionCard(
                    title = stringResource(Res.string.finance_payment_history),
                    actionLabel = if (paidPayments.isNotEmpty())
                        stringResource(Res.string.view_all_count, paidPayments.size) else null,
                    onActionClick = if (paidPayments.isNotEmpty()) onViewHistoryClick else null
                ) {
                    if (paidPayments.isEmpty()) {
                        EmptyContent(
                            title = stringResource(Res.string.finance_no_payments_yet),
                            message = stringResource(Res.string.finance_record_first_emi_hint),
                            fillMaxSize = false
                        )
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
                FleetTitledSectionCard(title = stringResource(Res.string.finance_bank_details_section)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        purchase.bankName?.let {
                            DetailRow(stringResource(Res.string.finance_label_bank_name), it)
                        }
                        purchase.bankAccountNumber?.let {
                            DetailRow(stringResource(Res.string.finance_label_account_number), it)
                        }
                        purchase.bankIfsc?.let {
                            DetailRow(stringResource(Res.string.finance_label_ifsc), it)
                        }
                        DetailRow(
                            stringResource(Res.string.finance_row_auto_debit),
                            if (purchase.autoDebitEnabled) {
                                stringResource(Res.string.finance_auto_debit_enabled_label)
                            } else {
                                stringResource(Res.string.finance_auto_debit_disabled_label)
                            }
                        )
                    }
                }
            }
        }

        // Notes
        purchase.notes?.let { notes ->
            if (notes.isNotBlank()) {
                item {
                    FleetTitledSectionCard(title = stringResource(Res.string.finance_section_notes)) {
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
                Text(stringResource(Res.string.finance_export_pdf_report))
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
    FleetMetricTile(
        value = value,
        label = label,
        subLabel = subValue,
        accent = color,
        valueColor = color,
        showBackground = false,
        centered = true
    )
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
                text = payment.localizedEmiLabel(),
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
    amountError: UiText?,
    dateError: UiText?,
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
                text = stringResource(Res.string.finance_record_emi_payment_title),
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
                        text = "🚛 ${purchase.vehicle?.registrationNumber ?: stringResource(Res.string.finance_vehicle_fallback)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(
                            Res.string.finance_emi_due_amount_line,
                            purchase.emisPaid + 1,
                            formatDueDateDisplay(purchase.nextEmiDueDate),
                            formatCurrency(purchase.emiAmount)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // Amount
            FleetInputField(
                value = amount,
                onValueChange = onAmountChange,
                fieldType = FieldType.NUMBER,
                label = stringResource(Res.string.finance_label_amount_paid),
                placeholder = purchase.emiAmount.toString(),
                leadingIcon = { Text("₹") },
                isError = amountError != null,
                errorMessage = amountError?.resolve()
            )

            // Payment Date
            FleetDateTimePicker(
                date = date,
                time = "",
                onDateTimeChange = { newDate, _ -> onDateChange(newDate) },
                mode = PickerMode.DATE_ONLY,
                label = stringResource(Res.string.finance_label_payment_date),
                isError = dateError != null,
                errorMessage = dateError?.resolve()
            )

            // Payment Mode
            FleetDropdown(
                label = stringResource(Res.string.finance_label_payment_mode),
                options = PaymentMode.entries.map { paymentMode ->
                    DropdownOption(id = paymentMode, label = paymentMode.localizedLabel())
                },
                selectedOptionId = mode,
                onOptionSelected = onModeChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(Res.string.finance_placeholder_payment_mode)
            )

            // Transaction Ref
            FleetInputField(
                value = transactionRef,
                onValueChange = onRefChange,
                fieldType = FieldType.DEFAULT,
                label = stringResource(Res.string.finance_label_transaction_ref),
                placeholder = stringResource(Res.string.finance_placeholder_transaction_ref)
            )

            // Payment Source
            FleetInputField(
                value = source,
                onValueChange = onSourceChange,
                fieldType = FieldType.DEFAULT,
                label = stringResource(Res.string.finance_label_payment_source),
                placeholder = stringResource(Res.string.finance_placeholder_payment_source)
            )

            // Late Fee
            FleetInputField(
                value = lateFee,
                onValueChange = onLateFeeChange,
                fieldType = FieldType.NUMBER,
                label = stringResource(Res.string.finance_label_late_fee),
                placeholder = stringResource(Res.string.finance_placeholder_late_fee),
                leadingIcon = { Text("₹") }
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text(stringResource(Res.string.finance_label_notes)) },
                placeholder = { Text(stringResource(Res.string.finance_placeholder_additional_notes)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Info text
            Text(
                text = stringResource(Res.string.finance_payment_recorded_bank_info),
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
                    Text(stringResource(Res.string.cancel))
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
                        Text(stringResource(Res.string.record_payment))
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
private fun getNextEmiDueDate(purchase: VehiclePurchase, notAvailableLabel: String): String {
    // Calculate from loanStartDate (epoch-ms) + emisPaid months
    val loanStartDate = purchase.loanStartDate
    if (loanStartDate != null && loanStartDate > 0L) {
        val startValue = FleetEpoch.toValue(loanStartDate)
        if (startValue != null) {
            val startDateStr = FleetDateTime.formatDate(startValue)
            val nextDueDateStr = FleetDateTime.addMonths(startDateStr, purchase.emisPaid)
            if (nextDueDateStr != null) {
                return FleetDateTime.formatAnyToDisplayDate(nextDueDateStr)
            }
        }
    }

    // Fallback to API-provided nextEmiDueDate (epoch-ms)
    val nextDue = purchase.nextEmiDueDate
    if (nextDue != null && nextDue > 0L) {
        return formatDueDateDisplay(nextDue)
    }

    return notAvailableLabel
}

/**
 * Format an epoch-ms timestamp for display ("DD-MMM-YYYY"). Treats null/0 as unset.
 */
private fun formatDueDateDisplay(timestampMillis: Long?): String =
    if (timestampMillis == null || timestampMillis <= 0L) ""
    else formatDateToHumanReadable(timestampMillis)

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
                text = stringResource(Res.string.finance_sheet_payment_details),
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
                        text = payment.localizedEmiLabel(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // Payment Details
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow(
                    stringResource(Res.string.finance_row_payment_date),
                    formatDueDateDisplay(payment.paymentDate)
                )
                payment.dueDate?.let {
                    DetailRow(stringResource(Res.string.finance_row_due_date), formatDueDateDisplay(it))
                }
                payment.paymentMode?.let {
                    DetailRow(stringResource(Res.string.finance_label_payment_mode), it.localizedLabel())
                }
                payment.transactionRef?.let {
                    DetailRow(stringResource(Res.string.finance_label_transaction_ref), it)
                }
                payment.paymentSource?.let {
                    DetailRow(stringResource(Res.string.finance_label_payment_source), it)
                }

                if (payment.lateFee > 0) {
                    DetailRow(
                        stringResource(Res.string.finance_label_late_fee),
                        formatCurrency(payment.lateFee),
                        valueColor = CriticalRed
                    )
                }

                if (payment.principalAmount > 0 || payment.interestAmount > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    if (payment.principalAmount > 0) {
                        DetailRow(
                            stringResource(Res.string.finance_row_principal),
                            formatCurrency(payment.principalAmount)
                        )
                    }
                    if (payment.interestAmount > 0) {
                        DetailRow(
                            stringResource(Res.string.finance_row_interest),
                            formatCurrency(payment.interestAmount)
                        )
                    }
                }
            }

            // Notes
            payment.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    HorizontalDivider()
                    Column {
                        Text(
                            text = stringResource(Res.string.finance_label_notes),
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
                Text(stringResource(Res.string.close))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Create PDF data from vehicle finance details.
 */
private fun createVehicleFinancePdfData(
    purchase: VehiclePurchase,
    vehicle: Vehicle?,
    payments: List<LoanPayment>
): VehicleFinancePdfData {
    val paidEmis = payments.filter { it.isPaid }
    val totalPaid = paidEmis.sumOf { it.amount }
    val remainingAmount = purchase.loanAmount - totalPaid
    val remainingEmis = purchase.tenureMonths - paidEmis.size

    return VehicleFinancePdfData(
        vehicleId = purchase.vehicleId,
        registrationNumber = vehicle?.registrationNumber ?: "N/A",
        vehicleName = "${vehicle?.make ?: ""} ${vehicle?.model ?: ""}".trim(),
        purchaseDate = formatDueDateDisplay(purchase.purchaseDate),
        purchasePrice = purchase.purchasePrice,
        paymentType = purchase.paymentType.name.lowercase(),
        downPayment = purchase.downPayment,
        loanAmount = purchase.loanAmount,
        interestRate = purchase.interestRate,
        tenureMonths = purchase.tenureMonths,
        emiAmount = purchase.emiAmount,
        financierName = purchase.financierName,
        totalPaidAmount = totalPaid,
        remainingAmount = remainingAmount.coerceAtLeast(0.0),
        paidEmisCount = paidEmis.size,
        remainingEmisCount = remainingEmis.coerceAtLeast(0),
        nextEmiDueDate = purchase.nextEmiDueDate?.let { formatDueDateDisplay(it) },
        emiPayments = payments.map { payment ->
            EmiPaymentPdfItem(
                emiNumber = payment.emiNumber ?: 0,
                paymentDate = formatDueDateDisplay(payment.paymentDate),
                amount = payment.amount,
                paymentMode = payment.paymentMode?.label ?: "N/A",
                status = payment.paymentStatus.name.lowercase()
            )
        },
        generatedAt = FleetDateTime.formatDisplayDateTime12Hour(FleetDateTime.now())
    )
}

