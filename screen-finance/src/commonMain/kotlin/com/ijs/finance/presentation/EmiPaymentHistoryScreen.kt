package com.ijs.finance.presentation

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
import com.indusjs.fleet.core.util.formatDateToHumanReadable
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FinanceColors
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isExpanded
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.indusjs.fleet.core.util.formatCurrency
import com.ijs.finance.domain.entity.*
import com.ijs.finance.presentation.VehicleFinanceContract.Intent
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// Use FinanceColors from core.ui
private val LoanBlue = FinanceColors.LoanBlue
private val LoanBlueDark = FinanceColors.LoanBlueDark
private val CashGreen = FinanceColors.CashGreen
private val WarningOrange = FinanceColors.WarningOrange
private val CriticalRed = FinanceColors.CriticalRed
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiPaymentHistoryScreen(
    vehicleId: Int,
    viewModel: VehicleFinanceViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(vehicleId) {
        viewModel.sendIntent(Intent.SelectVehicle(vehicleId))
    }
    val purchase = state.selectedPurchase
    val vehicle = state.vehicles.find { (it.id.toIntOrNull() ?: 0) == vehicleId }
    // Filter payments
    val paidPayments = state.loanPayments.filter { it.isPaid }
    val pendingPayments = state.loanPayments.filter { !it.isPaid }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(Res.string.finance_payment_history))
                        vehicle?.let {
                            Text(
                                text = it.registrationNumber,
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
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            state.isLoading -> LoadingContent()
            state.error != null -> ErrorContent(
                error = state.error?.resolve() ?: stringResource(Res.string.finance_error_generic),
                onRetry = { viewModel.sendIntent(Intent.SelectVehicle(vehicleId)) }
            )
            purchase == null || !purchase.isFinanced -> {
                EmptyContent(
                    title = stringResource(Res.string.finance_no_loan_title),
                    iconRes = Res.drawable.ic_info,
                    message = stringResource(Res.string.finance_no_loan_message)
                )
            }
            else -> EmiPaymentHistoryContent(
                purchase = purchase,
                paidPayments = paidPayments,
                pendingPayments = pendingPayments,
                notAvailableLabel = stringResource(Res.string.not_applicable_short),
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
@Composable
private fun EmiPaymentHistoryContent(
    purchase: VehiclePurchase,
    paidPayments: List<LoanPayment>,
    pendingPayments: List<LoanPayment>,
    notAvailableLabel: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val breakpoint = rememberFleetBreakpoint()
        // On Expanded the timeline is capped to a readable width and centered
        // instead of stretching edge-to-edge; Compact/Medium fill the width.
        val contentWidthModifier = if (breakpoint.isExpanded) {
            Modifier.fillMaxWidth().widthIn(max = FleetTokens.Width.MaxContent)
        } else {
            Modifier.fillMaxWidth()
        }
        LazyColumn(
            modifier = contentWidthModifier.fillMaxHeight().align(Alignment.TopCenter),
            contentPadding = PaddingValues(FleetTokens.Spacing.L),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
        ) {
            // Summary Card - Use purchase values from API for accuracy
            item {
                PaymentSummaryCard(
                    purchase = purchase,
                    paidCount = purchase.emisPaid,  // Use API value instead of list size
                    totalPaid = purchase.totalPaid,   // Use API value for total paid
                    notAvailableLabel = notAvailableLabel
                )
            }
            // Paid Payments Section
            item {
                Text(
                    text = stringResource(Res.string.finance_paid_emis_header, purchase.emisPaid),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CashGreen
                )
            }
            if (paidPayments.isEmpty()) {
                item {
                    FleetSectionCard(
                        containerColor = MaterialTheme.colorScheme.background,
                        border = null
                    ) {
                        EmptyContent(
                            title = stringResource(Res.string.finance_no_payments_yet),
                            message = stringResource(Res.string.finance_record_first_emi_hint),
                            fillMaxSize = false
                        )
                    }
                }
            } else {
                items(paidPayments) { payment ->
                    PaymentHistoryCard(payment = payment)
                }
            }
            // Pending/Upcoming Section - Only show genuinely upcoming EMIs
            // Filter out EMIs that might be marked as pending/overdue but user has already paid
            // Use purchase.emisPaid to determine how many EMIs have actually been paid
            val genuinelyUpcoming = pendingPayments.filter { payment ->
                // Only show EMIs with number greater than paid count
                val emiNum = payment.emiNumber ?: 0
                emiNum > purchase.emisPaid
            }.sortedBy { it.emiNumber }

            if (genuinelyUpcoming.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                    Text(
                        text = stringResource(Res.string.finance_upcoming_emis_header, purchase.emisRemaining),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarningOrange
                    )
                }

                // Show next 3 upcoming EMIs
                items(genuinelyUpcoming.take(3)) { payment ->
                    PendingPaymentCard(payment = payment)
                }

                if (genuinelyUpcoming.size > 3) {
                    item {
                        FleetSectionCard(
                            containerColor = MaterialTheme.colorScheme.background,
                            border = null
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(
                                        Res.string.finance_more_emis_scheduled,
                                        genuinelyUpcoming.size - 3
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = WarningOrange
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXL)) }
        }
    }
}
@Composable
private fun PaymentSummaryCard(
    purchase: VehiclePurchase,
    paidCount: Int,
    totalPaid: Double,
    notAvailableLabel: String
) {
    FleetTitledSectionCard(
        title = stringResource(Res.string.finance_payment_summary_title),
        accent = LoanBlue
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
        ) {
            // Loan Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.finance_percent_complete, "${purchase.loanProgressPercent.toInt()}%"),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = LoanBlue
                    )
                    Text(
                        text = stringResource(Res.string.finance_emis_slash, paidCount, purchase.tenureMonths),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = LoanBlueDark
                    )
                }
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                LinearProgressIndicator(
                    progress = { purchase.loanProgressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(FleetTokens.Spacing.M)
                        .clip(RoundedCornerShape(FleetTokens.Radius.Pill)),
                    color = LoanBlue,
                    trackColor = LoanBlue.copy(alpha = 0.2f)
                )
            }

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    value = paidCount.toString(),
                    label = stringResource(Res.string.finance_stat_emis_paid),
                    color = CashGreen
                )
                SummaryItem(
                    value = purchase.emisRemaining.toString(),
                    label = stringResource(Res.string.finance_stat_remaining),
                    color = WarningOrange
                )
                SummaryItem(
                    value = purchase.tenureMonths.toString(),
                    label = stringResource(Res.string.finance_stat_total),
                    color = LoanBlue
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Financial Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.finance_total_paid_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(totalPaid),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CashGreen
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(Res.string.finance_outstanding_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(purchase.outstandingBalance),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CriticalRed
                    )
                }
            }

            // Monthly EMI Info
            if (purchase.loanStatus == LoanStatus.ACTIVE) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.finance_monthly_emi_badge),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatCurrency(purchase.emiAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LoanBlueDark
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(Res.string.finance_row_financier),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = purchase.financierName ?: notAvailableLabel,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    value: String,
    label: String,
    color: Color
) {
    FleetMetricTile(
        value = value,
        label = label,
        accent = color,
        valueColor = color,
        showBackground = false,
        centered = true
    )
}

