package com.ijs.trip.payment.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.uicomponents.components.UiText
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
        Spacer(modifier = Modifier.height(8.dp))

        when (paymentMode) {
            PaymentMode.UPI -> {
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = onTransactionIdChange,
                    label = { Text(stringResource(Res.string.payment_label_upi_id)) },
                    placeholder = { Text(stringResource(Res.string.payment_placeholder_upi_id)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            PaymentMode.BANK_TRANSFER -> {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = onBankNameChange,
                    label = { Text(stringResource(Res.string.payment_label_bank_name)) },
                    placeholder = { Text(stringResource(Res.string.payment_placeholder_bank_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = paymentSource,
                    onValueChange = onPaymentSourceChange,
                    label = { Text(stringResource(Res.string.payment_label_account_number)) },
                    placeholder = { Text(stringResource(Res.string.payment_placeholder_account_number)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = onTransactionIdChange,
                    label = { Text(stringResource(Res.string.payment_label_transaction_ref)) },
                    placeholder = { Text(stringResource(Res.string.payment_placeholder_transaction_ref)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            PaymentMode.CARD -> {
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = onTransactionIdChange,
                    label = { Text("Transaction ID") },
                    placeholder = { Text("Enter card transaction ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            else -> {
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = onTransactionIdChange,
                    label = { Text(stringResource(Res.string.payment_label_reference)) },
                    placeholder = { Text(stringResource(Res.string.payment_placeholder_reference)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = receivedBy,
                onValueChange = onReceivedByChange,
                label = { Text("Received By") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = receivedAtLocation,
                onValueChange = onLocationChange,
                label = { Text(stringResource(Res.string.payment_label_location)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text(stringResource(Res.string.payment_label_notes)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
    }
}

