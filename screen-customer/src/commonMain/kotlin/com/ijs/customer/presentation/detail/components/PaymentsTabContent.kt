package com.ijs.customer.presentation.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.datetimeutils.FleetEpoch
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetSearchField
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.customer.presentation.detail.CustomerDetailContract.Intent
import com.ijs.customer.presentation.detail.CustomerDetailContract.State
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
    val unknownMonthLabel = stringResource(Res.string.payment_unknown)

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
    val groupedPayments = remember(filteredPayments, unknownMonthLabel) {
        filteredPayments.groupBy { payment ->
            payment.date?.let { ms ->
                FleetEpoch.toValue(ms)?.let { value ->
                    "${FleetDateTime.getMonthNameShort(value.month)} ${value.year}"
                }
            } ?: unknownMonthLabel
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search Bar (collapsible)
        if (isSearchVisible) {
            FleetSearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(Res.string.customer_search_payments)
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
                    error = state.paymentsError.resolve(),
                    onRetry = { onIntent(Intent.RefreshPayments) }
                )
            }
            filteredPayments.isEmpty() -> {
                if (searchQuery.isNotEmpty()) {
                    EmptyContent(
                        title = stringResource(Res.string.payment_no_results),
                        message = stringResource(Res.string.customer_payment_search_empty_hint),
                        iconRes = Res.drawable.ic_search
                    )
                } else {
                    EmptyContent(
                        title = stringResource(Res.string.customer_no_payments_title),
                        message = stringResource(Res.string.customer_no_payments_message),
                        iconRes = Res.drawable.ic_cost
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
        contentPadding = PaddingValues(FleetTokens.Spacing.M),
        verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
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
                        .padding(FleetTokens.Spacing.L),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingPayments) {
                        CircularProgressIndicator(modifier = Modifier.size(FleetTokens.IconSize.Default))
                    } else {
                        TextButton(onClick = onLoadMore) {
                            Text(stringResource(Res.string.action_load_more))
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
            .padding(vertical = FleetTokens.Spacing.S),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_calendar),
                contentDescription = null,
                modifier = Modifier.size(FleetTokens.IconSize.S),
                tint = MaterialTheme.colorScheme.primary
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

