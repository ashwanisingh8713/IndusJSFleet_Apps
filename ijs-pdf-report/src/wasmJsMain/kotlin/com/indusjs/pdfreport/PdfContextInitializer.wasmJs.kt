package com.indusjs.pdfreport

/**
 * WasmJS implementation of PdfContextInitializer.
 * No-op as WasmJS doesn't require a context for PDF generation.
 */
actual object PdfContextInitializer {
    actual fun initialize(context: Any?) {
        // No-op for WasmJS - context not needed
    }
}