/**
 * Format an epoch-ms timestamp for display ("DD-MMM-YYYY"). Treats null/0 as unset.
 */
private fun formatDateDisplay(timestampMillis: Long?): String =
    if (timestampMillis == null || timestampMillis <= 0L) ""
    else formatDateToHumanReadable(timestampMillis)

@Composable
private fun PaymentHistoryCard(
    payment: LoanPayment,
    onClick: (() -> Unit)? = null
) {
    FleetSectionCard(
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payment.localizedEmiLabel(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CashGreen
                )
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXS))
                Text(
                    text = stringResource(
                        Res.string.finance_paid_on,
                        formatDateDisplay(payment.paymentDate)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                payment.paymentMode?.let {
                    Text(
                        text = it.localizedLabel(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(payment.amount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CashGreen
                )
                if (payment.lateFee > 0) {
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXS))
                    Text(
                        text = stringResource(
                            Res.string.finance_late_fee_plus,
                            formatCurrency(payment.lateFee)
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = CriticalRed
                    )
                }
            }
        }
    }
}
@Composable
private fun PendingPaymentCard(payment: LoanPayment) {
    val accentColor = if (payment.isOverdue) CriticalRed else WarningOrange

    FleetSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                ) {
                    Text(
                        text = payment.localizedEmiLabel(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                    if (payment.isOverdue) {
                        Surface(
                            shape = RoundedCornerShape(FleetTokens.Radius.M),
                            color = CriticalRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = stringResource(Res.string.finance_emi_overdue_badge),
                                modifier = Modifier.padding(
                                    horizontal = FleetTokens.Spacing.XS + FleetTokens.Spacing.XXS,
                                    vertical = FleetTokens.Spacing.XXS
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CriticalRed
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXS))
                Text(
                    text = stringResource(
                        Res.string.finance_due_colon,
                        formatDateDisplay(payment.dueDate)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formatCurrency(payment.amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}
