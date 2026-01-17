package com.indusjs.fleet.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * iOS implementation of rememberPhoneDialer.
 * Opens the phone dialer using tel: URL scheme.
 */
@Composable
actual fun rememberPhoneDialer(phoneNumber: String): () -> Unit {
    return remember(phoneNumber) {
        {
            // Clean the phone number - keep only digits and + for international
            val cleanNumber = phoneNumber.filter { it.isDigit() || it == '+' }

            if (cleanNumber.isNotBlank()) {
                val urlString = "tel:$cleanNumber"
                val url = NSURL.URLWithString(urlString)

                if (url != null && UIApplication.sharedApplication.canOpenURL(url)) {
                    UIApplication.sharedApplication.openURL(url)
                }
            }
        }
    }
}
