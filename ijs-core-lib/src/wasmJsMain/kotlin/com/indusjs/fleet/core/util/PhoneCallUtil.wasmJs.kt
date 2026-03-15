package com.indusjs.fleet.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.window

/**
 * WasmJS implementation of rememberPhoneDialer.
 * Opens the phone dialer using tel: URL in browser.
 */
@Composable
actual fun rememberPhoneDialer(phoneNumber: String): () -> Unit {
    return remember(phoneNumber) {
        {
            // Clean the phone number - keep only digits and + for international
            val cleanNumber = phoneNumber.filter { it.isDigit() || it == '+' }

            if (cleanNumber.isNotBlank()) {
                window.location.href = "tel:$cleanNumber"
            }
        }
    }
}
