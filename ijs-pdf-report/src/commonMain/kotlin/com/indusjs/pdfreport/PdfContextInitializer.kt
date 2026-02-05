package com.indusjs.pdfreport

/**
 * Platform-specific context initializer for PDF generation.
 * On Android, this sets the Context required for WebView-based PDF generation.
 * On other platforms, this is a no-op.
 */
expect object PdfContextInitializer {
    /**
     * Initialize the PDF context with platform-specific context object.
     * @param context Platform-specific context (Android Context, etc.)
     */
    fun initialize(context: Any?)
}
