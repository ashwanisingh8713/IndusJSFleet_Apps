package com.ijs.customer.presentation.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetSearchField
import com.indusjs.uicomponents.components.LoadingContent
import com.ijs.customer.presentation.detail.CustomerDetailContract.Intent
import com.ijs.customer.presentation.detail.CustomerDetailContract.State

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
            FleetSearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Search by Trip ID, amount, reference..."
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
                PaymentsList(
                    groupedPayments = groupedPayments,
                    hasMorePayments = state.hasMorePayments,
                    isLoadingPayments = state.isLoadingPayments,
                    onLoadMore = { onIntent(Intent.LoadMorePayments) }
                )
            }
        }
    }
}

@Composable
private fun PaymentsList(
    groupedPayments: Map<String, List<com.ijs.customer.domain.entity.CustomerPayment>>,
    hasMorePayments: Boolean,
    isLoadingPayments: Boolean,
    onLoadMore: () -> Unit
) {
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

        if (hasMorePayments) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingPayments) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        TextButton(onClick = onLoadMore) {
                            Text("Load More")
                        }
                    }
                }
            }
        }
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

/**
 * Format currency with Indian suffix (K, L, Cr).
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
