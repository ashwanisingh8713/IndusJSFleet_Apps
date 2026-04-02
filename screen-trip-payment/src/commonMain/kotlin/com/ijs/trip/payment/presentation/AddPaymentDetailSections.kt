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
import com.ijs.trip.payment.domain.entity.PaymentMode

/**
 * Date & time picker section for payment date.
 */
@Composable
internal fun DateTimeSection(
    date: String,
    time: String,
    error: String?,
    minDate: String? = null,
    maxDate: String? = null,
    onDateTimeChange: (String, String) -> Unit
) {
    FleetDateTimePicker(
        date = date,
        time = time,
        onDateTimeChange = onDateTimeChange,
        mode = PickerMode.DATE_TIME,
        label = "Payment Date & Time *",
        isError = error != null,
        errorMessage = error,
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
            text = "Transaction Details",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        when (paymentMode) {
            PaymentMode.UPI -> {
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = onTransactionIdChange,
                    label = { Text("UPI ID *") },
                    placeholder = { Text("e.g., user@upi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            PaymentMode.BANK_TRANSFER -> {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = onBankNameChange,
                    label = { Text("Bank Name *") },
                    placeholder = { Text("Enter bank name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = paymentSource,
                    onValueChange = onPaymentSourceChange,
                    label = { Text("Account Number *") },
                    placeholder = { Text("Enter account number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = onTransactionIdChange,
                    label = { Text("Transaction Reference") },
                    placeholder = { Text("Enter transaction ID (optional)") },
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
                    label = { Text("Reference (Optional)") },
                    placeholder = { Text("Enter reference if any") },
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
            text = "Additional Info",
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
                label = { Text("Location") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
    }
}

