package com.indusjs.pdfreport

/**
 * iOS implementation of PdfContextInitializer.
 * No special context needed on iOS.
 */
actual object PdfContextInitializer {

    actual fun initialize(context: Any?) {
        // No-op on iOS - context not needed
    }
}
