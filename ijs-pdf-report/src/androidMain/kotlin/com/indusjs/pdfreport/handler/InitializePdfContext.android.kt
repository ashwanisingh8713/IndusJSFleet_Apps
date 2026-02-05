package com.indusjs.pdfreport.handler

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.indusjs.pdfreport.PdfContextInitializer

/**
 * Android implementation of InitializePdfContext.
 * Sets the context from LocalContext.current.
 */
@Composable
actual fun InitializePdfContext() {
    val context = LocalContext.current
    PdfContextInitializer.initialize(context)
}
