package com.ijs.trip.payment.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.indusjs.uicomponents.components.DropdownOption
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetDropdown
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.indusjs.uicomponents.theme.FleetTokens
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

        FleetSectionCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSelectTrip,
            border = if (error != null) {
                androidx.compose.foundation.BorderStroke(
                    FleetTokens.Height.Divider,
                    MaterialTheme.colorScheme.error
                )
            } else {
                androidx.compose.foundation.BorderStroke(
                    FleetTokens.Height.Divider,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            },
            contentPadding = FleetTokens.Spacing.L
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.padding(start = FleetTokens.Spacing.L, top = FleetTokens.Spacing.XS)
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
                            FleetStatusColors.PaymentPending
                        else
                            FleetStatusColors.PaymentReceived
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
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.payment_trip_start_label),
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
                        text = stringResource(Res.string.payment_trip_end_label),
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
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

        // Live inline validation: surface the existing amount-error string while typing
        // an invalid value; the VM also sets amountError on Save (both gate submit).
        val liveAmountError = if (
            amount.isNotBlank() &&
            !com.indusjs.fleet.core.util.ValidationUtils.validateAmount(amount).isValid
        ) {
            UiText.StringRes(Res.string.error_valid_amount)
        } else null
        val effectiveAmountError = amountError ?: liveAmountError

        FleetInputField(
            value = amount,
            onValueChange = onAmountChange,
            fieldType = FieldType.DECIMAL,
            label = stringResource(Res.string.payment_label_amount),
            placeholder = stringResource(Res.string.payment_placeholder_amount),
            leadingIcon = { Text("₹") },
            modifier = Modifier.fillMaxWidth(),
            isError = effectiveAmountError != null,
            errorMessage = effectiveAmountError?.resolve()
        )

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
        ) {
            FleetInputField(
                value = tdsAmount,
                onValueChange = onTdsChange,
                fieldType = FieldType.DECIMAL,
                label = stringResource(Res.string.payment_label_tds),
                leadingIcon = { Text("₹") },
                modifier = Modifier.weight(1f)
            )
            FleetInputField(
                value = discountAmount,
                onValueChange = onDiscountChange,
                fieldType = FieldType.DECIMAL,
                label = stringResource(Res.string.payment_label_discount),
                leadingIcon = { Text("₹") },
                modifier = Modifier.weight(1f)
            )
        }

        // Net amount display
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(FleetTokens.Radius.M))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(FleetTokens.Spacing.M),
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
 * Payment Type + Payment Mode as two side-by-side dropdowns ("dual"), followed by a one-line
 * description of the selected type. Both are canonical [FleetDropdown]s with clean text labels
 * (no emoji) for a consistent, polished look.
 */
@Composable
internal fun PaymentTypeAndModeSection(
    selectedType: PaymentType,
    selectedMode: PaymentMode,
    onTypeSelected: (PaymentType) -> Unit,
    onModeSelected: (PaymentMode) -> Unit
) {
    Column {
        // localizedDisplayName() is @Composable; List.map is inline so its lambda runs in
        // composable scope, making these calls legal.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            FleetDropdown(
                label = stringResource(Res.string.payment_section_type),
                options = PaymentType.entries.map { type ->
                    DropdownOption(id = type, label = type.localizedDisplayName())
                },
                selectedOptionId = selectedType,
                onOptionSelected = onTypeSelected,
                modifier = Modifier.weight(1f)
            )
            FleetDropdown(
                label = stringResource(Res.string.payment_add_payment_mode),
                options = PaymentMode.entries.map { mode ->
                    DropdownOption(id = mode, label = mode.localizedDisplayName())
                },
                selectedOptionId = selectedMode,
                onOptionSelected = onModeSelected,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
        Text(
            text = selectedType.localizedDescription(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = FleetTokens.Spacing.S)
        )
    }
}

