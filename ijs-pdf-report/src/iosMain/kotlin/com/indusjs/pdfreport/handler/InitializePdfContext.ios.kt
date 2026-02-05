package com.indusjs.pdfreport.handler

import androidx.compose.runtime.Composable

/**
 * iOS implementation of InitializePdfContext.
 * No-op on iOS as context is not needed.
 */
@Composable
actual fun InitializePdfContext() {
    // No-op on iOS
}
