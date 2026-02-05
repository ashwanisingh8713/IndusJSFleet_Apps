package com.indusjs.fleet.presentation.finance

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.fleet.core.ui.EmptyContent
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.FinanceColors
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.domain.entity.finance.*
import com.indusjs.fleet.presentation.finance.VehicleFinanceContract.Intent
import indusjsfleet.sharedui.generated.resources.*
import org.jetbrains.compose.resources.painterResource

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
                        Text("Payment History")
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
                            contentDescription = "Back"
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
                error = state.error ?: "Something went wrong",
                onRetry = { viewModel.sendIntent(Intent.SelectVehicle(vehicleId)) }
            )
            purchase == null || !purchase.isFinanced -> {
                EmptyContent(
                    title = "No loan information",
                    iconRes = Res.drawable.ic_info,
                    message = "This vehicle doesn't have loan/finance information"
                )
            }
            else -> EmiPaymentHistoryContent(
                purchase = purchase,
                paidPayments = paidPayments,
                pendingPayments = pendingPayments,
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
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Card - Use purchase values from API for accuracy
        item {
            PaymentSummaryCard(
                purchase = purchase,
                paidCount = purchase.emisPaid,  // Use API value instead of list size
                totalPaid = purchase.totalPaid   // Use API value for total paid
            )
        }
        // Paid Payments Section
        item {
            Text(
                text = "Paid EMIs (${purchase.emisPaid})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CashGreen
            )
        }
        if (paidPayments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No payments recorded yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Record your EMI payments to track history",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
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
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Upcoming EMIs (${purchase.emisRemaining})",
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${genuinelyUpcoming.size - 3} more EMIs scheduled",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = WarningOrange
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}
@Composable
private fun PaymentSummaryCard(
    purchase: VehiclePurchase,
    paidCount: Int,
    totalPaid: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Payment Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LoanBlueDark
            )

            // Loan Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${purchase.loanProgressPercent}% Complete",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = LoanBlue
                    )
                    Text(
                        text = "$paidCount/${purchase.tenureMonths} EMIs",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = LoanBlueDark
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
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

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    value = paidCount.toString(),
                    label = "EMIs Paid",
                    color = CashGreen
                )
                SummaryItem(
                    value = purchase.emisRemaining.toString(),
                    label = "Remaining",
                    color = WarningOrange
                )
                SummaryItem(
                    value = purchase.tenureMonths.toString(),
                    label = "Total",
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
                        text = "Total Paid",
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
                        text = "Outstanding",
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
                            text = "Monthly EMI",
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
                            text = "Financier",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = purchase.financierName ?: "N/A",
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Format date for display using FleetDateTime.
 * Output format: "DD-MMM-YYYY"
 */
private fun formatDateDisplay(dateString: String?): String =
    FleetDateTime.formatAnyToDisplayDate(dateString)

@Composable
private fun PaymentHistoryCard(
    payment: LoanPayment,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payment.emiLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CashGreen
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Paid on ${formatDateDisplay(payment.paymentDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                payment.paymentMode?.let {
                    Text(
                        text = it.label,
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
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "+ ${formatCurrency(payment.lateFee)} late fee",
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = payment.emiLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                    if (payment.isOverdue) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CriticalRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "OVERDUE",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CriticalRed
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Due: ${formatDateDisplay(payment.dueDate)}",
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
