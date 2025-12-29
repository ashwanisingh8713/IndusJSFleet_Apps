package com.indusjs.fleet.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

/**
 * Date input field with DD-MM-YYYY format using VisualTransformation.
 * Stores only digits internally but displays with dashes.
 */
@Composable
fun DateInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Date",
    error: String? = null,
    modifier: Modifier = Modifier
) {
    // Extract only digits from value for internal storage
    val digitsOnly = value.filter { it.isDigit() }.take(8)

    OutlinedTextField(
        value = digitsOnly,
        onValueChange = { newValue ->
            // Only accept digits, limit to 8
            val filtered = newValue.filter { it.isDigit() }.take(8)
            // Return formatted value with dashes for external storage
            onValueChange(formatDateForDisplay(filtered))
        },
        label = { Text(label) },
        placeholder = { Text("DD-MM-YYYY") },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = error != null,
        supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        visualTransformation = DateVisualTransformation()
    )
}

/**
 * Time input field with HH:MM format using VisualTransformation.
 * Stores only digits internally but displays with colon.
 */
@Composable
fun TimeInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Time",
    error: String? = null,
    modifier: Modifier = Modifier
) {
    // Extract only digits from value for internal storage
    val digitsOnly = value.filter { it.isDigit() }.take(4)

    OutlinedTextField(
        value = digitsOnly,
        onValueChange = { newValue ->
            // Only accept digits, limit to 4
            val filtered = newValue.filter { it.isDigit() }.take(4)
            // Return formatted value with colon for external storage
            onValueChange(formatTimeForDisplay(filtered))
        },
        label = { Text(label) },
        placeholder = { Text("HH:MM") },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = error != null,
        supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        visualTransformation = TimeVisualTransformation()
    )
}

/**
 * Format digits to DD-MM-YYYY display format.
 */
private fun formatDateForDisplay(digits: String): String {
    val sb = StringBuilder()
    for (i in digits.indices) {
        if (i == 2 || i == 4) {
            sb.append('-')
        }
        sb.append(digits[i])
    }
    return sb.toString()
}

/**
 * Format digits to HH:MM display format.
 */
private fun formatTimeForDisplay(digits: String): String {
    val sb = StringBuilder()
    for (i in digits.indices) {
        if (i == 2) {
            sb.append(':')
        }
        sb.append(digits[i])
    }
    return sb.toString()
}

