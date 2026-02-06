package com.indusjs.pdfreport

/**
 * JS implementation of PdfContextInitializer.
 * No-op as JS doesn't require a context for PDF generation.
 */
actual object PdfContextInitializer {
    actual fun initialize(context: Any?) {
        // No-op for JS - context not needed
    }
}
