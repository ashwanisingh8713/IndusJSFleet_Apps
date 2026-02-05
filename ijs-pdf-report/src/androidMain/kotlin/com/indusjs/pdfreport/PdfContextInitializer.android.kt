package com.indusjs.pdfreport

import android.content.Context

/**
 * Android implementation of PdfContextInitializer.
 * Sets the Android Context required for WebView-based PDF generation.
 */
actual object PdfContextInitializer {

    private var context: Context? = null

    /**
     * Initialize with Android Context.
     * @param context Must be an Android Context
     */
    actual fun initialize(context: Any?) {
        this.context = context as? Context
    }

    /**
     * Get the stored Android Context.
     * Used internally by PdfGenerator.
     */
    fun getContext(): Context? = context
}
