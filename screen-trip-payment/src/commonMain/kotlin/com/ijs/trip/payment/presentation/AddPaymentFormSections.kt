package com.ijs.trip.payment.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.theme.FleetTokens
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ijs.trip.payment.domain.entity.*
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Trip selection section — tap to open the trip selector bottom sheet.
 */
@Composable
internal fun TripSelectionSection(
    selectedTrip: TripSummaryForPayment?,
    error: UiText?,
    onSelectTrip: () -> Unit
) {
    Column {
        Text(
            text = stringResource(Res.string.payment_add_select_trip),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedCard(
            onClick = onSelectTrip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedTrip != null) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.payment_trip_id, selectedTrip.id),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = selectedTrip.routeDisplay,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedTrip.vehicleRegistration,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Text(
                        text = stringResource(Res.string.payment_add_tap_to_select),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    painter = painterResource(Res.drawable.ic_search),
                    contentDescription = stringResource(Res.string.payment_cd_select_trip),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        error?.let {
            Text(
                text = it.resolve(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Card showing selected trip info: price, pending, customer, dates.
 */
@Composable
internal fun SelectedTripInfoCard(
    trip: TripSummaryForPayment,
    startDateTimeDisplay: String,
    endDateTimeDisplay: String
) {
    FleetSectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        border = null,
        contentPadding = FleetTokens.Spacing.M
    ) {
            // Row 1: Trip Price, Pending, Customer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.payment_add_trip_price),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trip.tripPriceDisplay,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.payment_add_pending),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trip.pendingDisplay,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (trip.hasPendingAmount)
                            com.indusjs.uicomponents.theme.FleetStatusColors.PaymentPending
                        else
                            com.indusjs.uicomponents.theme.FleetStatusColors.PaymentReceived
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.payment_add_customer),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trip.customerName?.take(12) ?: stringResource(Res.string.label_not_applicable),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Row 2: Start & End Date/Time
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🚀 ${stringResource(Res.string.payment_trip_start_label)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = startDateTimeDisplay,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🏁 ${stringResource(Res.string.payment_trip_end_label)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = endDateTimeDisplay,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
    }
}

/**
 * Amount input section: amount, TDS, discount, and net amount display.
 */
@Composable
internal fun AmountSection(
    amount: String,
    tdsAmount: String,
    discountAmount: String,
    netAmount: String,
    amountError: UiText?,
    onAmountChange: (String) -> Unit,
    onTdsChange: (String) -> Unit,
    onDiscountChange: (String) -> Unit
) {
    Column {
        Text(
            text = stringResource(Res.string.payment_section_amount),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text(stringResource(Res.string.payment_label_amount)) },
            placeholder = { Text(stringResource(Res.string.payment_placeholder_amount)) },
            prefix = { Text("₹") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = amountError != null,
            supportingText = amountError?.let { err -> { Text(err.resolve()) } },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = tdsAmount,
                onValueChange = onTdsChange,
                label = { Text(stringResource(Res.string.payment_label_tds)) },
                prefix = { Text("₹") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            OutlinedTextField(
                value = discountAmount,
                onValueChange = onDiscountChange,
                label = { Text(stringResource(Res.string.payment_label_discount)) },
                prefix = { Text("₹") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        // Net amount display
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(Res.string.payment_label_net_amount),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = netAmount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Payment type dropdown (advance, partial, final, refund).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PaymentTypeDropdown(
    selectedType: PaymentType,
    onTypeSelected: (PaymentType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = stringResource(Res.string.payment_section_type),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = "${selectedType.icon} ${selectedType.localizedDisplayName()}",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                supportingText = {
                    Text(
                        text = selectedType.localizedDescription(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                PaymentType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "${type.icon} ${type.localizedDisplayName()} - ${type.localizedDescription()}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Payment mode selection chips (cash, UPI, bank transfer, card, credit).
 */
@Composable
internal fun PaymentModeChips(
    selectedMode: PaymentMode,
    onModeSelected: (PaymentMode) -> Unit
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentMode.entries.take(3).forEach { mode ->
                FilterChip(
                    selected = selectedMode == mode,
                    onClick = { onModeSelected(mode) },
                    label = { Text("${mode.icon} ${mode.localizedDisplayName()}") }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentMode.entries.drop(3).forEach { mode ->
                FilterChip(
                    selected = selectedMode == mode,
                    onClick = { onModeSelected(mode) },
                    label = { Text("${mode.icon} ${mode.localizedDisplayName()}") }
                )
            }
        }
    }
}

