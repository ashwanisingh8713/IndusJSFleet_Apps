package com.ijs.trip.payment.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.fleet.core.model.shared.SelectableCustomer
import com.indusjs.uicomponents.components.DropdownOption
import com.indusjs.uicomponents.components.FleetDropdown
import com.indusjs.uicomponents.components.FleetFilterChip
import com.ijs.trip.payment.domain.entity.PaymentStatus
import com.ijs.trip.payment.domain.entity.PaymentType
import com.ijs.trip.payment.domain.entity.TripPaymentFilter
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun PaymentFilterBottomSheet(
    filter: TripPaymentFilter,
    customers: List<SelectableCustomer>,
    onDismiss: () -> Unit,
    onUpdateCustomer: (String?) -> Unit,
    onUpdateType: (PaymentType?) -> Unit,
    onUpdateStatus: (PaymentStatus?) -> Unit,
    onUpdateDateRange: (String?, String?) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    paymentStateLabels: Map<String, String> = emptyMap()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Local state for date fields
    var startDate by remember(filter.startDate) { mutableStateOf(filter.startDate ?: "") }
    var endDate by remember(filter.endDate) { mutableStateOf(filter.endDate ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.payment_filter_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = {
                    startDate = ""
                    endDate = ""
                    onReset()
                }) {
                    Text(stringResource(Res.string.action_reset_all))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Customer Section (most-used filter, kept at the top for quick access)
            if (customers.isNotEmpty()) {
                val allCustomersLabel = stringResource(Res.string.payment_filter_all_customers)
                val customerOptions = remember(customers, allCustomersLabel) {
                    listOf(DropdownOption<String?>(null, allCustomersLabel)) +
                        customers.map { customer ->
                            val label = customer.companyName.ifBlank { customer.personName }
                            DropdownOption<String?>(customer.id, label)
                        }
                }
                FleetDropdown(
                    label = stringResource(Res.string.payment_filter_customer),
                    options = customerOptions,
                    selectedOptionId = filter.customerId,
                    onOptionSelected = { onUpdateCustomer(it) },
                    placeholder = allCustomersLabel,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Payment Type Section
            Text(
                text = stringResource(Res.string.payment_filter_type),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FleetFilterChip(
                    selected = filter.paymentType == null,
                    onClick = { onUpdateType(null) },
                    label = stringResource(Res.string.action_select_all)
                )
                PaymentType.entries.forEach { type ->
                    FleetFilterChip(
                        selected = filter.paymentType == type,
                        onClick = { onUpdateType(type) },
                        label = type.localizedDisplayName()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Payment Status Section
            Text(
                text = stringResource(Res.string.payment_filter_status),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FleetFilterChip(
                    selected = filter.paymentStatus == null,
                    onClick = { onUpdateStatus(null) },
                    label = stringResource(Res.string.action_select_all)
                )
                PaymentStatus.entries.forEach { status ->
                    FleetFilterChip(
                        selected = filter.paymentStatus == status,
                        onClick = { onUpdateStatus(status) },
                        label = "${status.icon} ${paymentStateLabels[status.apiValue] ?: status.localizedDisplayName()}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Date Range Section
            Text(
                text = stringResource(Res.string.payment_filter_date_range),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FleetDateTimePicker(
                    date = startDate,
                    time = "",
                    onDateTimeChange = { date, _ ->
                        startDate = date
                        onUpdateDateRange(date.ifEmpty { null }, endDate.ifEmpty { null })
                    },
                    modifier = Modifier.weight(1f),
                    mode = PickerMode.DATE_ONLY,
                    label = stringResource(Res.string.payment_label_from_date),
                    maxDate = endDate.ifEmpty { null }
                )

                FleetDateTimePicker(
                    date = endDate,
                    time = "",
                    onDateTimeChange = { date, _ ->
                        endDate = date
                        onUpdateDateRange(startDate.ifEmpty { null }, date.ifEmpty { null })
                    },
                    modifier = Modifier.weight(1f),
                    mode = PickerMode.DATE_ONLY,
                    label = stringResource(Res.string.payment_label_to_date),
                    minDate = startDate.ifEmpty { null }
                )
            }

            // Quick date range options
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(Res.string.payment_filter_quick_select),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Get today's date using FleetDateTime utility
            val todayFormatted = remember { FleetDateTime.today() }
            val todayValue = remember { FleetDateTime.now() }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // This Month
                AssistChip(
                    onClick = {
                        val startOfMonth = formatLocalDateToDDMMYYYY(
                            todayValue.year, todayValue.month, 1
                        )
                        startDate = startOfMonth
                        endDate = todayFormatted
                        onUpdateDateRange(startOfMonth, todayFormatted)
                    },
                    label = { Text(stringResource(Res.string.payment_filter_this_month), style = MaterialTheme.typography.labelSmall) }
                )

                AssistChip(
                    onClick = {
                        val lastMonthYear = if (todayValue.month == 1) todayValue.year - 1 else todayValue.year
                        val lastMonthNumber = if (todayValue.month == 1) 12 else todayValue.month - 1
                        val startOfLastMonth = formatLocalDateToDDMMYYYY(lastMonthYear, lastMonthNumber, 1)
                        val endOfLastMonth = formatLocalDateToDDMMYYYY(
                            lastMonthYear, lastMonthNumber,
                            getLastDayOfMonth(lastMonthYear, lastMonthNumber)
                        )
                        startDate = startOfLastMonth
                        endDate = endOfLastMonth
                        onUpdateDateRange(startOfLastMonth, endOfLastMonth)
                    },
                    label = { Text(stringResource(Res.string.payment_filter_last_month), style = MaterialTheme.typography.labelSmall) }
                )

                AssistChip(
                    onClick = {
                        // Use FleetDateTime.addDays with date string format
                        val last7Formatted = FleetDateTime.addDays(todayFormatted, -7) ?: todayFormatted
                        startDate = last7Formatted
                        endDate = todayFormatted
                        onUpdateDateRange(last7Formatted, todayFormatted)
                    },
                    label = { Text(stringResource(Res.string.payment_filter_last_7_days), style = MaterialTheme.typography.labelSmall) }
                )

                AssistChip(
                    onClick = {
                        // Use FleetDateTime.addDays with date string format
                        val last30Formatted = FleetDateTime.addDays(todayFormatted, -30) ?: todayFormatted
                        startDate = last30Formatted
                        endDate = todayFormatted
                        onUpdateDateRange(last30Formatted, todayFormatted)
                    },
                    label = { Text(stringResource(Res.string.payment_filter_last_30_days), style = MaterialTheme.typography.labelSmall) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.payment_filter_apply))
            }
        }
    }
}

/**
 * Format date to DD-MM-YYYY format.
 */

internal fun formatLocalDateToDDMMYYYY(year: Int, month: Int, day: Int): String {
    return "${day.toString().padStart(2, '0')}-${month.toString().padStart(2, '0')}-$year"
}

/**
 * Get the last day of a given month.
 */

internal fun getLastDayOfMonth(year: Int, month: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        else -> 30
    }
}

/**
 * Empty content shown when filters are applied but no results found.
 * Shows "Clear Filters" instead of "Add Payment".
 */

