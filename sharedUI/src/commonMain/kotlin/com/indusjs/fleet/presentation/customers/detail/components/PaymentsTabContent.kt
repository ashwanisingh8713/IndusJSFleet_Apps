package com.indusjs.fleet.presentation.customers.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.fleet.core.ui.EmptyContent
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.domain.entity.customer.*
import com.indusjs.fleet.presentation.customers.detail.CustomerDetailContract.Intent
import com.indusjs.fleet.presentation.customers.detail.CustomerDetailContract.State
import org.jetbrains.compose.resources.painterResource
import indusjsfleet.sharedui.generated.resources.*

/**
 * Received Payments Tab Content.
 * Shows all payments received from customer with mode filter, search, and monthly grouping.
 * Only visible to Owner and General Manager.
 */
@Composable
fun PaymentsTabContent(
    state: State,
    onIntent: (Intent) -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    // Filter payments by search query
    val filteredPayments = remember(state.receivedPayments, searchQuery) {
        if (searchQuery.isBlank()) {
            state.receivedPayments
        } else {
            state.receivedPayments.filter { payment ->
                payment.tripId?.contains(searchQuery, ignoreCase = true) == true ||
                payment.modeDisplay.contains(searchQuery, ignoreCase = true) ||
                payment.amountDisplay.contains(searchQuery, ignoreCase = true) ||
                payment.receiptNumber?.contains(searchQuery, ignoreCase = true) == true ||
                payment.referenceNumber?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    // Group payments by month for better navigation
    val groupedPayments = remember(filteredPayments) {
        filteredPayments.groupBy { payment ->
            payment.date?.let { date ->
                FleetDateTime.formatIsoToMonthYear(date)
            } ?: "Unknown"
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search Bar (collapsible)
        if (isSearchVisible) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Search by Trip ID, amount, reference...") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_search),
                        contentDescription = "Search",
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_close),
                                contentDescription = "Clear",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Filter Chips with Search Toggle
        PaymentModeFilterChips(
            selectedMode = state.paymentModeFilter,
            onModeSelected = { onIntent(Intent.SetPaymentModeFilter(it)) },
            isSearchActive = isSearchVisible,
            onSearchToggle = { isSearchVisible = !isSearchVisible },
            resultCount = filteredPayments.size
        )

        // Payment Summary Card
        state.paymentSummary?.let { summary ->
            PaymentSummaryCard(
                summary = summary,
                onExportPdf = onExportPdf,
                isExporting = state.isExporting
            )
        }

        when {
            state.isLoadingPayments && state.receivedPayments.isEmpty() -> {
                LoadingContent()
            }
            state.paymentsError != null && state.receivedPayments.isEmpty() -> {
                ErrorContent(
                    error = state.paymentsError,
                    onRetry = { onIntent(Intent.RefreshPayments) }
                )
            }
            filteredPayments.isEmpty() -> {
                if (searchQuery.isNotEmpty()) {
                    EmptyContent(
                        title = "No results found",
                        message = "Try a different search term",
                        icon = "🔍"
                    )
                } else {
                    EmptyContent(
                        title = "No payments recorded",
                        message = "No payments received from this customer yet",
                        icon = "💳"
                    )
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Grouped by month
                    groupedPayments.forEach { (month, payments) ->
                        // Month Header
                        item(key = "header_$month") {
                            MonthHeader(
                                month = month,
                                count = payments.size,
                                total = formatCurrency(payments.sumOf { it.amount })
                            )
                        }

                        // Payment items for this month
                        items(payments, key = { "payment_${it.id}" }) { payment ->
                            EnhancedPaymentItem(payment = payment)
                        }
                    }

                    if (state.hasMorePayments) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.isLoadingPayments) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    TextButton(onClick = { onIntent(Intent.LoadMorePayments) }) {
                                        Text("Load More")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Format currency with suffix
 */
private fun formatCurrency(amount: Double): String {
    return when {
        amount >= 10000000 -> {
            val value = amount / 10000000
            "₹${((value * 10).toInt() / 10.0)}Cr"
        }
        amount >= 100000 -> {
            val value = amount / 100000
            "₹${((value * 10).toInt() / 10.0)}L"
        }
        amount >= 1000 -> {
            val value = amount / 1000
            "₹${((value * 10).toInt() / 10.0)}K"
        }
        else -> "₹${amount.toInt()}"
    }
}

@Composable
private fun MonthHeader(
    month: String,
    count: Int,
    total: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📅",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = month,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "($count)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = total,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PaymentModeFilterChips(
    selectedMode: PaymentMode?,
    onModeSelected: (PaymentMode?) -> Unit,
    isSearchActive: Boolean = false,
    onSearchToggle: () -> Unit = {},
    resultCount: Int = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // All filter
            item {
                FilterChip(
                    selected = selectedMode == null,
                    onClick = { onModeSelected(null) },
                    label = { Text("All") },
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // Mode filters
            items(PaymentMode.entries) { mode ->
                FilterChip(
                    selected = selectedMode == mode,
                    onClick = { onModeSelected(mode) },
                    label = { Text("${mode.icon} ${mode.displayName}") },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // Search toggle
        IconButton(onClick = onSearchToggle) {
            Icon(
                painter = painterResource(Res.drawable.ic_search),
                contentDescription = "Search",
                modifier = Modifier.size(20.dp),
                tint = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PaymentSummaryCard(
    summary: CustomerPaymentSummary,
    onExportPdf: () -> Unit,
    isExporting: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💰 Total Received",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = summary.totalAmountDisplay,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onExportPdf,
                        enabled = !isExporting,
                        modifier = Modifier.size(32.dp)
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("📄", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            if (summary.byMode.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Payment Mode Breakdown
                Text(
                    text = "Payment Breakdown",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    summary.byMode.forEach { modeData ->
                        PaymentModeRow(modeData)
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentModeRow(data: PaymentByMode) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(data.modeIcon, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = data.modeDisplay,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = data.amountDisplay,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = data.percentageDisplay,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PaymentItem(
    payment: CustomerPayment,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mode Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = payment.modeIcon,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Column {
                    Text(
                        text = "${payment.modeDisplay} Payment",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = payment.tripDisplay,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        payment.date?.let { date ->
                            Text(
                                text = "• $date",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    payment.referenceNumber?.takeIf { it.isNotBlank() }?.let { ref ->
                        Text(
                            text = "Ref: $ref",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = payment.amountDisplay,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Enhanced Payment Item with proper date format and compact layout.
 */
@Composable
fun EnhancedPaymentItem(
    payment: CustomerPayment,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Row 1: Mode, Trip ID, Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mode Icon Badge
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = payment.modeIcon,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Column {
                        Text(
                            text = payment.modeDisplay,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = payment.tripDisplay,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = payment.amountDisplay,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Date/Time, Payment Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date with proper format: DD-MMM-YYYY HH:mm
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "📅",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = payment.date?.let { FleetDateTime.formatIsoToDisplayDateTime(it) } ?: "-",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Payment Type Badge
                PaymentTypeBadge(type = payment.paymentType ?: "payment")
            }

            // Row 3: Receipt/Reference (if available)
            val hasReceipt = !payment.receiptNumber.isNullOrBlank()
            val hasReference = !payment.referenceNumber.isNullOrBlank()

            if (hasReceipt || hasReference) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    payment.receiptNumber?.takeIf { it.isNotBlank() }?.let { receipt ->
                        Text(
                            text = "Receipt: $receipt",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    payment.referenceNumber?.takeIf { it.isNotBlank() }?.let { ref ->
                        Text(
                            text = "Ref: $ref",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentTypeBadge(type: String) {
    val (bgColor, textColor, displayText) = when (type.lowercase()) {
        "advance" -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Advance"
        )
        "partial" -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Partial"
        )
        "final" -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Final"
        )
        "refund" -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Refund"
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            type.replaceFirstChar { it.uppercase() }
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
