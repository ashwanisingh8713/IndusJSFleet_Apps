package com.ijs.trip.payment.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.trip.payment.domain.entity.PaymentMode
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Date & time picker section for payment date.
 */
@Composable
internal fun DateTimeSection(
    date: String,
    time: String,
    error: UiText?,
    minDate: String? = null,
    maxDate: String? = null,
    onDateTimeChange: (String, String) -> Unit
) {
    FleetDateTimePicker(
        date = date,
        time = time,
        onDateTimeChange = onDateTimeChange,
        mode = PickerMode.DATE_TIME,
        label = stringResource(Res.string.payment_label_date_time),
        isError = error != null,
        errorMessage = error?.resolve(),
        minDate = minDate,
        maxDate = maxDate
    )
}

/**
 * Transaction details section — varies by payment mode.
 */
@Composable
internal fun TransactionDetailsSection(
    paymentMode: PaymentMode,
    transactionId: String,
    bankName: String,
    paymentSource: String,
    onTransactionIdChange: (String) -> Unit,
    onBankNameChange: (String) -> Unit,
    onPaymentSourceChange: (String) -> Unit
) {
    Column {
        Text(
            text = stringResource(Res.string.payment_section_transaction),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

        when (paymentMode) {
            PaymentMode.UPI -> {
                FleetInputField(
                    value = transactionId,
                    onValueChange = onTransactionIdChange,
                    fieldType = FieldType.DEFAULT,
                    label = stringResource(Res.string.payment_label_upi_id),
                    placeholder = stringResource(Res.string.payment_placeholder_upi_id),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            PaymentMode.BANK_TRANSFER -> {
                FleetInputField(
                    value = bankName,
                    onValueChange = onBankNameChange,
                    fieldType = FieldType.DEFAULT,
                    label = stringResource(Res.string.payment_label_bank_name),
                    placeholder = stringResource(Res.string.payment_placeholder_bank_name),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                FleetInputField(
                    value = paymentSource,
                    onValueChange = onPaymentSourceChange,
                    fieldType = FieldType.NUMBER,
                    label = stringResource(Res.string.payment_label_account_number),
                    placeholder = stringResource(Res.string.payment_placeholder_account_number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                FleetInputField(
                    value = transactionId,
                    onValueChange = onTransactionIdChange,
                    fieldType = FieldType.DEFAULT,
                    label = stringResource(Res.string.payment_label_transaction_ref),
                    placeholder = stringResource(Res.string.payment_placeholder_transaction_ref),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            PaymentMode.CARD -> {
                FleetInputField(
                    value = transactionId,
                    onValueChange = onTransactionIdChange,
                    fieldType = FieldType.DEFAULT,
                    label = stringResource(Res.string.payment_label_transaction_id),
                    placeholder = stringResource(Res.string.payment_placeholder_card_txn),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> {
                FleetInputField(
                    value = transactionId,
                    onValueChange = onTransactionIdChange,
                    fieldType = FieldType.DEFAULT,
                    label = stringResource(Res.string.payment_label_reference),
                    placeholder = stringResource(Res.string.payment_placeholder_reference),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Additional info section: notes, received by, location.
 */
@Composable
internal fun AdditionalInfoSection(
    notes: String,
    receivedBy: String,
    receivedAtLocation: String,
    onNotesChange: (String) -> Unit,
    onReceivedByChange: (String) -> Unit,
    onLocationChange: (String) -> Unit
) {
    Column {
        Text(
            text = stringResource(Res.string.payment_section_additional),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
        ) {
            FleetInputField(
                value = receivedBy,
                onValueChange = onReceivedByChange,
                fieldType = FieldType.DEFAULT,
                label = stringResource(Res.string.payment_label_received_by),
                modifier = Modifier.weight(1f)
            )
            FleetInputField(
                value = receivedAtLocation,
                onValueChange = onLocationChange,
                fieldType = FieldType.DEFAULT,
                label = stringResource(Res.string.payment_label_location),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

        FleetInputField(
            value = notes,
            onValueChange = onNotesChange,
            fieldType = FieldType.NOTES,
            label = stringResource(Res.string.payment_label_notes),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

