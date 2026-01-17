package com.indusjs.fleet.core.util

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Android implementation of rememberPhoneDialer.
 * Opens the phone dialer using ACTION_DIAL intent.
 */
@Composable
actual fun rememberPhoneDialer(phoneNumber: String): () -> Unit {
    val context = LocalContext.current

    return remember(phoneNumber) {
        {
            // Clean the phone number - keep only digits and + for international
            val cleanNumber = phoneNumber.filter { it.isDigit() || it == '+' }

            if (cleanNumber.isNotBlank()) {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$cleanNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Handle case where no dialer app is available
                    e.printStackTrace()
                }
            }
        }
    }
}
