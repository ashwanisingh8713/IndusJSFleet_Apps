package com.indusjs.fleet.core.util

import androidx.compose.runtime.Composable

/**
 * Returns a lambda that opens the phone dialer with the given phone number.
 * Must be called from a Composable context.
 * @param phoneNumber The phone number to dial (digits only or formatted)
 * @return A lambda that when invoked, opens the phone dialer
 */
@Composable
expect fun rememberPhoneDialer(phoneNumber: String): () -> Unit
