package com.indusjs.fleet.core.pdf

import androidx.compose.runtime.Composable
import com.indusjs.fleet.presentation.customers.detail.CustomerTripsPdfData

/**
 * iOS implementation of CustomerTripsPdfExportHandler.
 * Uses a stub implementation for now.
 */
@Composable
actual fun CustomerTripsPdfExportHandler(
    pdfData: CustomerTripsPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    // iOS implementation will use platform-specific PDF generation
    // For now, just complete the operation
    if (pdfData != null) {
        onExportError("PDF export is not yet available on iOS")
    }
}
